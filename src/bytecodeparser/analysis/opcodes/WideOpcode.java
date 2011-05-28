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
import bytecodeparser.analysis.decoders.DecodedWideOp;
import javassist.bytecode.Opcode;

public class WideOpcode extends Op {
	private final DecodedWideOp decodedWideOp;
	
	public WideOpcode() {
		this(null);
	}
	
	private WideOpcode(DecodedWideOp decodedWideOp) {
		super(Opcode.WIDE);
		this.decodedWideOp = decodedWideOp;
	}
	
	@Override
	public Op init(Context context, int index) {
		return new WideOpcode(new DecodedWideOp(this, context, index));
	}
	
	@Override
	public DecodedWideOp decode(Context context, int index) {
		if(decodedWideOp != null)
			return decodedWideOp;
		throw new RuntimeException("must be initialized before !");
	}
	
	public LocalVariableOpcode getWrappedLocalVariableOpcode() {
		if(decodedWideOp != null)
			return decodedWideOp.op.as(LocalVariableOpcode.class);
		throw new RuntimeException("must be initialized before !");
	}
	
	public DecodedLocalVariableOp getWrappedDecodedLocalVariableOp() {
		if(decodedWideOp != null)
			return decodedWideOp.wrappedDecodedLocalVariableOp;
		throw new RuntimeException("must be initialized before !");
	}
}
