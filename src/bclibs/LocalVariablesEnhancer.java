package bclibs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.SignatureAttribute.ArrayType;
import javassist.bytecode.SignatureAttribute.ClassType;
import javassist.bytecode.SignatureAttribute.ObjectType;
import javassist.bytecode.analysis.Analyzer;
import javassist.bytecode.analysis.Frame;
import javassist.bytecode.analysis.FramePrinter;
import javassist.bytecode.analysis.Type;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import bclibs.utils.LocalVariableOpcodes;
import bclibs.utils.Utils;
import bclibs.utils.LocalVariableOpcodes.LocalVariableOp;

public class LocalVariablesEnhancer {
	public class LocalVariable {
		public final String name;
		public final int index;
		public final LocalVariableType type;
		
		public int[] getValidityRange() {
			int[] result = new int[2];
			LocalVariableAttribute localVariableAttribute = Utils.getLocalVariableAttribute(behavior);
			result[0] = localVariableAttribute.startPc(index);
			result[1] = result[0] + localVariableAttribute.codeLength(index);
			return result;
		}
		
		public int getSlot() {
			return Utils.getLocalVariableAttribute(behavior).index(index);
		}
		
		public LocalVariable(int index, String name, LocalVariableType type) {
			this.index = index;
			this.name = name;
			this.type = type;
		}
		@Override
		public String toString() {
			return name + " (" + type.typeName + ") " + "[" + index + " -> " + getSlot() + "] between [" + getValidityRange()[0] + "," + getValidityRange()[1] + "]";
		}
	}
	
	public static class LocalVariableType {
		public final boolean isPrimitive;
		public final String signature;
		public final String typeName;
		public final String shortTypeName;
		public final int dimensions;
		
		private LocalVariableType(String signature, String typeName, String shortTypeName, boolean isPrimitive, int dimensions) {
			this.signature = signature;
			this.typeName = typeName;
			this.shortTypeName = shortTypeName;
			this.isPrimitive = isPrimitive;
			this.dimensions = dimensions;
		}
		public boolean isArray() {
			return dimensions > 0;
		}
		
		public static LocalVariableType parse(String signature) {
			int dimensions = 0;
			for(int i = 0; i < signature.length(); i++) {
				if(signature.charAt(i) == '[')
					dimensions++;
				else break;
			}
			try {
				javassist.bytecode.SignatureAttribute.Type objectType = SignatureAttribute.toFieldSignature(signature);
				if(objectType instanceof ArrayType)
					objectType = ((ArrayType) objectType).getComponentType();
				if(objectType instanceof ClassType) {
					String typeName = ((ClassType) objectType).getName();
					return new LocalVariableType(signature, addArrayTypeInfo(typeName, dimensions), typeName, false, dimensions);
				}
				throw new RuntimeException("unknown signature: " + signature + ", objectType=" + objectType.getClass());
			} catch(BadBytecode e) {
				// not a class
				String typeName = primitiveSymbols.get("" + signature.charAt(dimensions));
				return new LocalVariableType(signature, addArrayTypeInfo(typeName, dimensions), typeName, true, dimensions);
			}
		}
		
		private static String addArrayTypeInfo(String typeName, int dimensions) {
			String result = typeName;
			for(int i = 0; i < dimensions; i++)
				result += "[]";
			return result;
		}
		
		private static Map<String, String> primitiveSymbols = new HashMap<String, String>();
		static {
			primitiveSymbols.put("V", "void");
			primitiveSymbols.put("Z", "boolean");
			primitiveSymbols.put("B", "byte");
			primitiveSymbols.put("C", "char");
			primitiveSymbols.put("S", "short");
			primitiveSymbols.put("I", "int");
			primitiveSymbols.put("J", "long");
			primitiveSymbols.put("F", "float");
			primitiveSymbols.put("D", "double");
		}
	}
	
	/*public static class LocalVariableAccess {
		public int line;
		public int index;
		
		public LocalVariable localVariable;
		public boolean read;
		
		@Override
		public String toString() {
			return String.format("LocalVariableAccess (%s) on %s at line %s (index %s)", read ? "read" : "write", localVariable, line, index);
		}
	}*/
	
	public Map<Integer, LocalVariable> variables = new HashMap<Integer, LocalVariable>();
	//public Map<Integer, Set<LocalVariableAccess>> variableAccesses = new HashMap<Integer, Set<LocalVariableAccess>>();
	//public Map<Integer, Set<LocalVariable>> reads = new HashMap<Integer, Set<LocalVariable>>();
	//public Map<Integer, Set<LocalVariable>> writes = new HashMap<Integer, Set<LocalVariable>>();
	
	public final CtBehavior behavior;
	
	public LocalVariablesEnhancer() {
		behavior = null;
	}
	
	public LocalVariablesEnhancer(CtBehavior behavior) throws BadBytecode {
		System.out.println(behavior.getLongName());
		this.behavior = behavior;
		findLocalVariables();
	}
	
	public LocalVariable getLocalVariable(int slot, int index) {
		TreeMap<Integer, LocalVariable> variablesByDistance = new TreeMap<Integer, LocalVariable>();
		for(LocalVariable lv : variables.values())
			if(lv.getSlot() == slot) {
				int[] validityRange = lv.getValidityRange();
				if(validityRange[1] >= index) {
					if(validityRange[0] <= index)
						return lv;
					else
						variablesByDistance.put(validityRange[0] - index, lv);
				}
			}
		/*if(variablesByDistance.size() > 1) {
			System.out.println("choosing between");
			for(Entry<Integer, LocalVariable> entry : variablesByDistance.entrySet())
				System.out.println("\t " + entry.getKey() + " : " + entry.getValue());
		}*/
		return variablesByDistance.firstEntry().getValue();
	}
	
	public List<LocalVariable> getCandidate(int slot) {
		ArrayList<LocalVariable> candidates = new ArrayList<LocalVariable>();
		for(LocalVariable lv : variables.values())
			if(lv.getSlot() == slot)
				candidates.add(lv);
		return candidates;
	}
	
	public void proceed() throws BadBytecode {
		CodeAttribute codeAttribute = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator codeIterator = codeAttribute.iterator();
		LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute("LineNumberTable");
		
		System.out.println("original length : " + codeAttribute.length());
		
		while(codeIterator.hasNext()) {
			int index = codeIterator.next();
			
			LocalVariableOp localVariableOp = LocalVariableOpcodes.getLocalVariableOp(codeAttribute, index);
			if(localVariableOp != null) {
				/* Map<Integer, Set<LocalVariable>> map = localVariableOp.type == Opcodes.OpType.LOAD ? reads : writes;
				int line = lineNumberAttribute.toLineNumber(index);
				//System.out.println(line + ":: " + localVariableOp.type + " :>>> " + localVariableOp.varIndex);
				
				Set<LocalVariable> localVariables = map.get(line);
				if(localVariables == null) {
					localVariables = new HashSet<LocalVariable>();
					map.put(line, localVariables);
				}
				
				//List<LocalVariable> candidates = getCandidate(localVariableOp.varIndex);
				//System.out.println(String.format("at PC %s (line %s) : '%s' candidates are %s", index, line, localVariableOp.type == Opcodes.OpType.LOAD ? "read" : "write", candidates));
				LocalVariable var = getLocalVariable(localVariableOp.varIndex, index);
				//System.out.println("chose lv " + var);
				//System.out.println(String.format(line + ":: " + "add %s (for index %s)", variables.get(localVariableOp.varIndex), localVariableOp.varIndex));
				//localVariables.add(variables.get(localVariableOp.varIndex));
				localVariables.add(var);
				
				*/
				//Map<Integer, Set<LocalVariableAccesses>> map = 
				
				
				int line = lineNumberAttribute.toLineNumber(index);
				LocalVariable v = getLocalVariable(localVariableOp.varIndex, index);
				if(localVariableOp.type != LocalVariableOpcodes.LocalVariableOpType.LOAD || !v.type.isPrimitive) {
					System.out.println("yop " + v);
					try {
						insert(codeIterator, codeIterator.next(), "System.out.println(\"" + (localVariableOp.type == LocalVariableOpcodes.LocalVariableOpType.LOAD ? "read" : "write") + " " + v.name + " at line " + line + " => '\" + " + v.name + "+\"'\");");
					} catch (Exception e) {
						System.out.println(String.format("error referencing %s at index %s", v, index));
					}
				}
				/*int line = lineNumberAttribute.toLineNumber(index);
				LocalVariableAccess access = new LocalVariableAccess();
				access.index = index;
				access.line = line;
				access.localVariable = getLocalVariable(localVariableOp.varIndex, index);
				access.read = localVariableOp.type == Opcodes.OpType.LOAD;
				Set<LocalVariableAccess> localVariableAccesses = variableAccesses.get(line);
				if(localVariableAccesses == null) {
					localVariableAccesses = new HashSet<LocalVariableAccess>();
					variableAccesses.put(line, localVariableAccesses);
				}
				localVariableAccesses.add(access);*/
			}
		}
		System.out.println("new length : " + codeAttribute.length());
		codeAttribute.setMaxStack(codeAttribute.computeMaxStack());
		//new FramePrinter(System.out).print(behavior.getDeclaringClass());
	}
	
	public void yop() throws CannotCompileException, BadBytecode {
		final Frame[] frames = new Analyzer().analyze((CtMethod) behavior);
		System.out.println(frames.length + " frames");
		behavior.instrument(new ExprEditor() {
			public void edit(MethodCall m) throws CannotCompileException {
				try {
					int index = m.indexOfBytecode();
					CodeAttribute codeAttribute = behavior.getMethodInfo().getCodeAttribute();
					CodeIterator it = codeAttribute.iterator();
					System.out.println("methodcall " + m.getMethodName() + " at index " + index);
					Frame frame = frames[index];
					FrameDetails beginning = findBeginning(index, frames);
					it.move(beginning.index);
					int currentIndex = beginning.index;
					while(it.hasNext() && currentIndex < index && currentIndex >= 0) {
						it.next();
						LocalVariableOp op = LocalVariableOpcodes.getLocalVariableOp(codeAttribute, currentIndex);
						if(op != null) {
							LocalVariable v = getLocalVariable(op.varIndex, currentIndex);
							System.out.println("found variable=" + v + " on stack");
						}
						currentIndex = it.lookAhead();
					}
					System.out.println("frame [" + index + "]" + frame.toString() + " starts with frame [" + (beginning.index) + "]::: " + beginning.frame);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	private FrameDetails findBeginning(int index, Frame[] frames) {
		for(int i = index; i >= 0; i--) {
			Frame frame = frames[i];
			if(frame != null) {
			FrameDetails details = new FrameDetails(frame, i);
			//System.out.println("searching for beginning frame... at index=" + i + " length=" + details.types.size() + ", frame is " + frame);
			if(details.getStackSize() == 0)
				return details;
			}
		}
		return null;
	}

	private static class FrameDetails {
		public final Frame frame;
		public final int index;
		public List<Type> types = new ArrayList<Type>();
		public int top;
		
		public int getStackSize() {
			return top;
		}
		
		public FrameDetails(Frame frame, int index) {
			this.frame = frame;
			this.index = index;
			try {
				Field field = Frame.class.getDeclaredField("top");
				field.setAccessible(true);
				top = field.getInt(frame);
				field = Frame.class.getDeclaredField("stack");
				field.setAccessible(true);
				Type[] stack = (Type[]) field.get(frame);
				int nbTypes = 0;
				String types_ = "";
				for(int i = 0; i < top; i++) {
					Type type = stack[i];
					if(type != null) {
						if(!type.equals(Type.TOP))
							types.add(type);
						nbTypes++;
						types_ += type + " ";
					}
				}
				System.out.println(types_);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private void findLocalVariables() throws BadBytecode {
		CodeAttribute codeAttribute = behavior.getMethodInfo().getCodeAttribute();
		LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute("LocalVariableTable");
		for(int i = 0; i < localVariableAttribute.tableLength(); i++) {
			LocalVariable localVariable = new LocalVariable(i, localVariableAttribute.variableName(i), LocalVariableType.parse(localVariableAttribute.signature(i)));
			variables.put(i, localVariable);
			System.out.println("found var "+localVariable);
			//System.out.println(String.format("findLocalVariables: var %s is '%s' (slot %s)", i, localVariable.name, localVariable.getSlot()));
		}
	}
	
	private int insert(CodeIterator it, int index, String statement) throws CompileError, NotFoundException, BadBytecode {
		CodeAttribute codeAttribute = it.get();
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
		it.insertAt(index, bytecode.get());
		return bytecode.length();
	}
}
