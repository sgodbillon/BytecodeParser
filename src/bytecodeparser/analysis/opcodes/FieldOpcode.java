/**
 * 
 */
package bytecodeparser.analysis.opcodes;

import static bytecodeparser.analysis.Opcodes.OpParameterType.U2;
import bytecodeparser.Context;
import bytecodeparser.analysis.decoders.DecodedFieldOp;
import bytecodeparser.analysis.stack.Stack.StackElementLength;

public class FieldOpcode extends Op {
	private final DecodedFieldOp decodedOp;
	
	public FieldOpcode(int code) {
		this(code, null);
	}
	private FieldOpcode(int code, DecodedFieldOp decodedOp) {
		super(code, U2);
		this.decodedOp = decodedOp;
	}
	@Override
	public FieldOpcode init(Context context, int index) {
		return new FieldOpcode(code, decode(context, index));
	}
	@Override
	public DecodedFieldOp decode(Context context, int index) {
		if(decodedOp != null)
			return decodedOp;
		try {
			DecodedFieldOp decodedOp = new DecodedFieldOp(this, context, index);
			return decodedOp;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public StackElementLength[] getPops() {
		if(decodedOp == null)
			throw new IllegalStateException("must be initialized before !");
		return decodedOp.getPops();
	}
	
	public StackElementLength[] getPushes() {
		if(decodedOp == null)
			throw new IllegalStateException("must be initialized before !");
		return decodedOp.getPushes();
	}
}