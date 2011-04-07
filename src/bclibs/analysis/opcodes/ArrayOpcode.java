package bclibs.analysis.opcodes;

import java.util.Arrays;


import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import static bclibs.analysis.stack.Stack.StackElementLength;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.StackElement;
import bclibs.analysis.stack.TrackableArray;
import bclibs.analysis.stack.Constant.IntegerConstant;

public class ArrayOpcode extends BasicOpcode {
	public final boolean isLoad;
	
	public ArrayOpcode(int code, boolean isLoad, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
		this.isLoad = isLoad;
	}

	@Override
	public void simulate(Stack stack, Context context, int index) {
		if(!isLoad) {
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
		super.simulate(stack, context, index);
	}
}
