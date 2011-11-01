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
import bytecodeparser.analysis.decoders.DecodedLocalVariableOp;

/**
 * An opcode concerning a local variable (read, write).
 * @author Stephane Godbillon
 *
 */
public class LocalVariableOpcode extends Op {
	private final int base;
	private final boolean load;
	
	public LocalVariableOpcode(int code, boolean load) {
		this(code, code, load);
	}
	public LocalVariableOpcode(int code, int base, boolean load) {
		super(code);
		this.base = base;
		this.load = load;
	}
	
	@Override
	public DecodedLocalVariableOp decode(Context context, int index) {
		return new DecodedLocalVariableOp(this, context, index, false);
	}
	
	public DecodedLocalVariableOp decodeWide(Context context, int index) {
		return new DecodedLocalVariableOp(this, context, index, true);
	}
	
	public int getBaseOpcode() {
		return base;
	}
	
	/**
	 * States if this opcode is a read (load -> true) or a write (store -> false).
	 * @return
	 */
	public boolean isLoad() {
		return load;
	}
	
	@Override
	public String toString() {
		return "LocalVariableOp: " + getName();
	}
}