package bclibs.analysis.opcodes;


import bclibs.analysis.Context;
import bclibs.analysis.decoders.DecodedSwitchOpcode;

public class SwitchOpcode extends Op {
	
	public SwitchOpcode(int code) {
		super(code);
	}
	
	@Override
	public DecodedSwitchOpcode decode(Context context, int index) {
		return new DecodedSwitchOpcode(this, context, index);
	}
}
