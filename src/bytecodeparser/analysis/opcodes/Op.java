/**
 * 
 */
package bytecodeparser.analysis.opcodes;

import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes;
import bytecodeparser.analysis.Opcodes.OpParameterType;
import bytecodeparser.analysis.decoders.DecodedOp;

public abstract class Op {
	public final int code;
	protected final OpParameterType[] parameterTypes;
	private String name;
	public Op(int code, OpParameterType... opParameterTypes) {
		this.code = code;
		this.parameterTypes = opParameterTypes;
	}
	
	//public abstract void simulate(Stack stack, Context context, int index);
	public abstract DecodedOp decode(Context context, int index);
	
	/**
	 * Should be called before using this object.
	 * @return this object's copy with some contextual information, if needed.
	 */
	public Op init(Context context, int index) {
		return this;
	}
	
	public int getCode() {
		return code;
	}
	
	public OpParameterType[] getParameterTypes() {
		return parameterTypes;
	}
	
	public String getName() {
		if(name == null)
			name = Opcodes.findOpName(code);
		return name;
	}
	
	@SuppressWarnings(value="unchecked")
	public <T extends Op> T as(Class<T> specificOpClass) {
		return (T) this;
	}
	
	@Override
	public String toString() {
		return "op: " + getName() + "";
	}
}