package bclibs.analysis.decoders;

import java.lang.reflect.Method;

import javassist.bytecode.ConstPool;
import javassist.bytecode.Opcode;
import bclibs.analysis.Context;
import bclibs.analysis.Opcodes.OpParameterType;
import bclibs.analysis.opcodes.ConstantPushOpcode;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.Constant.DoubleConstant;
import bclibs.analysis.stack.Constant.FloatConstant;
import bclibs.analysis.stack.Constant.IntegerConstant;
import bclibs.analysis.stack.Constant.LongConstant;
import bclibs.analysis.stack.Constant.StringConstant;
import bclibs.analysis.stack.Constant.WhateverConstant;

public class DecodedConstantPushOp extends DecodedBasicOp {
	public DecodedConstantPushOp(ConstantPushOpcode op, Context context, int index) {
		super(op, context, index);
	}
	
	@Override
	public void simulate(Stack stack) {
		//System.out.println("simulate constantpush");
		ConstantPushOpcode cpop = this.op.as(ConstantPushOpcode.class);
		if(cpop.getParameterTypes().length == 0) {
			switch(cpop.baseCode) {
				case Opcode.ICONST_0:
					stack.push(new IntegerConstant(cpop.getCode() - cpop.baseCode));
					break;
				case Opcode.LCONST_0:
					stack.push2(new LongConstant(new Long(new Integer(cpop.getCode() - cpop.baseCode))));
					break;
				case Opcode.FCONST_0:
					stack.push(new FloatConstant(new Float(cpop.getCode() - cpop.baseCode)));
					break;
				case Opcode.DCONST_0:
					stack.push2(new DoubleConstant(new Double(cpop.getCode() - cpop.baseCode)));
					break;
				default:
					throw new RuntimeException("unsupported basecode=" + cpop.baseCode + "(" + cpop.getName() + ")");
			}
		} else {
			OpParameterType type = cpop.getParameterTypes()[0];
			int value = cpop.decode(context, index).parameterValues[0];
			if(type == OpParameterType.S1 || type == OpParameterType.S2) {
				//System.out.println("is bipush or sipush: " + value);
				for(int i = 0; i < getPops().length; i++) {
					stack.pop(getPops()[i]);
				}
				stack.push(new IntegerConstant(cpop.decode(context, index).parameterValues[0]));
			} else if(type == OpParameterType.U1 || type == OpParameterType.U2) {
				Object o = context.behavior.getMethodInfo().getConstPool().getLdcValue(value);
				if(o == null) {
					//System.out.println("$$ ERROR $$ " + index + ": " + getCode() + " (" + getName() + "), val="+value);
					ConstPool cp = context.behavior.getMethodInfo().getConstPool();
					for(Method m : ConstPool.class.getDeclaredMethods()) {
						if(m.getName().equals("getItem")) {
							m.setAccessible(true);
							try {
								Object _o = m.invoke(cp, new Integer(value));
								//System.out.println("entry was " + (_o == null ? "null" : _o.getClass().toString()));
								stack.push(new WhateverConstant(_o));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					return;
				}
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
				else throw new RuntimeException("unsupported type ??? =" + o.getClass() + "(" + cpop.code + " : " + cpop.getName() + ")");
			} else {
				throw new RuntimeException("unsupported code=" + cpop.code + "(" + cpop.getName() + ")");
			}
		}
	}
}
