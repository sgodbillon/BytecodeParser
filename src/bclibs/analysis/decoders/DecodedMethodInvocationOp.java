/**
 * 
 */
package bclibs.analysis.decoders;

import static bclibs.analysis.stack.Stack.StackElementLength;
import static bclibs.analysis.stack.Stack.StackElementLength.*;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import bclibs.analysis.Context;
import bclibs.analysis.opcodes.MethodInvocationOpcode;
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
}