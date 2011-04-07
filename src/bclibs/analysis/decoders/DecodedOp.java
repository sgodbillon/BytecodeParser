package bclibs.analysis.decoders;

import javassist.bytecode.CodeIterator;
import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.opcodes.Op;
import bclibs.analysis.stack.Stack;

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
			switch(type) {
				case S1:
					result[i] = iterator.byteAt(nextValIndex);
					nextValIndex ++;
					break;
				case S2:
					result[i] = iterator.s16bitAt(nextValIndex);
					nextValIndex += 2;
					break;
				case S4:
					result[i] = iterator.s32bitAt(nextValIndex);
					nextValIndex += 4;
					break;
				case U1:
					result[i] = iterator.byteAt(nextValIndex);
					nextValIndex ++;
					break;
				case U2:
					result[i] = iterator.u16bitAt(nextValIndex);
					nextValIndex += 2;
					break;
				case U4:
				default:
					throw new RuntimeException("unsupported");
				
			}
		}
		return result;
	}
	
	public abstract void simulate(Stack stack);
}
