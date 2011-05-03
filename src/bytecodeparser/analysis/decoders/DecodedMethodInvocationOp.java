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

import static bytecodeparser.analysis.stack.Stack.StackElementLength.DOUBLE;
import static bytecodeparser.analysis.stack.Stack.StackElementLength.ONE;

import java.util.Arrays;
import java.util.Iterator;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import bytecodeparser.Context;
import bytecodeparser.analysis.LocalVariable;
import bytecodeparser.analysis.LocalVariableType;
import bytecodeparser.analysis.opcodes.MethodInvocationOpcode;
import bytecodeparser.analysis.stack.Stack;
import bytecodeparser.analysis.stack.Stack.StackElementLength;
import bytecodeparser.analysis.stack.StackAnalyzer.Frame;
import bytecodeparser.analysis.stack.StackElement;
import bytecodeparser.analysis.stack.TOP;
import bytecodeparser.analysis.stack.TrackableArray;
import bytecodeparser.analysis.stack.ValueFromLocalVariable;
import bytecodeparser.analysis.stack.Whatever;
import bytecodeparser.utils.Utils;

public class DecodedMethodInvocationOp extends DecodedOp {
	protected int nbParameters;
	protected String descriptor;
	protected CtClass returnType;
	protected CtClass[] parameterTypes;
	protected String declaringClassName;
	protected String name;
	
	protected StackElementLength[] pops;
	protected StackElementLength returnTypeLength;
	
	public DecodedMethodInvocationOp(MethodInvocationOpcode mop, Context context, int index) throws NotFoundException {
		super(mop, context, index);
		ConstPool constPool = Utils.getConstPool(context.behavior);
		boolean interfaceMethod = constPool.getTag(getMethodRefIndex()) == ConstPool.CONST_InterfaceMethodref;
		descriptor = interfaceMethod ? constPool.getInterfaceMethodrefType(getMethodRefIndex()) : constPool.getMethodrefType(getMethodRefIndex());
		name = interfaceMethod ? constPool.getInterfaceMethodrefName(getMethodRefIndex()) : constPool.getMethodrefName(getMethodRefIndex());
		declaringClassName = interfaceMethod ? constPool.getInterfaceMethodrefClassName(getMethodRefIndex()) : constPool.getMethodrefClassName(getMethodRefIndex());
		ClassPool cp = context.behavior.getDeclaringClass().getClassPool();
		parameterTypes = Descriptor.getParameterTypes(descriptor, cp);
		nbParameters = parameterTypes.length;
		StackElementLength[] pops = new StackElementLength[parameterTypes.length];
		for(int i = parameterTypes.length - 1, j = 0; i >= 0; i--, j++) {
			pops[j] = ONE;
			CtClass ctClass = parameterTypes[i];
			if(ctClass.isPrimitive()) {
				char d = ((CtPrimitiveType) ctClass).getDescriptor();
				if(d == 'J' || d == 'D') {
					pops[j] = DOUBLE;
				}
			}
		}
		this.pops = pops;
		returnType = Descriptor.getReturnType(descriptor, cp);
		StackElementLength returnTypeLength = ONE;
		if(returnType.isPrimitive()) {
			char d = ((CtPrimitiveType) returnType).getDescriptor();
			if(d == 'V') {
				returnTypeLength = null;
			}
			if(d == 'J' || d == 'D') {
				returnTypeLength = DOUBLE;
			}
		}
		this.returnTypeLength = returnTypeLength != null ? returnTypeLength : null;
	}
	
	@Override
	public void simulate(Stack stack) {
		boolean isAutoboxing = isAutoboxing();
		StackElement se = null;
		for(int i = 0; i < pops.length; i++) {
			if(pops[i] == DOUBLE)
				se = stack.pop2();
			else se = stack.pop();
		}
		if(op.as(MethodInvocationOpcode.class).isInstanceMethod())
			stack.pop();
		if(returnTypeLength != null) {
			if(returnTypeLength == DOUBLE)
				stack.push2(isAutoboxing ? se.copy() : new Whatever());
			else stack.push(isAutoboxing ? se.copy() : new Whatever());
		}
	}
	
	public int getMethodRefIndex() {
		return parameterValues[0];
	}
	
	public String getDescriptor() {
		return descriptor;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDeclaringClassName() {
		return declaringClassName;
	}
	
	public CtClass[] getParameterTypes() {
		return parameterTypes;
	}
	
	public CtClass getReturnType() {
		return returnType;
	}
	
	public int getNbParameters() {
		return nbParameters;
	}
	
	public StackElementLength[] getPops() {
		return pops;
	}
	
	public StackElementLength[] getPushes() {
		return new StackElementLength[] { this.returnTypeLength };
	}
	
	@Override
	public String toString() {
		return "decodedMethodInvocation " + (isAutoboxing() ? "[ISAUTOBOXING]" : "") + " class=" + declaringClassName + ", descriptor=" + descriptor + ", name=" + name;
	}
	
	private boolean isAutoboxing() {
		return name.equals("valueOf") && ("java.lang.Boolean".equals(this.declaringClassName) && descriptor.equals("(Z)Ljava/lang/Boolean;") ||
				"java.lang.Byte".equals(declaringClassName) && descriptor.equals("(B)Ljava/lang/Byte;") ||
				"java.lang.Character".equals(declaringClassName) && descriptor.equals("(C)L/java/lang/Character;") ||
				"java.lang.Short".equals(declaringClassName) && descriptor.equals("(S)Ljava/lang/Short;") ||
				"java.lang.Integer".equals(declaringClassName) && descriptor.equals("(I)Ljava/lang/Integer;") ||
				"java.lang.Long".equals(declaringClassName) && descriptor.equals("(J)Ljava/lang/Long;") ||
				"java.lang.Float".equals(declaringClassName) && descriptor.equals("(F)Ljava/lang/Float;") ||
				"java.lang.Double".equals(declaringClassName) && descriptor.equals("(D)Ljava/lang/Double;"));
	}
	
	public static MethodParams resolveParameters(Frame frame) {
		DecodedMethodInvocationOp decoded = (DecodedMethodInvocationOp) frame.decodedOp;
		int nbParams = decoded.getNbParameters();
		MethodParam[] varargs = null;
		MethodParam[] params = resolveParameters(frame.stackBefore.stack, nbParams, false);
		if(nbParams > 0) {
			int stackIndex = 0;
			if(frame.stackBefore.stack.get(stackIndex) instanceof TOP)
				stackIndex = 1;
			if(frame.stackBefore.stack.get(stackIndex) instanceof TrackableArray) {
				TrackableArray trackableArray = (TrackableArray) frame.stackBefore.stack.get(stackIndex);
				varargs = resolveParameters(Arrays.asList(trackableArray.elements), trackableArray.elements.length, true);
			}
		}
		if(decoded.op.as(MethodInvocationOpcode.class).isInstanceMethod()) {
			StackElement subjectSE = frame.stackBefore.stack.get(StackElementLength.add(decoded.pops));
			LocalVariable lv = getLocalVariableIfAvailable(subjectSE);
			return new MethodParams(lv != null ? new MethodParam(lv.name, lv.type) : new MethodParam(null, null), params, varargs);
		}
		return new MethodParams(null, params, varargs);
	}
	
	public static String[] resolveParametersNames(Frame frame, boolean varargs) {
		MethodParam[] params = varargs ? resolveParameters(frame).merge() : resolveParameters(frame).params;
		String[] result = new String[params.length];
		for(int i = 0; i < result.length; i++)
			result[i] = params[i].name;
		return result;
	}
	
	private static MethodParam[] resolveParameters(final Iterable<StackElement> stack, final int elements, boolean reverse) {
		MethodParam[] result = new MethodParam[elements];
		Iterator<StackElement> it = stack.iterator();
		int i = 0;
		while(it.hasNext() && i < elements) {
			StackElement se = it.next();
			if(se instanceof TOP)
				se = it.next();
			LocalVariable lv = getLocalVariableIfAvailable(se);
			if(lv != null) {
				result[reverse ? i : elements - i - 1] = new MethodParam(lv.name, lv.type);
			} else {
				result[reverse ? i : elements - i - 1] = new MethodParam(null, null);
			}
			i++;
		}
		return result;
	}
	
	private static LocalVariable getLocalVariableIfAvailable(StackElement se) {
		if(se instanceof ValueFromLocalVariable) {
			ValueFromLocalVariable v = (ValueFromLocalVariable) se;
			return v.localVariable;
		}
		return null;
	}
	
	public static class MethodParam {
		public final String name;
		public final LocalVariableType type;
		
		public MethodParam(String name, LocalVariableType type) {
			this.name = name;
			this.type = type;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public static class MethodParams {
		public final MethodParam subject;
		public final MethodParam[] params;
		public final MethodParam[] varargs;
		
		public MethodParams(MethodParam subject, MethodParam[] params, MethodParam[] varargs) {
			this.subject = subject;
			this.params = params;
			this.varargs = varargs;
		}
		
		public MethodParam[] merge() {
			if(varargs == null)
				return Arrays.copyOf(params, params.length);
			MethodParam[] result = new MethodParam[params.length + varargs.length - 1];
			int i = 0;
			for(; i < params.length - 1; i++)
				result[i] = params[i];
			for(int j = 0; i < result.length; i++, j++)
				result[i] = varargs[j];
			return result;
		}
	}
}