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
import bytecodeparser.analysis.decoders.DecodedLocalVariableOp;
import bytecodeparser.analysis.stack.Stack.StackElementLength;

public class LocalVariableOpcode extends BasicOpcode {
	private int base;
	private boolean load;
	public LocalVariableOpcode(int code, boolean load, OpParameterType... opParameterTypes) {
		this(code, code, load, new StackElementLength[0], new StackElementLength[0], opParameterTypes);
	}
	public LocalVariableOpcode(int code, int base, boolean load, OpParameterType... opParameterTypes) {
		this(code, base, load, new StackElementLength[0], new StackElementLength[0], opParameterTypes);
	}
	public LocalVariableOpcode(int code, int base, boolean load, StackElementLength[] pops, StackElementLength[] pushes, OpParameterType... opParameterTypes) {
		super(code, pops, pushes, opParameterTypes);
		this.base = base;
		this.load = load;
	}
	@Override
	public DecodedLocalVariableOp decode(Context context, int index) {
		return new DecodedLocalVariableOp(this, context, index);
	}
	
	public int getBaseOpcode() {
		return base;
	}
	
	public boolean isLoad() {
		return load;
	}
	
	@Override
	public String toString() {
		return "LocalVariableOp: " + getName();
	}
}