/**
 * 
 */
package bclibs.analysis.opcodes;

import static bclibs.analysis.Opcodes.StackElementLength.DOUBLE;
import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.Opcodes.StackElementLength;
import bclibs.analysis.decoders.DecodedLocalVariableOp;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.ValueFromLocalVariable;

public class LocalVariableOpcode extends BasicOpcode {
	private int base;
	private boolean load;
	public LocalVariableOpcode(int code, boolean load, OpParameterType... opParameterTypes) {
		this(code, code, load, new StackElementLength[0], new StackElementLength[0], opParameterTypes);
	}
	public LocalVariableOpcode(int code, int base, boolean load, OpParameterType... opParameterTypes) {
		this(code, base, load, new StackElementLength[0], new StackElementLength[0], opParameterTypes);
	}
	public LocalVariableOpcode(int code, int base, boolean load, StackElementLength[] pops, StackElementLength[] pushes, OpParameterType... opParameterTypes) {
		super(code, pops, pushes, opParameterTypes);
		this.base = base;
		this.load = load;
	}
	@Override
	public DecodedLocalVariableOp decode(Context context, int index) {
		return new DecodedLocalVariableOp(this, context, index);
	}
	@Override
	public void simulate(Stack stack, Context context, int index) {
		ValueFromLocalVariable toPush = new ValueFromLocalVariable(decode(context, index).localVariable);
		for(int i = 0; i < getPops().length; i++) {
			if(getPops()[i] == DOUBLE)
				stack.pop2();
			else stack.pop();
		}
		for(int i = 0; i < getPushes().length; i++) {
			if(getPushes()[i] == DOUBLE)
				stack.push2(toPush);
			else stack.push(toPush);
		}
	}
	
	public int getBaseOpcode() {
		return base;
	}
	
	public boolean isLoad() {
		return load;
	}
	
	@Override
	public String toString() {
		return "LocalVariableOp: " + getName();
	}
}