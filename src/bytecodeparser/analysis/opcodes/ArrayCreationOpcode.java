/**
 * 
 */
package bytecodeparser.analysis.opcodes;

import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes.OpParameterType;
import bytecodeparser.analysis.decoders.DecodedArrayCreationOp;

public class ArrayCreationOpcode extends BasicOpcode {
	public ArrayCreationOpcode(int code, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
	}
	@Override
	public DecodedArrayCreationOp decode(Context context, int index) {
		return new DecodedArrayCreationOp(this, context, index);
	}
}