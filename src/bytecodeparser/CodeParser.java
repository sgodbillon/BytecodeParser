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
package bytecodeparser;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import bytecodeparser.analysis.Opcodes;
import bytecodeparser.analysis.opcodes.Op;

/**
 * A basic bytecode parser.
 * @author Stephane Godbillon
 *
 */
public class CodeParser {
	/**
	 * The matching context.
	 */
	public final Context context;
	private boolean stop = false;
	
	public CodeParser(CtBehavior behavior) {
		this.context = new Context(behavior);
	}
	/**
	 * Parses the bytecode with the given OpHandler.
	 * @param opHandler
	 * @throws BadBytecode
	 * @see {@link OpHandler}
	 */
	public void parse(OpHandler opHandler) throws BadBytecode {
		while(context.iterator.hasNext()) {
			if(stop)
				break;
			int index = context.iterator.next();
            Op opcode = Opcodes.OPCODES.get(context.iterator.byteAt(index));
            if(opcode == null) continue;
            Op op = opcode.init(context, index);
			opHandler.handle(op, index);
		}
	}
	
	/**
	 * Moves the code iterator to the given index.
	 * @param index
	 */
	public void move(int index) {
		stop = false;
		context.iterator.move(index);
	}
	
	/**
	 * Looks the next frame's index.
	 * @return
	 */
	public int nextIndex() {
		return context.iterator.lookAhead();
	}
	
	/**
	 * Resets the code iterator at the beginning of the bytecode.
	 */
	public void begin() {
		stop = false;
		context.iterator.begin();
	}
	
	/**
	 * Stops the parsing.
	 */
	public void stop() {
		stop = true;
	}
}
