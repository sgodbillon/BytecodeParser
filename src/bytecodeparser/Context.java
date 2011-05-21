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

import java.util.Map;

import javassist.CtBehavior;
import javassist.NotFoundException;
import javassist.bytecode.ExceptionTable;
import bytecodeparser.analysis.LocalVariable;

public class Context {
	public final CtBehavior behavior;
	public final MultiMarkerCodeIterator iterator;
	public final Map<Integer, LocalVariable> localVariables;
	public final int[] exceptionHandlers;
	
	public Context(CtBehavior behavior, MultiMarkerCodeIterator iterator, Map<Integer, LocalVariable> localVariables) {
		this.behavior = behavior;
		this.iterator = iterator;
		this.localVariables = localVariables;
		ExceptionTable exceptionTable = this.behavior.getMethodInfo().getCodeAttribute().getExceptionTable();
		
		if(exceptionTable != null) {
			this.exceptionHandlers = new int[exceptionTable.size()];
			for(int i = 0; i < exceptionTable.size(); i++) {
				exceptionHandlers[i] = exceptionTable.handlerPc(i);
			}
		} else {
			this.exceptionHandlers = new int[0];
		}
	}
	
	public Context(CtBehavior behavior, MultiMarkerCodeIterator iterator) {
		this(behavior, iterator, findLocalVariables(behavior));
	}
	
	public Context(CtBehavior behavior) {
		this(behavior, new MultiMarkerCodeIterator(behavior.getMethodInfo().getCodeAttribute()), findLocalVariables(behavior));
	}
	
	public boolean isStartOfExceptionHandler(int index) {
		for(int i = 0; i < exceptionHandlers.length; i++)
			if(exceptionHandlers[i] == index)
				return true;
		return false;
	}
	
	private static Map<Integer, LocalVariable> findLocalVariables(CtBehavior behavior) {
		try {
			return LocalVariable.findVariables(behavior);
		} catch (NotFoundException e) {
			throw new RuntimeException("Error while retrieving the behavior's local variables!", e);
		}
	}
}
