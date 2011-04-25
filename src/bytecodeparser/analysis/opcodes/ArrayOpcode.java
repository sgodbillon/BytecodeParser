package bytecodeparser.analysis.opcodes;

import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes.OpParameterType;
import bytecodeparser.analysis.decoders.DecodedArrayOp;

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
