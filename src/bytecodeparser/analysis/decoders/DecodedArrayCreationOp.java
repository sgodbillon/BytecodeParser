/*
 *  Copyright (C) 2011 Stephane Godbillon
 *  
 *  This file is part of BytecodeParser. See the README file in the root
 *  directory of this project.
 *
 *  BytecodeParser is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  BytecodeParser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with BytecodeParser.  If not, see <http://www.gnu.org/licenses/>.
 */
package bytecodeparser.analysis.decoders;

import javassist.bytecode.Opcode;
import bytecodeparser.Context;
import bytecodeparser.analysis.opcodes.ArrayCreationOpcode;
import bytecodeparser.analysis.stack.Array;
import bytecodeparser.analysis.stack.Constant.IntegerConstant;
import bytecodeparser.analysis.stack.Stack;
import bytecodeparser.analysis.stack.StackElement;
import bytecodeparser.analysis.stack.TrackableArray;
import bytecodeparser.utils.Utils;

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
