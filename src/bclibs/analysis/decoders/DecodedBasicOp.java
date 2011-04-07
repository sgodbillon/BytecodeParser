/**
 * 
 */
package bclibs.analysis.decoders;

import bclibs.analysis.Context;
import bclibs.analysis.opcodes.BasicOpcode;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.Stack.StackElementLength;

public class DecodedBasicOp extends DecodedOp {
	public final StackElementLength[] pops;
	public final StackElementLength[] pushes;
	
	public DecodedBasicOp(BasicOpcode op, Context context, int index) {
		super(op, context, index);
		
		this.pops = op.getPops();
		this.pushes = op.getPushes();
	}
	
	@Override
	public void simulate(Stack stack) {
		Stack.processBasicAlteration(stack, getPops(), getPushes());
	}
	
	public StackElementLength[] getPops() {
		return pops;
	}
	
	public StackElementLength[] getPushes() {
		return pushes;
	}
}