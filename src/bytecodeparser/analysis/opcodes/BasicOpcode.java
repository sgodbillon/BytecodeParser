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
package bytecodeparser.analysis.opcodes;

import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes.OpParameterType;
import bytecodeparser.analysis.decoders.DecodedBasicOp;
import bytecodeparser.analysis.stack.Stack.StackElementLength;

/**
 * A basic op that performs classic push/pops on the stack.
 * @author Stephane Godbillon
 *
 */
public class BasicOpcode extends Op {
	protected StackElementLength[] pops, pushes;
	public BasicOpcode(int code, OpParameterType... opParameterTypes) {
		this(code, new StackElementLength[0], new StackElementLength[0], opParameterTypes);
	}
	public BasicOpcode(int code, StackElementLength[] pops, StackElementLength[] pushes, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
		this.pops = pops;
		this.pushes = pushes;
	}
	/**
	 * @return the pops of this opcode.
	 */
	public StackElementLength[] getPops() {
		return pops;
	}
	/**
	 * @return the pushes of this opcode.
	 */
	public StackElementLength[] getPushes() {
		return pushes;
	}
	@Override
	public DecodedBasicOp decode(Context context, int index) {
		return new DecodedBasicOp(this, context, index);
	}
	@Override
	public String toString() {
		return "BasicOp: " + getName();
	}
	/**
	 * Internal use only.
	 */
	public BasicOpcode setPops(StackElementLength... pops) {
		if(pops != null)
			this.pops = pops;
		return this;
	}
	/**
	 * Internal use only.
	 */
	public BasicOpcode setPushes(StackElementLength... pushes) {
		if(pushes != null)
			this.pushes = pushes;
		return this;
	}
}