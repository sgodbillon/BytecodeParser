package bclibs.analysis.decoders;

import javassist.bytecode.Opcode;
import bclibs.analysis.Context;
import bclibs.analysis.opcodes.ArrayCreationOpcode;
import bclibs.analysis.stack.Array;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.StackElement;
import bclibs.analysis.stack.TrackableArray;
import bclibs.analysis.stack.Constant.IntegerConstant;

public class DecodedArrayCreationOp extends DecodedBasicOp {
	public DecodedArrayCreationOp(ArrayCreationOpcode op, Context context, int index) {
		super(op, context, index);
	}
	
	@Override
	public void simulate(Stack stack) {
		if(this.op.as(ArrayCreationOpcode.class).code != Opcode.MULTIANEWARRAY) {
			//System.out.println("creation of new array");
			StackElement se = stack.pop();
			if(se instanceof IntegerConstant) {
				//System.out.print(" - trackable. ");
				IntegerConstant ic = (IntegerConstant) se;
				stack.push(new TrackableArray("yop--", ic.getValue()));
			} else {
				//System.out.print(" - NOT trackable. ");
				stack.push(new Array("yop"));
			}
		} else throw new RuntimeException("unsupported");
	}
}
