package bclibs.analysis.opcodes;

import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.decoders.DecodedConstantPushOp;

public class ConstantPushOpcode extends BasicOpcode {
	public final int baseCode;
	
	public ConstantPushOpcode(int code, OpParameterType... opParameterTypes) {
		this(code, code, opParameterTypes);
	}
	
	public ConstantPushOpcode(int code, int baseCode, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
		this.baseCode = baseCode;
	}
	
	@Override
	public DecodedConstantPushOp decode(Context context, int index) {
		return new DecodedConstantPushOp(this, context, index);
	}
}
