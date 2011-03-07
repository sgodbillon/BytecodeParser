/**
 * 
 */
package bclibs;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javassist.CtBehavior;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import bclibs.utils.Utils;

public class LocalVariable {
	public final CtBehavior behavior;
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
	
	public LocalVariable(int index, String name, LocalVariableType type, CtBehavior behavior) {
		this.index = index;
		this.name = name;
		this.type = type;
		this.behavior = behavior;
	}
	@Override
	public String toString() {
		return name + " (" + type.typeName + ") " + "[" + index + " -> " + getSlot() + "] between [" + getValidityRange()[0] + "," + getValidityRange()[1] + "]";
	}
	
	public static Map<Integer, LocalVariable> findVariables(CtBehavior behavior) {
		Map<Integer, LocalVariable> variables = new HashMap<Integer, LocalVariable>();
		CodeAttribute codeAttribute = behavior.getMethodInfo().getCodeAttribute();
		LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute("LocalVariableTable");
		for(int i = 0; i < localVariableAttribute.tableLength(); i++) {
			LocalVariable localVariable = new LocalVariable(i, localVariableAttribute.variableName(i), LocalVariableType.parse(localVariableAttribute.signature(i)), behavior);
			variables.put(i, localVariable);
			//System.out.println("found var "+localVariable);
			//System.out.println(String.format("findLocalVariables: var %s is '%s' (slot %s)", i, localVariable.name, localVariable.getSlot()));
		}
		return variables;
	}
	
	public static LocalVariable getLocalVariable(int slot, int index, Map<Integer, LocalVariable> variables) {
		TreeMap<Integer, LocalVariable> variablesByDistance = new TreeMap<Integer, LocalVariable>();
		for(LocalVariable lv : variables.values()) {
			if(lv.getSlot() == slot) {
				int[] validityRange = lv.getValidityRange();
				if(validityRange[1] >= index) {
					if(validityRange[0] <= index)
						return lv;
					else
						variablesByDistance.put(validityRange[0] - index, lv);
				}
			}
		}
		return variablesByDistance.firstEntry().getValue();
	}
}