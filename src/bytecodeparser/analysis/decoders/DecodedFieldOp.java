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

import static bytecodeparser.analysis.stack.Stack.StackElementLength.DOUBLE;
import static bytecodeparser.analysis.stack.Stack.StackElementLength.ONE;
import javassist.bytecode.Descriptor;
import javassist.bytecode.Opcode;
import bytecodeparser.Context;
import bytecodeparser.analysis.opcodes.FieldOpcode;
import bytecodeparser.analysis.stack.Stack;
import bytecodeparser.analysis.stack.Stack.StackElementLength;

/**
 * A decoded field operation op.
 * @author Stephane Godbillon
 *
 */
public class DecodedFieldOp extends DecodedOp {
	protected String descriptor;
	protected boolean load;
	protected boolean isStatic;
	protected StackElementLength stackElementLength;
	
	public DecodedFieldOp(FieldOpcode fo, Context context, int index) {
		super(fo, context, index);
		String descriptor = context.behavior.getMethodInfo().getConstPool().getFieldrefType(getMethodRefIndex());
		StackElementLength sel = ONE;
		if(Descriptor.dataSize(descriptor) == 2)
			sel = DOUBLE;
		this.stackElementLength = sel;
		this.descriptor = descriptor;
		this.load = fo.getCode() == Opcode.GETFIELD || fo.getCode() == Opcode.GETSTATIC;
		this.isStatic = fo.getCode() == Opcode.GETSTATIC ||fo.getCode() == Opcode.PUTSTATIC;
	}
	
	@Override
	public void simulate(Stack stack) {
		Stack.processBasicAlteration(stack, getPops(), getPushes());
	}
	
	/**
	 * @return the methodRef index of this op.
	 */
	public int getMethodRefIndex() {
		return parameterValues[0];
	}
	
	/**
	 * @return the descriptor of the field.
	 */
	public String getDescriptor() {
		return descriptor;
	}
	
	/**
	 * Pops needed by this decoded op.
	 */
	public StackElementLength[] getPops() {
		if(isStatic && !load)
			return new StackElementLength[] { stackElementLength };
		else if(isStatic && load)
			return new StackElementLength[0];
		else if(!isStatic && !load)
			return new StackElementLength[] { stackElementLength, ONE };
		else return new StackElementLength[] { ONE };
	}
	
	/**
	 * Pushes needed by this decoded op.
	 */
	public StackElementLength[] getPushes() {
		if(load)
			return new StackElementLength[] { stackElementLength };
		return new StackElementLength[0];
	}
	
	/**
	 * States if this op is a read or write operation.
	 */
	public boolean isRead() {
		return load;
	}
}