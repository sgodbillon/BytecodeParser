/**
 * 
 */
package bclibs.analysis.decoders;

import static bclibs.analysis.stack.Stack.StackElementLength.DOUBLE;
import bclibs.LocalVariable;
import bclibs.analysis.Context;
import bclibs.analysis.opcodes.LocalVariableOpcode;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.ValueFromLocalVariable;

public class DecodedLocalVariableOp extends DecodedBasicOp {
	public final LocalVariable localVariable;
	public final boolean load;
	
	public DecodedLocalVariableOp(LocalVariableOpcode lvo, Context context, int index) {
		super(lvo, context, index);
		int slot;
		if(parameterTypes.length > 0)
			slot = parameterValues[0];
		else slot = lvo.getCode() - lvo.getBaseOpcode();
		this.load = lvo.isLoad();
		localVariable = LocalVariable.getLocalVariable(slot, index, context.localVariables);
	}
	
	@Override
	public void simulate(Stack stack) {
		ValueFromLocalVariable toPush = new ValueFromLocalVariable(localVariable);
		//System.out.println("load " + index + ":" + getName() + " " + toPush.localVariable + "? " + load);
		for(int i = 0; i < getPops().length; i++) {
			//System.out.println("pop");
			if(getPops()[i] == DOUBLE)
				stack.pop2();
			else stack.pop();
		}
		for(int i = 0; i < getPushes().length; i++) {
			//System.out.println("push");
			if(getPushes()[i] == DOUBLE)
				stack.push2(toPush);
			else stack.push(toPush);
		}
	}
}