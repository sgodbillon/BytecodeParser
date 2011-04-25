/**
 * 
 */
package bytecodeparser.analysis.decoders;

import static bytecodeparser.analysis.stack.Stack.StackElementLength.DOUBLE;
import static bytecodeparser.analysis.stack.Stack.StackElementLength.ONE;
import javassist.bytecode.Descriptor;
import javassist.bytecode.Opcode;
import bytecodeparser.Context;
import bytecodeparser.analysis.opcodes.FieldOpcode;
import bytecodeparser.analysis.stack.Stack;
import bytecodeparser.analysis.stack.Stack.StackElementLength;

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