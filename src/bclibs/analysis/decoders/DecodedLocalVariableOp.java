/**
 * 
 */
package bclibs.analysis.decoders;

import javassist.CtBehavior;
import javassist.bytecode.CodeIterator;
import bclibs.LocalVariable;
import bclibs.analysis.opcodes.LocalVariableOpcode;

public class DecodedLocalVariableOp extends DecodedOp {
	public final LocalVariable localVariable;
	public final boolean load;
	
	public DecodedLocalVariableOp(LocalVariableOpcode lvo, CtBehavior behavior, CodeIterator iterator, int index) {
		super(lvo, behavior, iterator, index);
		LocalVariable.findVariables(behavior);
		int slot;
		if(parameterTypes.length > 0)
			slot = parameterValues[0];
		else slot = lvo.getCode() - lvo.getBaseOpcode();
		this.load = lvo.isLoad();
		localVariable = LocalVariable.getLocalVariable(slot, index, LocalVariable.findVariables(behavior));
	}
}