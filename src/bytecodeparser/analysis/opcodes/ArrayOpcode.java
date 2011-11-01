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
import bytecodeparser.analysis.decoders.DecodedArrayOp;

/**
 * An opcode that is an operation on an array.
 * @author Stephane Godbillon
 *
 */
public class ArrayOpcode extends BasicOpcode {
	/**
	 * States if the operation is a read (load -> true) or a write (store -> false).
	 */
	public final boolean isLoad;
	
	public ArrayOpcode(int code, boolean isLoad, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
		this.isLoad = isLoad;
	}
	
	@Override
	public DecodedArrayOp decode(Context context, int index) {
		return new DecodedArrayOp(this, context, index);
	}
}
