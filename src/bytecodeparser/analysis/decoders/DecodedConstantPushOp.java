/*
 *  Copyright (C) 2011 Stephane Godbillon
 *  
 *  This file is part of BytecodeParser. See the README file in the root
 *  directory of this project.
 *
 *  BytecodeParser is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  BytecodeParser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with BytecodeParser.  If not, see <http://www.gnu.org/licenses/>.
 */
package bytecodeparser.analysis.decoders;

import java.lang.reflect.Method;

import javassist.bytecode.ConstPool;
import javassist.bytecode.Opcode;
import bytecodeparser.Context;
import bytecodeparser.analysis.Opcodes.OpParameterType;
import bytecodeparser.analysis.opcodes.ConstantPushOpcode;
import bytecodeparser.analysis.stack.Constant.DoubleConstant;
import bytecodeparser.analysis.stack.Constant.FloatConstant;
import bytecodeparser.analysis.stack.Constant.IntegerConstant;
import bytecodeparser.analysis.stack.Constant.LongConstant;
import bytecodeparser.analysis.stack.Constant.StringConstant;
import bytecodeparser.analysis.stack.Constant.WhateverConstant;
import bytecodeparser.analysis.stack.Stack.StackElementLength;
import bytecodeparser.analysis.stack.Stack;

public class DecodedConstantPushOp extends DecodedBasicOp {
	public DecodedConstantPushOp(ConstantPushOpcode op, Context context, int index) {
		super(op, context, index);
	}
	
	@Override
	public void simulate(Stack stack) {
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
				for(int i = 0; i < getPops().length; i++) {
					stack.pop(getPops()[i]);
				}
				stack.push(new IntegerConstant(cpop.decode(context, index).parameterValues[0]));
			} else if(type == OpParameterType.U1 || type == OpParameterType.U2) {
				Object o = context.behavior.getMethodInfo().getConstPool().getLdcValue(value);
				if(o == null) {
					ConstPool cp = context.behavior.getMethodInfo().getConstPool();
					for(Method m : ConstPool.class.getDeclaredMethods()) {
						if(m.getName().equals("getItem")) {
							m.setAccessible(true);
							try {
								Object _o = m.invoke(cp, new Integer(value));
								stack.push(new WhateverConstant(_o));
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					}
					return;
				}
				if(pushes[0].equals(StackElementLength.DOUBLE) && !(o instanceof Long) && !(o instanceof Double))
					throw new RuntimeException("Constant push of type " + op.getName() + " should push a double-size element but is not! (o = " + o + ")");
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
