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
package bytecodeparser.analysis;

import static bytecodeparser.analysis.Opcodes.OpParameterType.S1;
import static bytecodeparser.analysis.Opcodes.OpParameterType.S2;
import static bytecodeparser.analysis.Opcodes.OpParameterType.S4;
import static bytecodeparser.analysis.Opcodes.OpParameterType.U1;
import static bytecodeparser.analysis.Opcodes.OpParameterType.U2;
import static bytecodeparser.analysis.stack.Stack.StackElementLength.DOUBLE;
import static bytecodeparser.analysis.stack.Stack.StackElementLength.ONE;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javassist.bytecode.Opcode;
import bytecodeparser.Context;
import bytecodeparser.analysis.decoders.DecodedOp;
import bytecodeparser.analysis.opcodes.ArrayCreationOpcode;
import bytecodeparser.analysis.opcodes.ArrayOpcode;
import bytecodeparser.analysis.opcodes.BasicOpcode;
import bytecodeparser.analysis.opcodes.BranchOpCode;
import bytecodeparser.analysis.opcodes.ConstantPushOpcode;
import bytecodeparser.analysis.opcodes.ExitOpcode;
import bytecodeparser.analysis.opcodes.FieldOpcode;
import bytecodeparser.analysis.opcodes.LocalVariableOpcode;
import bytecodeparser.analysis.opcodes.MethodInvocationOpcode;
import bytecodeparser.analysis.opcodes.Op;
import bytecodeparser.analysis.opcodes.SwitchOpcode;
import bytecodeparser.analysis.stack.Stack;
import bytecodeparser.analysis.stack.StackElement;

public class Opcodes {
	public static enum OpParameterType {
		U1(1),
		U2(2),
		U4(4),
		S1(1),
		S2(2),
		S4(4);
		
		public final int size;
		
		private OpParameterType(int size) {
			this.size = size;
		}
	}
	
	public static String findOpName(int op) {
		for(Field field : javassist.bytecode.Opcode.class.getFields()) {
			if(Modifier.isStatic(field.getModifiers())) {
				try {
					int foundOp = field.getInt(null);
					if(foundOp == op) {
						return field.getName();
					}
				} catch (Exception e) {
					// nothing
				}
			}
		}
		return "UNKNOWN_OP";
	}
	
	public static final Map<Integer, Op> OPCODES;
	
	static {
		Map<Integer, Op> opcodes = new HashMap<Integer, Op>();
		
		opcodes.put(Opcode.NOP, new BasicOpcode(Opcode.NOP));
		opcodes.put(Opcode.ACONST_NULL, new BasicOpcode(Opcode.ACONST_NULL).setPushes(ONE));
		opcodes.put(Opcode.ICONST_M1, new ConstantPushOpcode(Opcode.ICONST_M1, Opcode.ICONST_0).setPushes(ONE));
		opcodes.put(Opcode.ICONST_0, new ConstantPushOpcode(Opcode.ICONST_0, Opcode.ICONST_0).setPushes(ONE));
		opcodes.put(Opcode.ICONST_1, new ConstantPushOpcode(Opcode.ICONST_1, Opcode.ICONST_0).setPushes(ONE));
		opcodes.put(Opcode.ICONST_2, new ConstantPushOpcode(Opcode.ICONST_2, Opcode.ICONST_0).setPushes(ONE));
		opcodes.put(Opcode.ICONST_3, new ConstantPushOpcode(Opcode.ICONST_3, Opcode.ICONST_0).setPushes(ONE));
		opcodes.put(Opcode.ICONST_4, new ConstantPushOpcode(Opcode.ICONST_4, Opcode.ICONST_0).setPushes(ONE));
		opcodes.put(Opcode.ICONST_5, new ConstantPushOpcode(Opcode.ICONST_5, Opcode.ICONST_0).setPushes(ONE));
		opcodes.put(Opcode.LCONST_0, new ConstantPushOpcode(Opcode.LCONST_0).setPushes(DOUBLE));
		opcodes.put(Opcode.LCONST_1, new ConstantPushOpcode(Opcode.LCONST_1, Opcode.LCONST_0).setPushes(DOUBLE));
		opcodes.put(Opcode.FCONST_0, new ConstantPushOpcode(Opcode.FCONST_0).setPushes(ONE));
		opcodes.put(Opcode.FCONST_1, new ConstantPushOpcode(Opcode.FCONST_1, Opcode.FCONST_0).setPushes(ONE));
		opcodes.put(Opcode.FCONST_2, new ConstantPushOpcode(Opcode.FCONST_2, Opcode.FCONST_0).setPushes(ONE));
		opcodes.put(Opcode.DCONST_0, new ConstantPushOpcode(Opcode.DCONST_0).setPushes(DOUBLE));
		opcodes.put(Opcode.DCONST_1, new ConstantPushOpcode(Opcode.DCONST_1, Opcode.DCONST_0).setPushes(DOUBLE));
		opcodes.put(Opcode.BIPUSH, new ConstantPushOpcode(Opcode.BIPUSH, S1).setPushes(ONE));
		opcodes.put(Opcode.SIPUSH, new ConstantPushOpcode(Opcode.SIPUSH, S2).setPushes(ONE));
		opcodes.put(Opcode.LDC, new ConstantPushOpcode(Opcode.LDC, U1).setPushes(ONE));
		opcodes.put(Opcode.LDC_W, new ConstantPushOpcode(Opcode.LDC_W, U2).setPushes(ONE));
		opcodes.put(Opcode.LDC2_W, new ConstantPushOpcode(Opcode.LDC2_W, U2).setPushes(DOUBLE));
		opcodes.put(Opcode.ILOAD, new LocalVariableOpcode(Opcode.ILOAD, true, U1).setPushes(ONE));
		opcodes.put(Opcode.LLOAD, new LocalVariableOpcode(Opcode.LLOAD, true, U1).setPushes(DOUBLE));
		opcodes.put(Opcode.FLOAD, new LocalVariableOpcode(Opcode.FLOAD, true, U1).setPushes(ONE));
		opcodes.put(Opcode.DLOAD, new LocalVariableOpcode(Opcode.DLOAD, true, U1).setPushes(DOUBLE));
		opcodes.put(Opcode.ALOAD, new LocalVariableOpcode(Opcode.ALOAD, true, U1).setPushes(ONE));
		opcodes.put(Opcode.ILOAD_0, new LocalVariableOpcode(Opcode.ILOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.ILOAD_1, new LocalVariableOpcode(Opcode.ILOAD_1, Opcode.ILOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.ILOAD_2, new LocalVariableOpcode(Opcode.ILOAD_2, Opcode.ILOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.ILOAD_3, new LocalVariableOpcode(Opcode.ILOAD_3, Opcode.ILOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.LLOAD_0, new LocalVariableOpcode(Opcode.LLOAD_0, true).setPushes(DOUBLE));
		opcodes.put(Opcode.LLOAD_1, new LocalVariableOpcode(Opcode.LLOAD_1, Opcode.LLOAD_0, true).setPushes(DOUBLE));
		opcodes.put(Opcode.LLOAD_2, new LocalVariableOpcode(Opcode.LLOAD_2, Opcode.LLOAD_0, true).setPushes(DOUBLE));
		opcodes.put(Opcode.LLOAD_3, new LocalVariableOpcode(Opcode.LLOAD_3, Opcode.LLOAD_0, true).setPushes(DOUBLE));
		opcodes.put(Opcode.FLOAD_0, new LocalVariableOpcode(Opcode.FLOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.FLOAD_1, new LocalVariableOpcode(Opcode.FLOAD_1, Opcode.FLOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.FLOAD_2, new LocalVariableOpcode(Opcode.FLOAD_2, Opcode.FLOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.FLOAD_3, new LocalVariableOpcode(Opcode.FLOAD_3, Opcode.FLOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.DLOAD_0, new LocalVariableOpcode(Opcode.DLOAD_0, true).setPushes(DOUBLE));
		opcodes.put(Opcode.DLOAD_1, new LocalVariableOpcode(Opcode.DLOAD_1, Opcode.DLOAD_0, true).setPushes(DOUBLE));
		opcodes.put(Opcode.DLOAD_2, new LocalVariableOpcode(Opcode.DLOAD_2, Opcode.DLOAD_0, true).setPushes(DOUBLE));
		opcodes.put(Opcode.DLOAD_3, new LocalVariableOpcode(Opcode.DLOAD_3, Opcode.DLOAD_0, true).setPushes(DOUBLE));
		opcodes.put(Opcode.ALOAD_0, new LocalVariableOpcode(Opcode.ALOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.ALOAD_1, new LocalVariableOpcode(Opcode.ALOAD_1, Opcode.ALOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.ALOAD_2, new LocalVariableOpcode(Opcode.ALOAD_2, Opcode.ALOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.ALOAD_3, new LocalVariableOpcode(Opcode.ALOAD_3, Opcode.ALOAD_0, true).setPushes(ONE));
		opcodes.put(Opcode.IALOAD, new ArrayOpcode(Opcode.IALOAD, true).setPops(ONE, ONE).setPushes(ONE)); // ARRAY
		opcodes.put(Opcode.LALOAD, new ArrayOpcode(Opcode.LALOAD, true).setPops(ONE, ONE).setPushes(DOUBLE)); // ARRAY
		opcodes.put(Opcode.FALOAD, new ArrayOpcode(Opcode.FALOAD, true).setPops(ONE, ONE).setPushes(ONE)); // ARRAY
		opcodes.put(Opcode.DALOAD, new ArrayOpcode(Opcode.DALOAD, true).setPops(ONE, ONE).setPushes(DOUBLE)); // ARRAY
		opcodes.put(Opcode.AALOAD, new ArrayOpcode(Opcode.AALOAD, true).setPops(ONE, ONE).setPushes(ONE)); // ARRAY
		opcodes.put(Opcode.BALOAD, new ArrayOpcode(Opcode.BALOAD, true).setPops(ONE, ONE).setPushes(ONE)); // ARRAY
		opcodes.put(Opcode.CALOAD, new ArrayOpcode(Opcode.CALOAD, true).setPops(ONE, ONE).setPushes(ONE)); // ARRAY
		opcodes.put(Opcode.SALOAD, new ArrayOpcode(Opcode.SALOAD, true).setPops(ONE, ONE).setPushes(ONE)); // ARRAY
		opcodes.put(Opcode.ISTORE, new LocalVariableOpcode(Opcode.ISTORE, false, U1).setPops(ONE));
		opcodes.put(Opcode.LSTORE, new LocalVariableOpcode(Opcode.LSTORE, false, U1).setPops(DOUBLE));
		opcodes.put(Opcode.FSTORE, new LocalVariableOpcode(Opcode.FSTORE, false, U1).setPops(ONE));
		opcodes.put(Opcode.DSTORE, new LocalVariableOpcode(Opcode.DSTORE, false, U1).setPops(DOUBLE));
		opcodes.put(Opcode.ASTORE, new LocalVariableOpcode(Opcode.ASTORE, false, U1).setPops(ONE));
		opcodes.put(Opcode.ISTORE_0, new LocalVariableOpcode(Opcode.ISTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.ISTORE_1, new LocalVariableOpcode(Opcode.ISTORE_1, Opcode.ISTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.ISTORE_2, new LocalVariableOpcode(Opcode.ISTORE_2, Opcode.ISTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.ISTORE_3, new LocalVariableOpcode(Opcode.ISTORE_3, Opcode.ISTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.LSTORE_0, new LocalVariableOpcode(Opcode.LSTORE_0, false).setPops(DOUBLE));
		opcodes.put(Opcode.LSTORE_1, new LocalVariableOpcode(Opcode.LSTORE_1, Opcode.LSTORE_0, false).setPops(DOUBLE));
		opcodes.put(Opcode.LSTORE_2, new LocalVariableOpcode(Opcode.LSTORE_2, Opcode.LSTORE_0, false).setPops(DOUBLE));
		opcodes.put(Opcode.LSTORE_3, new LocalVariableOpcode(Opcode.LSTORE_3, Opcode.LSTORE_0, false).setPops(DOUBLE));
		opcodes.put(Opcode.FSTORE_0, new LocalVariableOpcode(Opcode.FSTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.FSTORE_1, new LocalVariableOpcode(Opcode.FSTORE_1, Opcode.FSTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.FSTORE_2, new LocalVariableOpcode(Opcode.FSTORE_2, Opcode.FSTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.FSTORE_3, new LocalVariableOpcode(Opcode.FSTORE_3, Opcode.FSTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.DSTORE_0, new LocalVariableOpcode(Opcode.DSTORE_0, false).setPops(DOUBLE));
		opcodes.put(Opcode.DSTORE_1, new LocalVariableOpcode(Opcode.DSTORE_1, Opcode.DSTORE_0, false).setPops(DOUBLE));
		opcodes.put(Opcode.DSTORE_2, new LocalVariableOpcode(Opcode.DSTORE_2, Opcode.DSTORE_0, false).setPops(DOUBLE));
		opcodes.put(Opcode.DSTORE_3, new LocalVariableOpcode(Opcode.DSTORE_3, Opcode.DSTORE_0, false).setPops(DOUBLE));
		opcodes.put(Opcode.ASTORE_0, new LocalVariableOpcode(Opcode.ASTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.ASTORE_1, new LocalVariableOpcode(Opcode.ASTORE_1, Opcode.ASTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.ASTORE_2, new LocalVariableOpcode(Opcode.ASTORE_2, Opcode.ASTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.ASTORE_3, new LocalVariableOpcode(Opcode.ASTORE_3, Opcode.ASTORE_0, false).setPops(ONE));
		opcodes.put(Opcode.IASTORE, new ArrayOpcode(Opcode.IASTORE, false).setPops(ONE, ONE, ONE)); // ARRAY
		opcodes.put(Opcode.LASTORE, new ArrayOpcode(Opcode.LASTORE, false).setPops(DOUBLE, ONE, ONE)); // ARRAY
		opcodes.put(Opcode.FASTORE, new ArrayOpcode(Opcode.FASTORE, false).setPops(ONE, ONE, ONE)); // ARRAY
		opcodes.put(Opcode.DASTORE, new ArrayOpcode(Opcode.DASTORE, false).setPops(DOUBLE, ONE, ONE)); // ARRAY
		opcodes.put(Opcode.AASTORE, new ArrayOpcode(Opcode.AASTORE, false).setPops(ONE, ONE, ONE)); // ARRAY
		opcodes.put(Opcode.BASTORE, new ArrayOpcode(Opcode.BASTORE, false).setPops(ONE, ONE, ONE)); // ARRAY
		opcodes.put(Opcode.CASTORE, new ArrayOpcode(Opcode.CASTORE, false).setPops(ONE, ONE, ONE)); // ARRAY
		opcodes.put(Opcode.SASTORE, new ArrayOpcode(Opcode.SASTORE, false).setPops(ONE, ONE, ONE)); // ARRAY
		opcodes.put(Opcode.POP, new BasicOpcode(Opcode.POP).setPops(ONE));
		opcodes.put(Opcode.POP2, new Op(Opcode.POP2) {
			@Override
			public DecodedOp decode(Context context, int index) {
				return new DecodedOp(this, context, index) {
					@Override
					public void simulate(Stack stack) {
						stack.stack.pop();
						stack.stack.pop();
					}
				};
			}
		});
		opcodes.put(Opcode.DUP, new Op(Opcode.DUP) {
			@Override
			public DecodedOp decode(Context context, int index) {
				return new DecodedOp(this, context, index) {
					@Override
					public void simulate(Stack stack) {
						stack.push(stack.peek().copy());
					}
				};
			}
		});
		opcodes.put(Opcode.DUP_X1, new Op(Opcode.DUP_X1) {
			@Override
			public DecodedOp decode(Context context, int index) {
				return new DecodedOp(this, context, index) {
					@Override
					public void simulate(Stack stack) {
						StackElement se = stack.peek().copy();
						stack.stack.add(2, se);
					}
				};
			};
		});
		opcodes.put(Opcode.DUP_X2, new Op(Opcode.DUP_X2) {
			@Override
			public DecodedOp decode(Context context, int index) {
				return new DecodedOp(this, context, index) {
					@Override
					public void simulate(Stack stack) {
						StackElement se = stack.peek().copy();
						stack.stack.add(3, se);
					}
				};
			}
		});
		opcodes.put(Opcode.DUP2, new Op(Opcode.DUP2) {
			@Override
			public DecodedOp decode(Context context, int index) {
				return new DecodedOp(this, context, index) {
					@Override
					public void simulate(Stack stack) {
						StackElement[] elements = new StackElement[2];
						elements[0] = stack.stack.get(0).copy();
						elements[1] = stack.stack.get(1).copy();
						stack.stack.push(elements[1]);
						stack.stack.push(elements[0]);
					}
				};
			}
		});
		opcodes.put(Opcode.DUP2_X1, new Op(Opcode.DUP2_X1) {
			@Override
			public DecodedOp decode(Context context, int index) {
				return new DecodedOp(this, context, index) {
					@Override
					public void simulate(Stack stack) {
						StackElement[] elements = new StackElement[2];
						elements[0] = stack.stack.get(0).copy();
						elements[1] = stack.stack.get(1).copy();
						stack.stack.add(3, elements[0]);
						stack.stack.add(3, elements[1]);
					}
				};
			}
		});
		opcodes.put(Opcode.DUP2_X2, new Op(Opcode.DUP2_X2) {
			public DecodedOp decode(Context context, int index) {
				return new DecodedOp(this, context, index) {
					@Override
					public void simulate(Stack stack) {
						StackElement[] elements = new StackElement[2];
						elements[0] = stack.stack.get(0).copy();
						elements[1] = stack.stack.get(1).copy();
						stack.stack.add(4, elements[0]);
						stack.stack.add(4, elements[1]);
					}
				};
			}
		});
		opcodes.put(Opcode.SWAP, new Op(Opcode.SWAP) {
			@Override
			public DecodedOp decode(Context context, int index) {
				return new DecodedOp(this, context, index) {
					@Override
					public void simulate(Stack stack) {
						StackElement se = stack.pop();
						stack.stack.add(stack.stack.size() - 1, se);
					}
				};
			}
		});
		opcodes.put(Opcode.IADD, new BasicOpcode(Opcode.IADD).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LADD, new BasicOpcode(Opcode.LADD).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.FADD, new BasicOpcode(Opcode.FADD).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.DADD, new BasicOpcode(Opcode.DADD).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.ISUB, new BasicOpcode(Opcode.ISUB).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LSUB, new BasicOpcode(Opcode.LSUB).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.FSUB, new BasicOpcode(Opcode.FSUB).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.DSUB, new BasicOpcode(Opcode.DSUB).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.IMUL, new BasicOpcode(Opcode.IMUL).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LMUL, new BasicOpcode(Opcode.LMUL).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.FMUL, new BasicOpcode(Opcode.FMUL).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.DMUL, new BasicOpcode(Opcode.DMUL).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.IDIV, new BasicOpcode(Opcode.IDIV).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LDIV, new BasicOpcode(Opcode.LDIV).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.FDIV, new BasicOpcode(Opcode.FDIV).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.DDIV, new BasicOpcode(Opcode.DDIV).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.IREM, new BasicOpcode(Opcode.IREM).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LREM, new BasicOpcode(Opcode.LREM).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.FREM, new BasicOpcode(Opcode.FREM).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.DREM, new BasicOpcode(Opcode.DREM).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.INEG, new BasicOpcode(Opcode.INEG).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.LNEG, new BasicOpcode(Opcode.LNEG).setPops(DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.FNEG, new BasicOpcode(Opcode.FNEG).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.DNEG, new BasicOpcode(Opcode.DNEG).setPops(DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.ISHL, new BasicOpcode(Opcode.ISHL).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LSHL, new BasicOpcode(Opcode.LSHL).setPops(ONE, DOUBLE).setPushes(ONE));
		opcodes.put(Opcode.ISHR, new BasicOpcode(Opcode.ISHR).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LSHR, new BasicOpcode(Opcode.LSHR).setPops(ONE, DOUBLE).setPushes(ONE));
		opcodes.put(Opcode.IUSHR, new BasicOpcode(Opcode.IUSHR).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LUSHR, new BasicOpcode(Opcode.LUSHR).setPops(ONE, DOUBLE).setPushes(ONE));
		opcodes.put(Opcode.IAND, new BasicOpcode(Opcode.IAND).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LAND, new BasicOpcode(Opcode.LAND).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.IOR, new BasicOpcode(Opcode.IOR).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LOR, new BasicOpcode(Opcode.LOR).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.IXOR, new BasicOpcode(Opcode.IXOR).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LXOR, new BasicOpcode(Opcode.LXOR).setPops(DOUBLE, DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.IINC, new LocalVariableOpcode(Opcode.IINC, false, U1, S1));
		opcodes.put(Opcode.I2L, new BasicOpcode(Opcode.I2L).setPops(ONE).setPushes(DOUBLE));
		opcodes.put(Opcode.I2F, new BasicOpcode(Opcode.I2F).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.I2D, new BasicOpcode(Opcode.I2D).setPops(ONE).setPushes(DOUBLE));
		opcodes.put(Opcode.L2I, new BasicOpcode(Opcode.L2I).setPops(DOUBLE).setPushes(ONE));
		opcodes.put(Opcode.L2F, new BasicOpcode(Opcode.L2F).setPops(DOUBLE).setPushes(ONE));
		opcodes.put(Opcode.L2F, new BasicOpcode(Opcode.L2F).setPops(DOUBLE).setPushes(ONE));
		opcodes.put(Opcode.L2D, new BasicOpcode(Opcode.L2D).setPops(DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.F2I, new BasicOpcode(Opcode.F2I).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.F2L, new BasicOpcode(Opcode.F2L).setPops(ONE).setPushes(DOUBLE));
		opcodes.put(Opcode.F2D, new BasicOpcode(Opcode.F2D).setPops(ONE).setPushes(DOUBLE));
		opcodes.put(Opcode.D2I, new BasicOpcode(Opcode.D2I).setPops(DOUBLE).setPushes(ONE));
		opcodes.put(Opcode.D2L, new BasicOpcode(Opcode.D2L).setPops(DOUBLE).setPushes(DOUBLE));
		opcodes.put(Opcode.D2F, new BasicOpcode(Opcode.D2F).setPops(DOUBLE).setPushes(ONE));
		opcodes.put(Opcode.I2B, new BasicOpcode(Opcode.I2B).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.I2C, new BasicOpcode(Opcode.I2C).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.I2S, new BasicOpcode(Opcode.I2S).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.LCMP, new BasicOpcode(Opcode.LCMP).setPops(DOUBLE, DOUBLE).setPushes(ONE));
		opcodes.put(Opcode.FCMPL, new BasicOpcode(Opcode.FCMPL).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.FCMPG, new BasicOpcode(Opcode.FCMPG).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.DCMPL, new BasicOpcode(Opcode.DCMPL).setPops(DOUBLE, DOUBLE).setPushes(ONE));
		opcodes.put(Opcode.DCMPG, new BasicOpcode(Opcode.DCMPG).setPops(DOUBLE, DOUBLE).setPushes(ONE));
		opcodes.put(Opcode.IFEQ, new BranchOpCode(Opcode.IFEQ, S2).setPops(ONE));
		opcodes.put(Opcode.IFNE, new BranchOpCode(Opcode.IFNE, S2).setPops(ONE));
		opcodes.put(Opcode.IFLT, new BranchOpCode(Opcode.IFLT, S2).setPops(ONE));
		opcodes.put(Opcode.IFGE, new BranchOpCode(Opcode.IFGE, S2).setPops(ONE));
		opcodes.put(Opcode.IFGT, new BranchOpCode(Opcode.IFGT, S2).setPops(ONE));
		opcodes.put(Opcode.IFLE, new BranchOpCode(Opcode.IFLE, S2).setPops(ONE));
		opcodes.put(Opcode.IF_ICMPEQ, new BranchOpCode(Opcode.IF_ICMPEQ, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ICMPNE, new BranchOpCode(Opcode.IF_ICMPNE, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ICMPLT, new BranchOpCode(Opcode.IF_ICMPLT, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ICMPGE, new BranchOpCode(Opcode.IF_ICMPGE, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ICMPGT, new BranchOpCode(Opcode.IF_ICMPGT, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ICMPLE, new BranchOpCode(Opcode.IF_ICMPLE, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ACMPEQ, new BranchOpCode(Opcode.IF_ACMPEQ, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ACMPNE, new BranchOpCode(Opcode.IF_ACMPNE, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.GOTO, new BranchOpCode(Opcode.GOTO, S2));
		opcodes.put(Opcode.JSR, new BranchOpCode(Opcode.JSR, S2));
		opcodes.put(Opcode.RET, new BranchOpCode(Opcode.RET, U1));
		opcodes.put(Opcode.TABLESWITCH, new SwitchOpcode(Opcode.TABLESWITCH));
		opcodes.put(Opcode.LOOKUPSWITCH, new SwitchOpcode(Opcode.LOOKUPSWITCH));
		opcodes.put(Opcode.IRETURN, new ExitOpcode(Opcode.IRETURN).setPops(ONE));
		opcodes.put(Opcode.LRETURN, new ExitOpcode(Opcode.LRETURN).setPops(DOUBLE));
		opcodes.put(Opcode.FRETURN, new ExitOpcode(Opcode.FRETURN).setPops(ONE));
		opcodes.put(Opcode.DRETURN, new ExitOpcode(Opcode.DRETURN).setPops(DOUBLE));
		opcodes.put(Opcode.ARETURN, new ExitOpcode(Opcode.ARETURN).setPops(ONE));
		opcodes.put(Opcode.RETURN, new ExitOpcode(Opcode.RETURN));
		opcodes.put(Opcode.GETSTATIC, new FieldOpcode(Opcode.GETSTATIC));
		opcodes.put(Opcode.PUTSTATIC, new FieldOpcode(Opcode.PUTSTATIC));
		opcodes.put(Opcode.GETFIELD, new FieldOpcode(Opcode.GETFIELD));
		opcodes.put(Opcode.PUTFIELD, new FieldOpcode(Opcode.PUTFIELD));
		opcodes.put(Opcode.INVOKEVIRTUAL, new MethodInvocationOpcode(Opcode.INVOKEVIRTUAL));
		opcodes.put(Opcode.INVOKESPECIAL, new MethodInvocationOpcode(Opcode.INVOKESPECIAL));
		opcodes.put(Opcode.INVOKESTATIC, new MethodInvocationOpcode(Opcode.INVOKESTATIC));
		opcodes.put(Opcode.INVOKEINTERFACE, new MethodInvocationOpcode(Opcode.INVOKEINTERFACE));
		opcodes.put(Opcode.NEW, new BasicOpcode(Opcode.NEW, U2).setPushes(ONE));
		opcodes.put(Opcode.NEWARRAY, new ArrayCreationOpcode(Opcode.NEWARRAY, U1).setPops(ONE).setPushes(ONE)); // ARRAY
		opcodes.put(Opcode.ANEWARRAY, new ArrayCreationOpcode(Opcode.ANEWARRAY, U2).setPops(ONE).setPushes(ONE)); // ARRAY
		opcodes.put(Opcode.ARRAYLENGTH, new BasicOpcode(Opcode.ARRAYLENGTH).setPops(ONE).setPushes(ONE)); // ARRAY
		opcodes.put(Opcode.ATHROW, new ExitOpcode(Opcode.ATHROW).setPops(ONE));
		opcodes.put(Opcode.CHECKCAST, new BasicOpcode(Opcode.CHECKCAST, U2).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.INSTANCEOF, new BasicOpcode(Opcode.INSTANCEOF, U2).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.MONITORENTER, new BasicOpcode(Opcode.MONITORENTER).setPops(ONE));
		opcodes.put(Opcode.MONITOREXIT, new BasicOpcode(Opcode.MONITOREXIT).setPops(ONE));
		//opcodes.put(Opcode.WIDE, new BasicOpcode(Opcode.WIDE)); // TODO
		opcodes.put(Opcode.MULTIANEWARRAY, new ArrayCreationOpcode(Opcode.MULTIANEWARRAY, U2, U1));
		opcodes.put(Opcode.IFNULL, new BranchOpCode(Opcode.IFNULL, S2).setPops(ONE));
		opcodes.put(Opcode.IFNONNULL, new BranchOpCode(Opcode.IFNONNULL, S2).setPops(ONE));
		opcodes.put(Opcode.GOTO_W, new BranchOpCode(Opcode.GOTO_W, S4));
		opcodes.put(Opcode.JSR_W, new BranchOpCode(Opcode.JSR_W, S4).setPushes(ONE));
		
		OPCODES = Collections.unmodifiableMap(opcodes);
	}
}
