package bclibs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import bclibs.utils.LocalVariableOpcodes;
import bclibs.utils.Utils;
import bclibs.utils.LocalVariableOpcodes.LocalVariableOp;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.analysis.FramePrinter;

public class LocalVariablesFinder {
	public class LocalVariable {
		public String name;
		public int index;
		
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
		
		public LocalVariable(int index, String name) {
			this.index = index;
			this.name = name;
		}
		@Override
		public String toString() {
			return name + "[" + index + " -> " + getSlot() + "] between [" + getValidityRange()[0] + "," + getValidityRange()[1] + "]";
		}
	}
	
	public static class LocalVariableAccess {
		public int line;
		public int index;
		
		public LocalVariable localVariable;
		public boolean read;
		
		@Override
		public String toString() {
			return String.format("LocalVariableAccess (%s) on %s at line %s (index %s)", read ? "read" : "write", localVariable, line, index);
		}
	}
	
	public Map<Integer, LocalVariable> variables = new HashMap<Integer, LocalVariable>();
	public Map<Integer, Set<LocalVariableAccess>> variableAccesses = new HashMap<Integer, Set<LocalVariableAccess>>();
	//public Map<Integer, Set<LocalVariable>> reads = new HashMap<Integer, Set<LocalVariable>>();
	//public Map<Integer, Set<LocalVariable>> writes = new HashMap<Integer, Set<LocalVariable>>();
	
	public final CtBehavior behavior;
	
	public LocalVariablesFinder() {
		behavior = null;
	}
	
	public LocalVariablesFinder(CtBehavior behavior) throws BadBytecode {
		System.out.println(behavior.getLongName());
		this.behavior = behavior;
		findLocalVariables();
		parse();
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
	
	private void parse() throws BadBytecode {
		new FramePrinter(System.out).print(behavior.getDeclaringClass());
		CodeAttribute codeAttribute = behavior.getMethodInfo().getCodeAttribute();
		CodeIterator codeIterator = codeAttribute.iterator();
		LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute("LineNumberTable");
		
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
				LocalVariableAccess access = new LocalVariableAccess();
				access.index = index;
				access.line = line;
				access.localVariable = getLocalVariable(localVariableOp.varIndex, index);
				access.read = localVariableOp.type == LocalVariableOpcodes.LocalVariableOpType.LOAD;
				Set<LocalVariableAccess> localVariableAccesses = variableAccesses.get(line);
				if(localVariableAccesses == null) {
					localVariableAccesses = new HashSet<LocalVariableAccess>();
					variableAccesses.put(line, localVariableAccesses);
				}
				localVariableAccesses.add(access);
			}
		}
	}
	
	private void findLocalVariables() {
		CodeAttribute codeAttribute = behavior.getMethodInfo().getCodeAttribute();
		LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute("LocalVariableTable");
		for(int i = 0; i < localVariableAttribute.tableLength(); i++) {
			LocalVariable localVariable = new LocalVariable(i, localVariableAttribute.variableName(i));
			variables.put(i, localVariable);
			//System.out.println(String.format("findLocalVariables: var %s is '%s' (slot %s)", i, localVariable.name, localVariable.getSlot()));
		}
	}
}
