package bytecodeparser.analysis.opcodes;

import javassist.bytecode.Opcode;
import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes.OpParameterType;
import bytecodeparser.analysis.decoders.DecodedBranchOp;
import bytecodeparser.analysis.stack.Stack.StackElementLength;

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
	
	public boolean isConditional() {
		return code >= Opcode.IFEQ && code <= Opcode.IF_ACMPNE || code == Opcode.IFNULL || code == Opcode.IFNONNULL;
	}
}
