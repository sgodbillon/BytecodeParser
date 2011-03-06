package bclibs.analysis;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;

import bclibs.LocalVariable;
import bclibs.utils.LocalVariableOpcodes;
import bclibs.utils.LocalVariableOpcodes.LocalVariableOp;

public class Stack {
	protected final CtBehavior behavior;
	protected final Map<Integer, LocalVariable> variables;
	protected final LinkedList<StackElement> stack = new LinkedList<StackElement>();
	
	public Stack(CtBehavior behavior) {
		this.behavior = behavior;
		this.variables = LocalVariable.findVariables(behavior);
	}
	
	public void process(int fromIndex, int to) throws BadBytecode {
		System.out.println("process from " + fromIndex + " to " + to);
		CodeIterator it = behavior.getMethodInfo().getCodeAttribute().iterator();
		it.move(fromIndex);
		int index = fromIndex;
		while(it.hasNext() && index < to) {
			index = it.next();
			int op = it.byteAt(index);
			String opName = null;
			for(Field field : Opcode.class.getFields()) {
				if(Modifier.isStatic(field.getModifiers())) {
					try {
						int foundOp = field.getInt(null);
						if(foundOp == op) {
							opName = field.getName();
							break;
						}
					} catch (Exception e) {
						//...
					}
				}
			}
			OperationHandler handler = operationHandlers.get(op);
			System.out.println("found handler " + handler + " for op " + op + " (" + opName + ")");
		}
	}
	
	protected StackElement pop() {
		StackElement removed = stack.pop();
		if(removed instanceof DWElement)
			throw new RuntimeException("inconstancy!");
		return removed;
	}
	
	protected StackElement pop2() {
		StackElement removed = stack.pop();
		if(!(removed instanceof DWElement))
			throw new RuntimeException("inconstancy!");
		return stack.pop();
	}
	
	protected void push(StackElement stackElement) {
		stack.push(stackElement);
	}
	
	protected void push(StackElement stackElement, int indexFromTop) {
		int index = stack.size() - indexFromTop;
		stack.add(index, stackElement);
	}
	
	protected void pushDoubleWord(StackElement stackElement) {
		stack.push(stackElement);
		stack.push(new DWElement());
	}
	
	protected void pushDoubleWord(StackElement stackElement, int indexFromTop) {
		int index = stack.size() - indexFromTop;
		stack.add(index, new DWElement());
		stack.add(index, stackElement);
	}
	
	// ****** classes ****** //
	public static interface OperationHandler {
		void handle(Stack stack, CodeIterator iterator, int index);
	}
	
	public static class SimplePopHandler implements OperationHandler {
		public boolean doubleWord;
		
		public SimplePopHandler(boolean doubleWord) {
			this.doubleWord = doubleWord;
		}
		
		@Override
		public void handle(Stack stack, CodeIterator iterator, int index) {
			if(doubleWord)
				stack.pop2();
			else stack.pop();
		}
	}
	
	public static class SimplePushHandler implements OperationHandler {
		public final boolean doubleWord;
		public SimplePushHandler(boolean doubleWord) {
			this.doubleWord = doubleWord;
		}
		@Override
		public void handle(Stack stack, CodeIterator iterator, int index) {
			stack.push(new OtherElement());
			if(doubleWord)
				stack.push(new DWElement());
		}
	}
	
	public static class LocalVariablePushHandler implements OperationHandler {
		public final boolean doubleWord;
		public LocalVariablePushHandler(boolean doubleWord) {
			this.doubleWord = doubleWord;
		}
		
		@Override
		public void handle(Stack stack, CodeIterator iterator, int index) {
			try {
				LocalVariableOp localVariableOp = LocalVariableOpcodes.getLocalVariableOp(iterator.get(), index);
				LocalVariable v = LocalVariable.getLocalVariable(localVariableOp.varIndex, index, stack.variables);
				ValueFromLocalVariable stackElement = new ValueFromLocalVariable(v);
				if(doubleWord)
					stack.pushDoubleWord(stackElement);
				else stack.push(stackElement);
			} catch (BadBytecode e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static class DupHandler implements OperationHandler {
		public final boolean doubleWord;
		public final int indexFromTop;
		public DupHandler(boolean doubleWord, int indexFromTop) {
			this.doubleWord = doubleWord;
			this.indexFromTop = indexFromTop;
		}
		@Override
		public void handle(Stack stack, CodeIterator iterator, int index) {
			if(doubleWord)
				stack.pushDoubleWord(stack.stack.get(stack.stack.size() - 2).copy(), indexFromTop + 1);
			else stack.push(stack.stack.get(stack.stack.size() - 1).copy(), indexFromTop);
		}
	}
	
	public static class SimplePushPopHandler implements OperationHandler {
		public final boolean doubleWord;
		public SimplePushPopHandler(boolean doubleWord) {
			this.doubleWord = doubleWord;
		}
		public void handle(Stack stack, CodeIterator iterator, int index) {
			if(doubleWord) {
				stack.pop2();
				stack.pushDoubleWord(new OtherElement());
			}
		}
	}
	
	public static class NMHandler implements OperationHandler {
		public final boolean doubleWordPops;
		public final int pops;
		public final boolean doubleWordPushes;
		public final int pushes;
		public NMHandler(boolean doubleWordPops, int pops, boolean doubleWordPushes, int pushes) {
			this.doubleWordPops = doubleWordPops;
			this.pops = pops;
			this.doubleWordPushes = doubleWordPushes;
			this.pushes = pushes;
		}
		public void handle(Stack stack, CodeIterator iterator, int index) {
			for(int i = 0; i < pops; i++) {
				if(doubleWordPops)
					stack.pop2();
				else stack.pop();
			}
			for(int i = 0; i < pushes; i++) {
				if(doubleWordPushes)
					stack.pushDoubleWord(new OtherElement());
				else stack.push(new OtherElement());
			}
		}
	}
	
	public static abstract class StackElement { 
		public abstract StackElement copy();
		@Override public String toString() {
			return this.getClass().toString();
		}
	}
	
	public static class OtherElement extends StackElement {
		@Override
		public StackElement copy() {
			return new OtherElement();
		}
	}
	
	public static class DWElement extends StackElement {
		@Override
		public StackElement copy() {
			return new DWElement();
		}
	}
	
	public static class ValueFromLocalVariable extends StackElement {
		public final LocalVariable localVariable;
		public ValueFromLocalVariable(LocalVariable localVariable) {
			this.localVariable = localVariable;
		}
		@Override
		public StackElement copy() {
			return new ValueFromLocalVariable(localVariable);
		}
		
		@Override
		public String toString() {
			return "ValueFromLocalVariable '" + localVariable.name + "'";
		}
	}
	
	
	final static HashMap<Integer, OperationHandler> operationHandlers = new HashMap<Integer, OperationHandler>();
	
	final static OperationHandler PUSH_HANDLER = new SimplePushHandler(false);
	final static OperationHandler PUSH2_HANDLER = new SimplePushHandler(true);
	final static OperationHandler POP_HANDLER = new SimplePopHandler(false);
	final static OperationHandler POP2_HANDLER = new SimplePopHandler(true);
	final static OperationHandler LOCAL_VARIABLE_PUSH_HANDLER = new LocalVariablePushHandler(false);
	final static OperationHandler LOCAL_VARIABLE_PUSH2_HANDLER = new LocalVariablePushHandler(true);
	final static OperationHandler PUSH_POP_HANDLER = new SimplePushPopHandler(false);
	final static OperationHandler PUSH_POP2_HANDLER = new SimplePushPopHandler(true);
	
	static {
		operationHandlers.put(Opcode.BIPUSH, PUSH_HANDLER);
		operationHandlers.put(Opcode.SIPUSH, PUSH_HANDLER);
		operationHandlers.put(Opcode.LDC, PUSH_HANDLER);
		operationHandlers.put(Opcode.LDC_W, PUSH_HANDLER);
		operationHandlers.put(Opcode.LDC2_W, PUSH2_HANDLER);
		operationHandlers.put(Opcode.ACONST_NULL, PUSH_HANDLER);
		operationHandlers.put(Opcode.ICONST_M1, PUSH_HANDLER);
		operationHandlers.put(Opcode.ICONST_0, PUSH_HANDLER);
		operationHandlers.put(Opcode.ICONST_1, PUSH_HANDLER);
		operationHandlers.put(Opcode.ICONST_2, PUSH_HANDLER);
		operationHandlers.put(Opcode.ICONST_3, PUSH_HANDLER);
		operationHandlers.put(Opcode.ICONST_4, PUSH_HANDLER);
		operationHandlers.put(Opcode.ICONST_5, PUSH_HANDLER);
		operationHandlers.put(Opcode.LCONST_0, PUSH2_HANDLER);
		operationHandlers.put(Opcode.LCONST_1, PUSH2_HANDLER);
		operationHandlers.put(Opcode.FCONST_0, PUSH_HANDLER);
		operationHandlers.put(Opcode.FCONST_1, PUSH_HANDLER);
		operationHandlers.put(Opcode.FCONST_2, PUSH_HANDLER);
		operationHandlers.put(Opcode.DCONST_0, PUSH2_HANDLER);
		operationHandlers.put(Opcode.DCONST_1, PUSH2_HANDLER);
		
		operationHandlers.put(Opcode.NOP, new OperationHandler() {
			@Override public void handle(Stack stack, CodeIterator iterator, int index) { }
		});
		
		operationHandlers.put(Opcode.POP, POP_HANDLER);
		operationHandlers.put(Opcode.POP2, POP2_HANDLER);
		
		operationHandlers.put(Opcode.DUP, new DupHandler(false, 1));
		operationHandlers.put(Opcode.DUP2, new DupHandler(true, 1));
		operationHandlers.put(Opcode.DUP_X1, new DupHandler(false, 2));
		operationHandlers.put(Opcode.DUP2_X1, new DupHandler(true, 2));
		operationHandlers.put(Opcode.DUP_X2, new DupHandler(false, 3));
		operationHandlers.put(Opcode.DUP2_X2, new DupHandler(true, 3));
		operationHandlers.put(Opcode.SWAP, new OperationHandler() {
			@Override
			public void handle(Stack stack, CodeIterator iterator, int index) {
				stack.push(stack.pop(), 1);
			}
		});

		// adfil
		operationHandlers.put(Opcode.ALOAD, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.ALOAD_0, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.ALOAD_1, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.ALOAD_2, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.ALOAD_3, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.DLOAD, LOCAL_VARIABLE_PUSH2_HANDLER);
		operationHandlers.put(Opcode.DLOAD_0, LOCAL_VARIABLE_PUSH2_HANDLER);
		operationHandlers.put(Opcode.DLOAD_1, LOCAL_VARIABLE_PUSH2_HANDLER);
		operationHandlers.put(Opcode.DLOAD_2, LOCAL_VARIABLE_PUSH2_HANDLER);
		operationHandlers.put(Opcode.DLOAD_3, LOCAL_VARIABLE_PUSH2_HANDLER);
		operationHandlers.put(Opcode.FLOAD, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.FLOAD_0, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.FLOAD_1, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.FLOAD_2, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.FLOAD_3, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.ILOAD, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.ILOAD_0, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.ILOAD_1, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.ILOAD_2, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.ILOAD_3, LOCAL_VARIABLE_PUSH_HANDLER);
		operationHandlers.put(Opcode.LLOAD, LOCAL_VARIABLE_PUSH2_HANDLER);
		operationHandlers.put(Opcode.LLOAD_0, LOCAL_VARIABLE_PUSH2_HANDLER);
		operationHandlers.put(Opcode.LLOAD_1, LOCAL_VARIABLE_PUSH2_HANDLER);
		operationHandlers.put(Opcode.LLOAD_2, LOCAL_VARIABLE_PUSH2_HANDLER);
		operationHandlers.put(Opcode.LLOAD_3, LOCAL_VARIABLE_PUSH2_HANDLER);
		
		operationHandlers.put(Opcode.ASTORE, POP_HANDLER);
		operationHandlers.put(Opcode.ASTORE_0, POP_HANDLER);
		operationHandlers.put(Opcode.ASTORE_1, POP_HANDLER);
		operationHandlers.put(Opcode.ASTORE_2, POP_HANDLER);
		operationHandlers.put(Opcode.ASTORE_3, POP_HANDLER);
		operationHandlers.put(Opcode.DSTORE, POP2_HANDLER);
		operationHandlers.put(Opcode.DSTORE_0, POP2_HANDLER);
		operationHandlers.put(Opcode.DSTORE_1, POP2_HANDLER);
		operationHandlers.put(Opcode.DSTORE_2, POP2_HANDLER);
		operationHandlers.put(Opcode.DSTORE_3, POP2_HANDLER);
		operationHandlers.put(Opcode.FSTORE, POP_HANDLER);
		operationHandlers.put(Opcode.FSTORE_0, POP_HANDLER);
		operationHandlers.put(Opcode.FSTORE_1, POP_HANDLER);
		operationHandlers.put(Opcode.FSTORE_2, POP_HANDLER);
		operationHandlers.put(Opcode.FSTORE_3, POP_HANDLER);
		operationHandlers.put(Opcode.ISTORE, POP_HANDLER);
		operationHandlers.put(Opcode.ISTORE_0, POP_HANDLER);
		operationHandlers.put(Opcode.ISTORE_1, POP_HANDLER);
		operationHandlers.put(Opcode.ISTORE_2, POP_HANDLER);
		operationHandlers.put(Opcode.ISTORE_3, POP_HANDLER);
		operationHandlers.put(Opcode.LSTORE, POP2_HANDLER);
		operationHandlers.put(Opcode.LSTORE_0, POP2_HANDLER);
		operationHandlers.put(Opcode.LSTORE_1, POP2_HANDLER);
		operationHandlers.put(Opcode.LSTORE_2, POP2_HANDLER);
		operationHandlers.put(Opcode.LSTORE_3, POP2_HANDLER);
		
		operationHandlers.put(Opcode.NEWARRAY, PUSH_POP_HANDLER);
		operationHandlers.put(Opcode.ANEWARRAY, PUSH_POP_HANDLER);
		
		// TODO multiNewArray
		operationHandlers.put(Opcode.AALOAD, new NMHandler(false, 2, false, 1));
		operationHandlers.put(Opcode.BALOAD, new NMHandler(false, 2, false, 1));
		operationHandlers.put(Opcode.CALOAD, new NMHandler(false, 2, false, 1));
		operationHandlers.put(Opcode.DALOAD, new NMHandler(false, 2, true, 1));
		operationHandlers.put(Opcode.FALOAD, new NMHandler(false, 2, false, 1));
		operationHandlers.put(Opcode.IALOAD, new NMHandler(false, 2, false, 1));
		operationHandlers.put(Opcode.LALOAD, new NMHandler(false, 2, true, 1));
		operationHandlers.put(Opcode.SALOAD, new NMHandler(false, 2, false, 1));
		operationHandlers.put(Opcode.AASTORE, new NMHandler(false, 3, false, 0));
		operationHandlers.put(Opcode.BASTORE, new NMHandler(false, 3, false, 0));
		operationHandlers.put(Opcode.CASTORE, new NMHandler(false, 3, false, 0));
		OperationHandler doubleASTOREHandler = new OperationHandler() {
			
			@Override
			public void handle(Stack stack, CodeIterator iterator, int index) {
				stack.pop2();
				stack.pop();
				stack.pop();
			}
		};
		operationHandlers.put(Opcode.DASTORE, doubleASTOREHandler);
		operationHandlers.put(Opcode.FASTORE, new NMHandler(false, 3, false, 0));
		operationHandlers.put(Opcode.IASTORE, new NMHandler(false, 3, false, 0));
		operationHandlers.put(Opcode.LASTORE, doubleASTOREHandler);
		operationHandlers.put(Opcode.CASTORE, new NMHandler(false, 3, false, 0));
		
		operationHandlers.put(Opcode.ARRAYLENGTH, PUSH_POP_HANDLER);
		operationHandlers.put(Opcode.NEW, PUSH_HANDLER);
		// TODO PUTFIELD, PUTSTATIC, GETFIELD, GETSTATIC
	}
	
}
