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
import javassist.bytecode.Opcode;
import bytecodeparser.Context;
import bytecodeparser.analysis.decoders.DecodedMethodInvocationOp;

/**
 * An opcode that stands for a method invocation.
 * @author Stephane Godbillon
 *
 */
public class MethodInvocationOpcode extends Op {
	public MethodInvocationOpcode(int code) {
		super(code, U2);
	}
	/**
	 * States if the invoked method is static or not.
	 */
	public boolean isInstanceMethod() {
		return code != Opcode.INVOKESTATIC;
	}
	@Override
	public DecodedMethodInvocationOp decode(Context context, int index) {
		try {
			return new DecodedMethodInvocationOp(this, context, index);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}