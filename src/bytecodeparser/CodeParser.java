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

public class CodeParser {
	public final Context context;
	private boolean stop = false;
	
	public CodeParser(CtBehavior behavior) {
		this.context = new Context(behavior, behavior.getMethodInfo().getCodeAttribute().iterator());
	}
	public void parse(OpHandler opHandler) throws BadBytecode {
		while(context.iterator.hasNext()) {
			if(stop)
				break;
			int index = context.iterator.next();
			Op op = Opcodes.OPCODES.get(context.iterator.byteAt(index)).init(context, index);
			opHandler.handle(op, index);
		}
	}
	
	public void move(int index) {
		stop = false;
		context.iterator.move(index);
	}
	
	public int nextIndex() {
		return context.iterator.lookAhead();
	}
	
	public void begin() {
		stop = false;
		context.iterator.begin();
	}
	
	public void stop() {
		stop = true;
	}
}