/**
 * 
 */
package bytecodeparser.analysis.opcodes;

import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes.OpParameterType;
import bytecodeparser.analysis.decoders.DecodedBasicOp;
import bytecodeparser.analysis.stack.Stack.StackElementLength;

public class BasicOpcode extends Op {
	protected StackElementLength[] pops, pushes;
	public BasicOpcode(int code, OpParameterType... opParameterTypes) {
		this(code, new StackElementLength[0], new StackElementLength[0], opParameterTypes);
	}
	public BasicOpcode(int code, StackElementLength[] pops, StackElementLength[] pushes, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
		this.pops = pops;
		this.pushes = pushes;
	}
	public StackElementLength[] getPops() {
		return pops;
	}
	public StackElementLength[] getPushes() {
		return pushes;
	}
	@Override
	public DecodedBasicOp decode(Context context, int index) {
		return new DecodedBasicOp(this, context, index);
	}
	@Override
	public String toString() {
		return "BasicOp: " + getName();
	}
	public BasicOpcode setPops(StackElementLength... pops) {
		if(pops != null)
			this.pops = pops;
		return this;
	}
	public BasicOpcode setPushes(StackElementLength... pushes) {
		if(pushes != null)
			this.pushes = pushes;
		return this;
	}
}