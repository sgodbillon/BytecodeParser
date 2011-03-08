/**
 * 
 */
package bclibs.analysis.opcodes;

import javassist.CtBehavior;
import javassist.bytecode.CodeIterator;
import bclibs.analysis.Opcodes;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.decoders.DecodedOp;
import bclibs.analysis.stack.Stack;

public abstract class Op {
	protected final int code;
	protected final OpParameterType[] parameterTypes;
	private String name;
	public Op(int code, OpParameterType... opParameterTypes) {
		this.code = code;
		this.parameterTypes = opParameterTypes;
	}
	
	public abstract void simulate(Stack stack, CtBehavior behavior, CodeIterator iterator, int index);
	public abstract DecodedOp decode(CtBehavior behavior, CodeIterator iterator, int index);
	
	/**
	 * Should be called before using this object.
	 * @return this object's copy with some contextual information, if needed.
	 */
	public Op init(CtBehavior behavior, CodeIterator iterator, int index) {
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
	
	@Override
	public String toString() {
		return "op: " + getName() + "";
	}
}