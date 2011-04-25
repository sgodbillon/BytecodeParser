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
package test;

import java.util.Arrays;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import bytecodeparser.Context;
import bytecodeparser.analysis.decoders.DecodedMethodInvocationOp;
import bytecodeparser.analysis.stack.StackAnalyzer.Frame;
import bytecodeparser.analysis.stack.StackElement;
import bytecodeparser.analysis.stack.TOP;
import bytecodeparser.analysis.stack.TrackableArray;
import bytecodeparser.analysis.stack.ValueFromLocalVariable;

public class CommonTests {
	public static CtClass getCtClass(String clazz) {
		ClassPool cp = ClassPool.getDefault();
		try {
			return cp.get(clazz);
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static CtMethod getMethod(CtClass ctClass, String name) {
		for(CtMethod ctMethod : ctClass.getMethods()) {
			if(ctMethod.getName().equals(name)) {
				return ctMethod;
			}
		}
		throw new RuntimeException("method '" + name + "' not found in class " + ctClass);
	}
	
	public static void assertDeepEquals(String[] array1, String[] array2) {
		if(array1.length != array2.length)
			throw new RuntimeException(Arrays.toString(array1) + " does not equal " + Arrays.toString(array2) + " !");
		for(int i = 0; i < array1.length; i++)
			if(array1[i] != array2[i] && (array1[i] == null || !array1[i].equals(array2[i])) )
				throw new RuntimeException(Arrays.toString(array1) + " does not equal " + Arrays.toString(array2) + " !");
	}
	
	// old
	
	private static String getLocalVariableName(StackElement se) {
		if(se instanceof ValueFromLocalVariable) {
			ValueFromLocalVariable v = (ValueFromLocalVariable) se;
			if(v.localVariable != null)
				return v.localVariable.name;
		}
		return null;
	}
	
	public static String getMethodNamedSignature(Context context, Frame frame) {
		DecodedMethodInvocationOp decoded = (DecodedMethodInvocationOp) frame.decodedOp;
		String name = decoded.getName();
		String[] names = DecodedMethodInvocationOp.resolveParametersNames(frame, true);
		StringBuffer sb = new StringBuffer();
		if(names.length > 0) {
			sb.append(names[0]);
			for(int i = 1; i < names.length; i++) {
				sb.append(", ").append(names[i]);
			}
		}
		sb.insert(0, "(").insert(0, name).append(")");
		return sb.toString();
	}
	
	public static String[] methodInvocationNames(Frame frame) {
		DecodedMethodInvocationOp decoded = (DecodedMethodInvocationOp) frame.decodedOp;
		int nbParams = decoded.getNbParameters();
		String[] result = new String[nbParams];
		if(nbParams > 0) {
			int stackIndex = 0;
			if(frame.stackBefore.stack.get(stackIndex) instanceof TrackableArray) {
				StackElement[] varargs = ((TrackableArray) frame.stackBefore.stack.get(0)).elements;
				nbParams = nbParams + varargs.length - 1;
				result = new String[nbParams];
				for(int i = 0; i < varargs.length; i++, nbParams--) {
					result[nbParams - 1] = getLocalVariableName(varargs[i]) + "(" + varargs[i] + ")";
				}
				stackIndex++;
			}
			while(nbParams > 0) {
				StackElement se = frame.stackBefore.stack.get(stackIndex++);
				if(se instanceof TOP)
					se = frame.stackBefore.stack.get(stackIndex++);
				result[nbParams - 1] = getLocalVariableName(se) + "(" + se + ")";
				nbParams--;
			}
		}
		return result;
	}
}
