package bytecodeparser.analysis.opcodes;

import bytecodeparser.Context;
import bytecodeparser.analysis.decoders.DecodedSwitchOpcode;

public class SwitchOpcode extends Op {
	
	public SwitchOpcode(int code) {
		super(code);
	}
	
	@Override
	public DecodedSwitchOpcode decode(Context context, int index) {
		return new DecodedSwitchOpcode(this, context, index);
	}
}
