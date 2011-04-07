/**
 * 
 */
package bclibs.analysis.opcodes;

import static bclibs.analysis.Opcodes.OpParameterType.U2;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

import bclibs.analysis.Context;
import bclibs.analysis.decoders.DecodedMethodInvocationOp;

public class MethodInvocationOpcode extends Op {
	public MethodInvocationOpcode(int code) {
		super(code, U2);
	}
	public boolean isInstanceMethod() {
		return code != Opcodes.INVOKESTATIC;
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