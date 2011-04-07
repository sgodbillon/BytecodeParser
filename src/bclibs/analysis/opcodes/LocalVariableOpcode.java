/**
 * 
 */
package bclibs.analysis.opcodes;

import static bclibs.analysis.stack.Stack.StackElementLength;
import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.decoders.DecodedLocalVariableOp;

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