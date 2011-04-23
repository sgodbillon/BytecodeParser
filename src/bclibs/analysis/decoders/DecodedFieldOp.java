/**
 * 
 */
package bclibs.analysis.decoders;

import static bclibs.analysis.stack.Stack.StackElementLength;
import static bclibs.analysis.stack.Stack.StackElementLength.*;
import javassist.bytecode.Descriptor;
import javassist.bytecode.Opcode;
import bclibs.analysis.Context;
import bclibs.analysis.opcodes.FieldOpcode;
import bclibs.analysis.stack.Stack;

public class DecodedFieldOp extends DecodedOp {
	protected String descriptor;
	protected boolean load;
	protected boolean isStatic;
	protected StackElementLength stackElementLength;
	
	public DecodedFieldOp(FieldOpcode fo, Context context, int index) {
		super(fo, context, index);
		String descriptor = context.behavior.getMethodInfo().getConstPool().getFieldrefType(getMethodRefIndex());
		StackElementLength sel = ONE;
		if(Descriptor.dataSize(descriptor) == 2)
			sel = DOUBLE;
		this.stackElementLength = sel;
		this.descriptor = descriptor;
		this.load = fo.getCode() == Opcode.GETFIELD || fo.getCode() == Opcode.GETSTATIC;
		this.isStatic = fo.getCode() == Opcode.GETSTATIC ||fo.getCode() == Opcode.PUTSTATIC;
	}
	
	@Override
	public void simulate(Stack stack) {
		Stack.processBasicAlteration(stack, getPops(), getPushes());
	}
	
	public int getMethodRefIndex() {
		return parameterValues[0];
	}
	public String getDescriptor() {
		return descriptor;
	}
	public StackElementLength[] getPops() {
		if(isStatic && !load)
			return new StackElementLength[] { stackElementLength };
		else if(isStatic && load)
			return new StackElementLength[0];
		else if(!isStatic && !load)
			return new StackElementLength[] { stackElementLength, ONE };
		else return new StackElementLength[] { ONE };
	}
	public StackElementLength[] getPushes() {
		if(load)
			return new StackElementLength[] { stackElementLength };
		return new StackElementLength[0];
	}
	public boolean isRead() {
		return load;
	}
}