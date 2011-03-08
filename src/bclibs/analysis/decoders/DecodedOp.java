/**
 * 
 */
package bclibs.analysis.decoders;

import javassist.CtBehavior;
import javassist.bytecode.CodeIterator;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.opcodes.Op;

public class DecodedOp {
	public final OpParameterType[] parameterTypes;
	public final int[] parameterValues;
	public final CtBehavior behavior;
	public final CodeIterator iterator;
	public final int index;
	
	public DecodedOp(Op op, CtBehavior behavior, CodeIterator iterator, int index) {
		this.parameterTypes = op.getParameterTypes();
		this.parameterValues = decodeValues(iterator, index);
		this.behavior = behavior;
		this.iterator = iterator;
		this.index = index;
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
}