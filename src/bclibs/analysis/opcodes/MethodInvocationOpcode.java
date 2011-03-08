/**
 * 
 */
package bclibs.analysis.opcodes;

import static bclibs.analysis.Opcodes.OpParameterType.U2;
import static bclibs.analysis.Opcodes.StackElementLength.*;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

import bclibs.analysis.Context;
import bclibs.analysis.decoders.DecodedMethodInvocationOp;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.Whatever;

public class MethodInvocationOpcode extends Op {
	private final DecodedMethodInvocationOp decodedOp;
	
	public MethodInvocationOpcode(int code) {
		this(code, null);
	}
	private MethodInvocationOpcode(int code, DecodedMethodInvocationOp decodedOp) {
		super(code, U2);
		this.decodedOp = decodedOp;
	}
	public boolean isInstanceMethod() {
		return code != Opcodes.INVOKESTATIC;
	}
	@Override
	public MethodInvocationOpcode init(Context context, int index) {
		return new MethodInvocationOpcode(code, decode(context, index));
	}
	@Override
	public DecodedMethodInvocationOp decode(Context context, int index) {
		if(decodedOp != null)
			return decodedOp;
		try {
			return new DecodedMethodInvocationOp(this, context, index);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public void simulate(Stack stack, Context context, int index) {
		DecodedMethodInvocationOp decodedOp = decode(context, index);
		for(int i = 0; i < decodedOp.getPops().length; i++) {
			if(decodedOp.getPops()[i] == DOUBLE)
				stack.pop2();
			else stack.pop();
		}
		for(int i = 0; i < decodedOp.getPushes().length; i++) {
			if(decodedOp.getPushes()[i] == DOUBLE)
				stack.push2(new Whatever());
			else stack.push(new Whatever());
		}
	}
}