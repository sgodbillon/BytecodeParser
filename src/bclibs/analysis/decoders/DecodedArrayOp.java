package bclibs.analysis.decoders;

import java.util.Arrays;

import bclibs.analysis.Context;
import bclibs.analysis.opcodes.ArrayOpcode;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.StackElement;
import bclibs.analysis.stack.TrackableArray;
import bclibs.analysis.stack.Constant.IntegerConstant;
import bclibs.analysis.stack.Stack.StackElementLength;

public class DecodedArrayOp extends DecodedBasicOp {
	public DecodedArrayOp(ArrayOpcode op, Context context, int index) {
		super(op, context, index);
	}
	@Override
	public void simulate(Stack stack) {
		if(!this.op.as(ArrayOpcode.class).isLoad) {
			StackElementLength[] pops = Arrays.copyOf(getPops(), getPops().length - 1);
			StackElement subject = stack.getFromTop(StackElementLength.add(pops));
			//System.out.println("Array: subject is " + subject);
			if(subject instanceof TrackableArray) {
				TrackableArray array = (TrackableArray) subject;
				//System.out.println("subject is a trackable array ! get from top " + StackElementLength.add(array.componentLength));
				StackElement i = stack.getFromTop(StackElementLength.add(array.componentLength));
				if(i instanceof IntegerConstant) {
					StackElement value = stack.peek(array.componentLength);
					//System.out.println("ok, put value "+value+" at index " + i);
					array.set(((IntegerConstant) i).getValue(), value);
				} else {
					//System.out.println("NOK!!, index " + i + " is not IntegerConstant!");
					array.isDirty = true;
				}
			}
		}
		super.simulate(stack);
	}
}
