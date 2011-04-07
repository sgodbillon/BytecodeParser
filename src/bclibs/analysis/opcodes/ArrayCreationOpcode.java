/**
 * 
 */
package bclibs.analysis.opcodes;

import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.decoders.DecodedArrayCreationOp;

public class ArrayCreationOpcode extends BasicOpcode {
	public ArrayCreationOpcode(int code, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
	}
	@Override
	public DecodedArrayCreationOp decode(Context context, int index) {
		return new DecodedArrayCreationOp(this, context, index);
	}
}