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

import javassist.bytecode.Opcode;
import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes.OpParameterType;
import bytecodeparser.analysis.decoders.DecodedBranchOp;
import bytecodeparser.analysis.stack.Stack.StackElementLength;

/**
 * An opcode that stands for a fork in the bytecode.
 * @author Stephane Godbillon
 *
 */
public class BranchOpCode extends BasicOpcode {
	public BranchOpCode(int code, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
	}
	public BranchOpCode(int code, StackElementLength[] pops, StackElementLength[] pushes, OpParameterType... opParameterTypes) {
		super(code, pops, pushes, opParameterTypes);
	}
	
	@Override
	public DecodedBranchOp decode(Context context, int index) {
		return new DecodedBranchOp(this, context, index);
	}
	
	/**
	 * States if this opcode is conditional fork or not.
	 */
	public boolean isConditional() {
		return code >= Opcode.IFEQ && code <= Opcode.IF_ACMPNE || code == Opcode.IFNULL || code == Opcode.IFNONNULL;
	}
}
