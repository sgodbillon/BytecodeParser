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
package bytecodeparser.utils;

import javassist.CtBehavior;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;

public class Utils {
	/**
	 * Gets the LocalVariableAttribute of the behavior.
	 * @param behavior
	 * @throws NullPointerException if this behavior has no code attribute.
	 * @return the LocalVariableAttribute or null if none.
	 */
	public static LocalVariableAttribute getLocalVariableAttribute(CtBehavior behavior) {
		return (LocalVariableAttribute) behavior.getMethodInfo().getCodeAttribute().getAttribute("LocalVariableTable");
	}
	
	/**
	 * Gets the LineNumberAttribute of the behavior.
	 * @param behavior
	 * @throws NullPointerException if this behavior has no code attribute.
	 * @return the LineNumberAttribute or null if none.
	 */
	public static LineNumberAttribute getLineNumberAttribute(CtBehavior behavior) {
		return (LineNumberAttribute) behavior.getMethodInfo().getCodeAttribute().getAttribute("LineNumberTable");
	}
	
	/**
	 * Gets the constpool attribute of this behavior.
	 * @param behavior
	 * @return the constpool attribute of this behavior or null if none.
	 */
	public static ConstPool getConstPool(CtBehavior behavior) {
		return behavior.getMethodInfo().getConstPool();
	}
	
	public static void debugExceptionTable(ExceptionTable et, ConstPool cp) {
		for(int i = 0; i < et.size(); i++) {
			System.out.println(et.startPc(i) + " -> " + et.endPc(i) + " : " + et.handlerPc(i) + ", type=" + (et.catchType(i) != 0 ? cp.getClassInfo(et.catchType(i)) : "any"));
		}
	}
}
