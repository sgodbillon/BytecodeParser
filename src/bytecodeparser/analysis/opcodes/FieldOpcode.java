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

import static bytecodeparser.analysis.Opcodes.OpParameterType.U2;
import bytecodeparser.Context;
import bytecodeparser.analysis.decoders.DecodedFieldOp;

/**
 * An opcode concerning a field (read, write).
 * @author Stephane Godbillon
 *
 */
public class FieldOpcode extends Op {
	private final DecodedFieldOp decodedOp;
	
	public FieldOpcode(int code) {
		this(code, null);
	}
	private FieldOpcode(int code, DecodedFieldOp decodedOp) {
		super(code, U2);
		this.decodedOp = decodedOp;
	}
	@Override
	public FieldOpcode init(Context context, int index) {
		return new FieldOpcode(code, decode(context, index));
	}
	@Override
	public DecodedFieldOp decode(Context context, int index) {
		if(decodedOp != null)
			return decodedOp;
		try {
			DecodedFieldOp decodedOp = new DecodedFieldOp(this, context, index);
			return decodedOp;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}