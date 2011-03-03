package bclibs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import bclibs.LocalVariablesFinder.LocalVariable;
import bclibs.LocalVariablesFinder.LocalVariableAccess;
import bclibs.utils.Utils;
import javassist.CtBehavior;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

public class LocalVariableTraceEnhancer {
	private final CtBehavior behavior;
	private final LocalVariablesFinder finder;
	
	public LocalVariableTraceEnhancer(CtBehavior behavior) throws BadBytecode {
		this.behavior = behavior;
		this.finder = new LocalVariablesFinder(behavior);
	}
	
	public void proceed() throws Exception {
		LineNumberAttribute lineNumberAttribute = Utils.getLineNumberAttribute(behavior);
		for(int i = 0; i < lineNumberAttribute.tableLength(); i++) {
			int line = lineNumberAttribute.lineNumber(i);
			Set<LocalVariableAccess> localVariableAccesses = finder.variableAccesses.get(line);
			if(localVariableAccesses != null) {
				for(LocalVariableAccess lva : localVariableAccesses) {
					LocalVariable v = lva.localVariable;
					CodeIterator iterator = behavior.getMethodInfo().getCodeAttribute().iterator();
					int index = lva.index;
					/*if(i == lineNumberAttribute.tableLength() - 1) { // last
						index = iterator.getCodeLength() - 1;
					} else {
						index = lineNumberAttribute.startPc(i + 1);
					}
					if(index < v.getValidityRange()[0])
						index = v.getValidityRange()[0];
					else if (index > v.getValidityRange()[1])
						index = v.getValidityRange()[1];*/
					
					if(index < v.getValidityRange()[0]) {
						index = v.getValidityRange()[0];
					}
					iterator.move(index);
					try {
						insert(iterator, iterator.next(), "System.out.println(\"" + (lva.read ? "read" : "write") + " " + v.name + " at line " + line + " => '\" + " + v.name + "+\"'\");");
					} catch (CompileError e) {
						System.out.println(String.format("error referencing %s at index %s", v, index));
					}
				}
			}
			/*
			Set<LocalVariable> reads = finder.reads.get(line);
			if(reads != null) {
				for(LocalVariable v : reads) {
					CodeIterator iterator = behavior.getMethodInfo().getCodeAttribute().iterator();
					int index;
					if(i == lineNumberAttribute.tableLength() - 1) { // last
						index = iterator.getCodeLength() - 1;
					} else {
						index = lineNumberAttribute.startPc(i + 1);
					}
					if(index < v.getValidityRange()[0])
						index = v.getValidityRange()[0];
					else if (index > v.getValidityRange()[1])
						index = v.getValidityRange()[1];
					iterator.move(index);
					try {
					insert(iterator, iterator.next(), "System.out.println(\"read " + v.name + " at line " + line + " => '\" + " + v.name + "+\"'\");");
					} catch (CompileError e) {
						System.out.println(String.format("error referencing %s at index %s", v, index));
					}
				}
			}
			Set<LocalVariable> writes = finder.writes.get(line);
			if(writes != null) {
				for(LocalVariable v : writes) {
					CodeIterator iterator = behavior.getMethodInfo().getCodeAttribute().iterator();
					int index;
					if(i == lineNumberAttribute.tableLength() - 1) { // last
						index = iterator.getCodeLength() - 1;
					} else {
						index = lineNumberAttribute.startPc(i + 1);
					}
					if(index < v.getValidityRange()[0])
						index = v.getValidityRange()[0];
					else if (index > v.getValidityRange()[1])
						index = v.getValidityRange()[1];
					iterator.move(index);
					System.out.println(String.format("inserting at index %s for var '%s' [%s, %s]", index, v.name, v.getValidityRange()[0], v.getValidityRange()[1]));
					try {
						insert(iterator, iterator.next(), "System.out.println(\"wrote " + v.name + " at line " + line + " => '\" + " + v.name + "+\"'\");");
					} catch (CompileError e) {
						System.out.println(String.format("error referencing %s at index %s", v, index));
					}
				}
			} */
		}
		behavior.getMethodInfo().getCodeAttribute().setMaxStack(behavior.getMethodInfo().getCodeAttribute().computeMaxStack());
	}
	
	/*public static class CodeIteratorWrapper {
		private final CodeIterator iterator;
		private final Map<Integer, Integer> marks = new HashMap<Integer, Integer>();
		
		public CodeIteratorWrapper(CodeIterator iterator) {
			this.iterator = iterator;
		}
		
		public CodeAttribute getCodeAttribute() {
			return iterator.get();
		}
		
		public int insertAt(int index, byte[] bytecode) throws BadBytecode {
			return iterator.insertAt(index, bytecode);
		}
		
		public boolean hasNext() {
			return iterator.hasNext();
		}
		
		public int next() throws BadBytecode {
			return iterator.next();
		}
		
		public void move(int index) {
			iterator.move(index);
		}
		
		public int lookAhead() {
			return iterator.lookAhead();
		}
		
		public void setMark(int index) {
			
		}
		
		public int getMark(int index) {
			return 0;
		}
	}*/
	
	private int insert(CodeIterator it, int index, String statement) throws CompileError, NotFoundException, BadBytecode {
		CodeAttribute codeAttribute = behavior.getMethodInfo().getCodeAttribute();
		Javac javac = new Javac(behavior.getDeclaringClass());
		javac.recordLocalVariables(codeAttribute, index);
		javac.setMaxLocals(codeAttribute.getMaxLocals());
		javac.recordParams(behavior.getParameterTypes(), Modifier.isStatic(behavior.getModifiers()));
		javac.compileStmnt(statement);
		Bytecode bytecode = javac.getBytecode();
		int maxStack = bytecode.getMaxStack();
		if(maxStack > codeAttribute.getMaxStack()) {
			System.out.println("set max stack to " + maxStack);
			codeAttribute.setMaxStack(maxStack);
		}
		it.insertAt(index, javac.getBytecode().get());
		//codeAttribute.setMaxStack(codeAttribute.computeMaxStack());
		return bytecode.length();
	}
}
