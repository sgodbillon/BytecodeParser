package bytecodeparser.analysis.decoders;

import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes;
import bytecodeparser.analysis.opcodes.LocalVariableOpcode;
import bytecodeparser.analysis.opcodes.WideOpcode;
import bytecodeparser.analysis.stack.Stack;

public class DecodedWideOp extends DecodedOp {
	public final DecodedLocalVariableOp wrappedDecodedLocalVariableOp;
	
	public DecodedWideOp(WideOpcode wide, Context context, int index) {
		super(wide, context, index);
		wrappedDecodedLocalVariableOp = Opcodes.OPCODES.get(context.iterator.byteAt(index + 1)).as(LocalVariableOpcode.class).decodeWide(context, index);
	}
	
	@Override
	public void simulate(Stack stack) {
		wrappedDecodedLocalVariableOp.simulate(stack);
	}
}
