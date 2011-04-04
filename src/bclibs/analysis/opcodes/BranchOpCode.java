package bclibs.analysis.opcodes;

import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.decoders.DecodedBranchOp;
import bclibs.analysis.stack.Stack.StackElementLength;

public class BranchOpCode extends BasicOpcode {
	public BranchOpCode(int code, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
	}
	public BranchOpCode(int code, StackElementLength[] pops, StackElementLength[] pushes, OpParameterType... opParameterTypes) {
		super(code, pops, pushes, opParameterTypes);
	}
	
	@Override
	public DecodedBranchOp decode(Context context, int index) {
		return new DecodedBranchOp(this, context, index);
	}
}
