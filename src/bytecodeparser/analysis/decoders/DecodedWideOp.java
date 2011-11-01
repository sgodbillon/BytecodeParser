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

import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes;
import bytecodeparser.analysis.opcodes.LocalVariableOpcode;
import bytecodeparser.analysis.opcodes.WideOpcode;
import bytecodeparser.analysis.stack.Stack;

/**
 * A decoded wide local variable operation op.
 * @author Stephane Godbillon
 *
 */
public class DecodedWideOp extends DecodedOp {
	/**
	 * The wrapped decoded local variable opcode.
	 */
	public final DecodedLocalVariableOp wrappedDecodedLocalVariableOp;
	
	public DecodedWideOp(WideOpcode wide, Context context, int index) {
		super(wide, context, index);
		wrappedDecodedLocalVariableOp = Opcodes.OPCODES.get(context.iterator.byteAt(index + 1)).as(LocalVariableOpcode.class).decodeWide(context, index);
	}
	
	@Override
	public void simulate(Stack stack) {
		wrappedDecodedLocalVariableOp.simulate(stack);
	}
}
