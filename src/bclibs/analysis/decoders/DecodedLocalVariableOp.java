/**
 * 
 */
package bclibs.analysis.decoders;

import bclibs.LocalVariable;
import bclibs.analysis.Context;
import bclibs.analysis.opcodes.LocalVariableOpcode;

public class DecodedLocalVariableOp extends DecodedOp {
	public final LocalVariable localVariable;
	public final boolean load;
	
	public DecodedLocalVariableOp(LocalVariableOpcode lvo, Context context, int index) {
		super(lvo, context, index);
		int slot;
		if(parameterTypes.length > 0)
			slot = parameterValues[0];
		else slot = lvo.getCode() - lvo.getBaseOpcode();
		this.load = lvo.isLoad();
		localVariable = LocalVariable.getLocalVariable(slot, index, context.localVariables);
	}
}