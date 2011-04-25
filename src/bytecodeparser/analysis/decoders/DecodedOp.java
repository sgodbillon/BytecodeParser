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

import javassist.bytecode.CodeIterator;
import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes.OpParameterType;
import bytecodeparser.analysis.opcodes.Op;
import bytecodeparser.analysis.stack.Stack;

public abstract class DecodedOp {
	public final Context context;
	public final int index;
	public final Op op;
	
	public final OpParameterType[] parameterTypes;
	public final int[] parameterValues;
	
	public DecodedOp(Op op, Context context, int index) {
		this.context = context;
		this.index = index;
		this.op = op;
		this.parameterTypes = op.getParameterTypes();
		this.parameterValues = decodeValues(context.iterator, index);
	}
	
	protected final int[] decodeValues(CodeIterator iterator, int index) {
		int[] result = new int[parameterTypes.length];
		int nextValIndex = index + 1;
		for(int i = 0; i < parameterTypes.length; i++) {
			OpParameterType type = parameterTypes[i];
			result[i] = decodeValueAt(type, iterator, nextValIndex);
			nextValIndex += type.size;
		}
		return result;
	}
	
	protected final int decodeValueAt(OpParameterType type, CodeIterator iterator, int index) {
		switch(type) {
			case S1:
				return iterator.byteAt(index);
			case S2:
				return iterator.s16bitAt(index);
			case S4:
				return iterator.s32bitAt(index);
			case U1:
				return iterator.byteAt(index);
			case U2:
				return iterator.u16bitAt(index);
			case U4:
			default:
				throw new RuntimeException("unsupported");
			
		}
	}
	
	public abstract void simulate(Stack stack);
}
