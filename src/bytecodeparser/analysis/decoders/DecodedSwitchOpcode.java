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

import static bytecodeparser.analysis.Opcodes.OpParameterType.S4;

import java.util.Arrays;

import javassist.bytecode.Opcode;
import bytecodeparser.Context;
import bytecodeparser.analysis.opcodes.SwitchOpcode;
import bytecodeparser.analysis.stack.Stack;

public class DecodedSwitchOpcode extends DecodedOp {
	public final int defaultOffset;
	public final int padding;
	public final int low;
	public final int high;
	public final int nbEntries;
	public final int[] offsets;
	
	public DecodedSwitchOpcode(SwitchOpcode op, Context context, int index) {
		super(op, context, index);
		int _padding = (index + 1) % 4;
		padding = _padding == 0 ? 0 : 4 - _padding;
		int nextIndex = index + padding + 1;
		defaultOffset = decodeValueAt(S4, context.iterator, nextIndex) + index;
		nextIndex += S4.size;
		if(op.code == Opcode.TABLESWITCH) {
			low = decodeValueAt(S4, context.iterator, nextIndex);
			nextIndex += S4.size;
			high = decodeValueAt(S4, context.iterator, nextIndex);
			nextIndex += S4.size;
			nbEntries = high - low + 1;
			offsets = new int[nbEntries];
			for(int i = 0; i < offsets.length; i++) {
				offsets[i] = decodeValueAt(S4, context.iterator, nextIndex) + index;
				nextIndex += S4.size;
			}
		} else { // LOOKUPSWITCH
			low = high = -1;
			nbEntries = decodeValueAt(S4, context.iterator, nextIndex);
			nextIndex += S4.size;
			offsets = new int[nbEntries];
			for(int i = 0; i < offsets.length; i++) {
				nextIndex += S4.size;
				offsets[i] = decodeValueAt(S4, context.iterator, nextIndex) + index;
				nextIndex += S4.size;
			}
		}
	}
	
	@Override
	public void simulate(Stack stack) {
		stack.pop();
	}
	
	@Override
	public String toString() {
		return "DecodedSwitchOpcode [" + op.getName() + "] : redirects -> " + Arrays.toString(offsets) + ", default " + defaultOffset + "(high=" + high + ", low=" + low + ")";
	}
}