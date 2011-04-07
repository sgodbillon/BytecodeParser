package bclibs.analysis.opcodes;

import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.decoders.DecodedArrayOp;

public class ArrayOpcode extends BasicOpcode {
	public final boolean isLoad;
	
	public ArrayOpcode(int code, boolean isLoad, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
		this.isLoad = isLoad;
	}
	
	@Override
	public DecodedArrayOp decode(Context context, int index) {
		return new DecodedArrayOp(this, context, index);
	}
}
