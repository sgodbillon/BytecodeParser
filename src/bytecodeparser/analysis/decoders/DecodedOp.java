package bytecodeparser.analysis.decoders;

import javassist.bytecode.CodeIterator;
import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes.OpParameterType;
import bytecodeparser.analysis.opcodes.Op;
import bytecodeparser.analysis.stack.Stack;

public abstract class DecodedOp {
	public final Context context;
	public final int index;
	public final Op op;
	
	public final OpParameterType[] parameterTypes;
	public final int[] parameterValues;
	
	public DecodedOp(Op op, Context context, int index) {
		this.context = context;
		this.index = index;
		this.op = op;
		this.parameterTypes = op.getParameterTypes();
		this.parameterValues = decodeValues(context.iterator, index);
	}
	
	protected final int[] decodeValues(CodeIterator iterator, int index) {
		int[] result = new int[parameterTypes.length];
		int nextValIndex = index + 1;
		for(int i = 0; i < parameterTypes.length; i++) {
			OpParameterType type = parameterTypes[i];
			result[i] = decodeValueAt(type, iterator, nextValIndex);
			nextValIndex += type.size;
		}
		return result;
	}
	
	protected final int decodeValueAt(OpParameterType type, CodeIterator iterator, int index) {
		switch(type) {
			case S1:
				return iterator.byteAt(index);
			case S2:
				return iterator.s16bitAt(index);
			case S4:
				return iterator.s32bitAt(index);
			case U1:
				return iterator.byteAt(index);
			case U2:
				return iterator.u16bitAt(index);
			case U4:
			default:
				throw new RuntimeException("unsupported");
			
		}
	}
	
	public abstract void simulate(Stack stack);
}
