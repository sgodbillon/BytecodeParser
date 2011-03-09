package bclibs.analysis.opcodes;

import javassist.bytecode.Opcode;
import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.Constant.*;

public class ConstantPushOpcode extends BasicOpcode {
	public final int baseCode;
	
	public ConstantPushOpcode(int code, OpParameterType... opParameterTypes) {
		this(code, code, opParameterTypes);
	}
	
	public ConstantPushOpcode(int code, int baseCode, OpParameterType... opParameterTypes) {
		super(code, opParameterTypes);
		this.baseCode = baseCode;
	}

	@Override
	public void simulate(Stack stack, Context context, int index) {
		System.out.println("simulate constantpush");
		if(getParameterTypes().length == 0) {
			switch(baseCode) {
				case Opcode.ICONST_0:
					stack.push(new IntegerConstant(getCode() - baseCode));
					break;
				case Opcode.LCONST_0:
					stack.push2(new LongConstant(new Long(new Integer(getCode() - baseCode))));
					break;
				case Opcode.FCONST_0:
					stack.push(new FloatConstant(new Float(getCode() - baseCode)));
					break;
				case Opcode.DCONST_0:
					stack.push2(new DoubleConstant(new Double(getCode() - baseCode)));
					break;
				default:
					throw new RuntimeException("unsupported basecode=" + baseCode + "(" + getName() + ")");
			}
		} else {
			OpParameterType type = getParameterTypes()[0];
			int value = decode(context, index).parameterValues[0];
			if(type == OpParameterType.S1 || type == OpParameterType.S2) {
				System.out.println("is bipush or sipush: " + value);
				for(int i = 0; i < getPops().length; i++) {
					stack.pop(getPops()[i]);
				}
				stack.push(new IntegerConstant(decode(context, index).parameterValues[0]));
			} else if(type == OpParameterType.U1 || type == OpParameterType.U2) {
				Object o = context.behavior.getMethodInfo().getConstPool().getLdcValue(value);
				if(o instanceof Integer)
					stack.push(new IntegerConstant((Integer)o));
				else if(o instanceof Long)
					stack.push2(new LongConstant((Long)o));
				else if(o instanceof Float)
					stack.push(new FloatConstant((Float)o));
				else if(o instanceof Double)
					stack.push2(new DoubleConstant((Double)o));
				else if(o instanceof String)
					stack.push(new StringConstant((String)o));
				else throw new RuntimeException("unsupported type ??? =" + o.getClass() + "(" + code + " : " + getName() + ")");
			} else {
				throw new RuntimeException("unsupported code=" + code + "(" + getName() + ")");
			}
		}
	}
	
	
}
