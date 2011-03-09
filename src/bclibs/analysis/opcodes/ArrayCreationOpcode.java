/**
 * 
 */
package bclibs.analysis.opcodes;

import javassist.bytecode.Opcode;
import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.decoders.DecodedOp;
import bclibs.analysis.stack.Array;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.StackElement;
import bclibs.analysis.stack.TrackableArray;
import bclibs.analysis.stack.Constant.IntegerConstant;

public class ArrayCreationOpcode extends BasicOpcode {
	public ArrayCreationOpcode(int code, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
	}
	@Override
	public DecodedOp decode(Context context, int index) {
		return new DecodedOp(this, context, index);
	}
	@Override
	public void simulate(Stack stack, Context context, int index) {
		if(code != Opcode.MULTIANEWARRAY) {
			System.out.println("creation of new array");
			StackElement se = stack.pop();
			if(se instanceof IntegerConstant) {
				System.out.print(" - trackable. ");
				IntegerConstant ic = (IntegerConstant) se;
				stack.push(new TrackableArray("yop--", ic.getValue()));
			} else {
				System.out.print(" - NOT trackable. ");
				stack.push(new Array("yop"));
			}
		} else throw new RuntimeException("unsupported");
	}
}