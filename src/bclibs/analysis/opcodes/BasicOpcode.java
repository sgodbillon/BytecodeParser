/**
 * 
 */
package bclibs.analysis.opcodes;

import static bclibs.analysis.stack.Stack.StackElementLength;
import static bclibs.analysis.stack.Stack.StackElementLength.*;
import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.decoders.DecodedOp;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.Whatever;

public class BasicOpcode extends Op {
	private StackElementLength[] pops, pushes;
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
	public DecodedOp decode(Context context, int index) {
		return new DecodedOp(this, context, index);
	}
	@Override
	public void simulate(Stack stack, Context context, int index) {
		for(int i = 0; i < getPops().length; i++) {
			if(getPops()[i] == DOUBLE)
				stack.pop2();
			else stack.pop();
		}
		for(int i = 0; i < getPushes().length; i++) {
			if(getPushes()[i] == DOUBLE)
				stack.push2(new Whatever());
			else stack.push(new Whatever());
		}
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