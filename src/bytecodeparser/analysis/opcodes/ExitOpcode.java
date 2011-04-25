package bytecodeparser.analysis.opcodes;

import bytecodeparser.analysis.Opcodes.OpParameterType;
import bytecodeparser.analysis.stack.Stack.StackElementLength;

public class ExitOpcode extends BasicOpcode {
	public ExitOpcode(int code, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
	}
	public ExitOpcode(int code, StackElementLength[] pops, StackElementLength[] pushes, OpParameterType... opParameterTypes) {
		super(code, pops, pushes, opParameterTypes);
	}
}
