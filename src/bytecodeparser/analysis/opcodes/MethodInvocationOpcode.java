/**
 * 
 */
package bytecodeparser.analysis.opcodes;

import static bytecodeparser.analysis.Opcodes.OpParameterType.U2;
import javassist.bytecode.Opcode;
import bytecodeparser.Context;
import bytecodeparser.analysis.decoders.DecodedMethodInvocationOp;

public class MethodInvocationOpcode extends Op {
	public MethodInvocationOpcode(int code) {
		super(code, U2);
	}
	public boolean isInstanceMethod() {
		return code != Opcode.INVOKESTATIC;
	}
	@Override
	public DecodedMethodInvocationOp decode(Context context, int index) {
		try {
			return new DecodedMethodInvocationOp(this, context, index);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}