/**
 * 
 */
package bclibs.analysis.decoders;

import static bclibs.analysis.stack.Stack.StackElementLength;
import static bclibs.analysis.stack.Stack.StackElementLength.*;

import java.util.Collections;
import java.util.LinkedList;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import bclibs.LocalVariable;
import bclibs.LocalVariableType;
import bclibs.analysis.Context;
import bclibs.analysis.opcodes.MethodInvocationOpcode;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.StackElement;
import bclibs.analysis.stack.TOP;
import bclibs.analysis.stack.TrackableArray;
import bclibs.analysis.stack.ValueFromLocalVariable;
import bclibs.analysis.stack.StackAnalyzer.Frame;
import bclibs.analysis.stack.Whatever;
import bclibs.utils.Utils;

public class DecodedMethodInvocationOp extends DecodedOp {
	protected int nbParameters;
	protected String descriptor;
	protected CtClass[] parameterTypes;
	protected String declaringClassName;
	protected String name;
	
	protected StackElementLength[] pops;
	protected StackElementLength[] pushes;
	
	public DecodedMethodInvocationOp(MethodInvocationOpcode mop, Context context, int index) throws NotFoundException {
		super(mop, context, index);
		ConstPool constPool = Utils.getConstPool(context.behavior);
		boolean interfaceMethod = constPool.getTag(getMethodRefIndex()) == ConstPool.CONST_InterfaceMethodref;
		descriptor = interfaceMethod ? constPool.getInterfaceMethodrefType(getMethodRefIndex()) : constPool.getMethodrefType(getMethodRefIndex());
		name = interfaceMethod ? constPool.getInterfaceMethodrefName(getMethodRefIndex()) : constPool.getMethodrefName(getMethodRefIndex());
		declaringClassName = context.behavior.getDeclaringClass().getName();
		ClassPool cp = context.behavior.getDeclaringClass().getClassPool();
		parameterTypes = Descriptor.getParameterTypes(descriptor, cp);
		nbParameters = parameterTypes.length;
		StackElementLength[] pops = new StackElementLength[parameterTypes.length + (mop.isInstanceMethod() ? 1 : 0)];
		for(int i = parameterTypes.length - 1, j = 0; i >= 0; i--, j++) {
			CtClass ctClass = parameterTypes[i];
			if(ctClass.isPrimitive()) {
				char d = ((CtPrimitiveType) ctClass).getDescriptor();
				if(d == 'J' || d == 'D') {
					pops[j] = DOUBLE;
				} else {
					pops[j] = ONE;
				}
			}
		}
		if(mop.isInstanceMethod())
			pops[pops.length - 1] = ONE;
		this.pops = pops;
		CtClass returnType = Descriptor.getReturnType(descriptor, cp);
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
		pushes = returnTypeLength != null ? new StackElementLength[] { returnTypeLength } : new StackElementLength[0];
	}
	
	@Override
	public void simulate(Stack stack) {
		for(int i = 0; i < getPops().length; i++) {
			if(getPops()[i] == DOUBLE)
				stack.pop2();
			else stack.pop();
		}
		for(int i = 0; i < getPushes().length; i++) {
			if(getPushes()[i] == DOUBLE)
				stack.push2(new Whatever());
			else stack.push(new Whatever());
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
	public int getNbParameters() {
		return nbParameters;
	}
	public StackElementLength[] getPops() {
		return pops;
	}
	public StackElementLength[] getPushes() {
		return pushes;
	}
	
	public static MethodParam[] resolveParameters(Frame frame) {
		LinkedList<MethodParam> result = new LinkedList<MethodParam>();
		DecodedMethodInvocationOp decoded = (DecodedMethodInvocationOp) frame.decodedOp;
		int nbParams = decoded.getNbParameters();
		System.out.println("nbParams == " + nbParams + " for decoded " + decoded.name);
		if(nbParams > 0) {
			int stackIndex = 0;
			if(frame.stackBefore.stack.get(stackIndex) instanceof TrackableArray) {
				StackElement[] varargs = ((TrackableArray) frame.stackBefore.stack.get(0)).elements;
				nbParams = nbParams + varargs.length - 1;
				for(int i = 0; i < varargs.length; i++, nbParams--) {
					StackElement se = varargs[i];
					if(se instanceof TOP)
						se = varargs[++i];
					LocalVariable lv = getLocalVariableIfAvailable(se);
					if(lv != null) {
						result.add(new MethodParam(lv.name, lv.type));
					} else {
						result.add(new MethodParam(null, null));
					}
				}
				stackIndex++;
			}
			while(nbParams > 0) {
				StackElement se = frame.stackBefore.stack.get(stackIndex++);
				if(se instanceof TOP)
					se = frame.stackBefore.stack.get(stackIndex++);
				LocalVariable lv = getLocalVariableIfAvailable(se);
				if(lv != null) {
					result.add(new MethodParam(lv.name, lv.type));
				} else {
					result.add(new MethodParam(null, null));
				}
				nbParams--;
			}
		}
		Collections.reverse(result);
		return result.toArray(new MethodParam[0]);
	}
	
	public static String[] resolveParametersNames(Frame frame) {
		MethodParam[] params = resolveParameters(frame);
		String[] result = new String[params.length];
		for(int i = 0; i < params.length; i++)
			result[i] = params[i].name;
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
	}
}