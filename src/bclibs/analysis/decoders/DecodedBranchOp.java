package bclibs.analysis.decoders;

import bclibs.analysis.Context;
import bclibs.analysis.opcodes.BasicOpcode;

public class DecodedBranchOp extends DecodedOp {
	private final int jump;
	
	public DecodedBranchOp(BasicOpcode op, Context context, int index) {
		super(op, context, index);
		jump = parameterValues[0];
	}
	
	public int getJump() {
		return jump + index;
	}
}
