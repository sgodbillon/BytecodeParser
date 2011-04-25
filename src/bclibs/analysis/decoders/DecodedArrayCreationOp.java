package bclibs.analysis.decoders;

import javassist.bytecode.Opcode;
import bclibs.analysis.Context;
import bclibs.analysis.opcodes.ArrayCreationOpcode;
import bclibs.analysis.stack.Array;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.StackElement;
import bclibs.analysis.stack.TrackableArray;
import bclibs.analysis.stack.Constant.IntegerConstant;
import bclibs.utils.Utils;

public class DecodedArrayCreationOp extends DecodedBasicOp {
	public final int dimensions;
	public final String signature;
	
	public DecodedArrayCreationOp(ArrayCreationOpcode op, Context context, int index) {
		super(op, context, index);
		if(op.getCode() == Opcode.MULTIANEWARRAY)
			dimensions = parameterValues[1];
		else dimensions = 1;
		if(op.getCode() == Opcode.NEWARRAY)
			signature = getSignatureForSingleDimensionArrayOfPrimitive(parameterValues[0]);
		else signature = Utils.getConstPool(context.behavior).getClassInfo(parameterValues[0]);
	}
	
	@Override
	public void simulate(Stack stack) {
		int size = -1;
		if(dimensions == 1) {
			StackElement se = stack.pop();
			if(se instanceof IntegerConstant) {
				IntegerConstant ic = (IntegerConstant) se;
				size = ic.getValue();
			}
		} else {
			for(int i = 0; i < dimensions; i++)
				stack.pop();
		}
		if(size > -1)
			stack.push(new TrackableArray(signature, size));
		else stack.push(new Array(signature));
	}
	
	private static String getSignatureForSingleDimensionArrayOfPrimitive(int type) {
		switch(type) {
			case 4: return "[Z";
			case 5: return "[C";
			case 6: return "[F";
			case 7: return "[D";
			case 8: return "[B";
			case 9: return "[S";
			case 10: return "[I";
			case 11: return "[J"; 
			default: throw new RuntimeException("unexpected primitive array type! '" + type + "'");
		}
	}
	
	@Override
	public String toString() {
		return "DecodedArrayCreationOp dimensions=" + dimensions + " signature=" + signature;
	}
}
