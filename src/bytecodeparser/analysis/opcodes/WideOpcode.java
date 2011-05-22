package bytecodeparser.analysis.opcodes;

import bytecodeparser.Context;
import bytecodeparser.analysis.decoders.DecodedLocalVariableOp;
import bytecodeparser.analysis.decoders.DecodedWideOp;
import javassist.bytecode.Opcode;

public class WideOpcode extends Op {
	private final DecodedWideOp decodedWideOp;
	
	public WideOpcode() {
		this(null);
	}
	
	private WideOpcode(DecodedWideOp decodedWideOp) {
		super(Opcode.WIDE);
		this.decodedWideOp = decodedWideOp;
	}
	
	@Override
	public Op init(Context context, int index) {
		return new WideOpcode(new DecodedWideOp(this, context, index));
	}
	
	@Override
	public DecodedWideOp decode(Context context, int index) {
		if(decodedWideOp != null)
			return decodedWideOp;
		throw new RuntimeException("must be initialized before !");
	}
	
	public LocalVariableOpcode getWrappedLocalVariableOpcode() {
		if(decodedWideOp != null)
			return decodedWideOp.op.as(LocalVariableOpcode.class);
		throw new RuntimeException("must be initialized before !");
	}
	
	public DecodedLocalVariableOp getWrappedDecodedLocalVariableOp() {
		if(decodedWideOp != null)
			return decodedWideOp.wrappedDecodedLocalVariableOp;
		throw new RuntimeException("must be initialized before !");
	}
}
