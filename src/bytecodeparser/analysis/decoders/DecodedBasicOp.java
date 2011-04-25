/**
 * 
 */
package bytecodeparser.analysis.decoders;

import bytecodeparser.Context;
import bytecodeparser.analysis.opcodes.BasicOpcode;
import bytecodeparser.analysis.stack.Stack;
import bytecodeparser.analysis.stack.Stack.StackElementLength;

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