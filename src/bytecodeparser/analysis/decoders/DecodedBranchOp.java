package bytecodeparser.analysis.decoders;

import bytecodeparser.Context;
import bytecodeparser.analysis.opcodes.BasicOpcode;

public class DecodedBranchOp extends DecodedBasicOp {
	private final int jump;
	
	public DecodedBranchOp(BasicOpcode op, Context context, int index) {
		super(op, context, index);
		jump = parameterValues[0];
	}
	
	public int getJump() {
		return jump + index;
	}
}
