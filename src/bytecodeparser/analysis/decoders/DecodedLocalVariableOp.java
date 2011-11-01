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

import javassist.bytecode.Opcode;

import org.apache.log4j.Logger;

import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes.OpParameterType;
import static bytecodeparser.analysis.Opcodes.OpParameterType.*;
import bytecodeparser.analysis.LocalVariable;
import bytecodeparser.analysis.opcodes.LocalVariableOpcode;
import bytecodeparser.analysis.stack.Stack;
import bytecodeparser.analysis.stack.StackElement;
import bytecodeparser.analysis.stack.ValueFromLocalVariable;

/**
 * A decoded local variable operation op.
 * @author Stephane Godbillon
 *
 */
public class DecodedLocalVariableOp extends DecodedOp {
	private static final Logger LOGGER = Logger.getLogger(DecodedLocalVariableOp.class);
	/**
	 * The matching local variable.
	 */
	public final LocalVariable localVariable;
	/**
	 * States if this op is a read (load) or write (store).
	 */
	public final boolean load;
	/**
	 * States if the matching localVariable is one- or two-words long.
	 */
	public final boolean doubleLength;
	/**
	 * States if the op is wide (nb localvars > 256)
	 */
	public final boolean isWide;
	
	public DecodedLocalVariableOp(LocalVariableOpcode lvo, Context context, int index, boolean isWide) {
		super(lvo, context, index, guessTypes(lvo, context, index, isWide), decodeValues(guessTypes(lvo, context, index, isWide), context.iterator, index + (isWide ? 1 : 0)));
		this.isWide = isWide;
		int slot;
		if(parameterTypes.length > 0)
			slot = parameterValues[0];
		else slot = lvo.getCode() - lvo.getBaseOpcode();
		localVariable = LocalVariable.getLocalVariable(slot, index, context.localVariables);
		int base = lvo.getBaseOpcode();
		this.load = lvo.isLoad();
		doubleLength = base == Opcode.DLOAD || base == Opcode.DLOAD_0 || base == Opcode.LLOAD || base == Opcode.LLOAD_0 || base == Opcode.DSTORE || base == Opcode.DSTORE_0 || base == Opcode.LSTORE || base == Opcode.LSTORE_0;
	}
	
	@Override
	public void simulate(Stack stack) {
		if(op.code != Opcode.IINC) {
			ValueFromLocalVariable toPush = new ValueFromLocalVariable(localVariable);
			if(!load) {
				StackElement poppedSe;
				if(doubleLength)
					poppedSe = stack.pop2();
				else poppedSe = stack.pop();
				/* when a name is null while LocalVariableTable is present, it is likely that this class has been
				 * previously enhanced and this local variable is just a variable proxy, so grab the original local
				 * variable and consider its name
				 */
				if(poppedSe instanceof ValueFromLocalVariable && (localVariable == null || localVariable.name == null)) {
					LOGGER.debug("ATTENTION ************** variable proxy for lv = '" + ((ValueFromLocalVariable) poppedSe).localVariable + "'");
					toPush = new ValueFromLocalVariable(((ValueFromLocalVariable) poppedSe).localVariable);
				}
			} else {
				if(doubleLength)
					stack.push2(toPush);
				else stack.push(toPush);
			}
		}
	}
	
	private static OpParameterType[] guessTypes(LocalVariableOpcode lvo, Context context, int index, boolean isWide) {
		int code = lvo.code;
		if(code != Opcode.ALOAD &&
				code != Opcode.ASTORE &&
				code != Opcode.DLOAD &&
				code != Opcode.DSTORE &&
				code != Opcode.FLOAD &&
				code != Opcode.FSTORE &&
				code != Opcode.ILOAD &&
				code != Opcode.ISTORE &&
				code != Opcode.LLOAD &&
				code != Opcode.LSTORE &&
				code != Opcode.IINC)
			return new OpParameterType[0];
		boolean isIINC = lvo.code == Opcode.IINC;
		OpParameterType[] result = new OpParameterType[isIINC ? 2 : 1];
		result[0] = isWide ? U2 : U1;
		if(isIINC)
			result[1] = isWide ? S2 : S1;
		return result;
	}
}