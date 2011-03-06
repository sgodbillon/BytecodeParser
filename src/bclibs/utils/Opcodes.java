package bclibs.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import bclibs.LocalVariable;

import javassist.CtBehavior;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;
import static bclibs.utils.Opcodes.StackElementLength.*;
import static bclibs.utils.Opcodes.OpParameterType.*;

public class Opcodes {
	public static enum StackElementLength {
		ONE, // u1
		DOUBLE // u2
	}
	
	public static enum OpParameterType {
		U1,
		U2,
		U4,
		S1,
		S2,
		S4
	}
	
	public static abstract class Stack {
		//public void pop();
	}
	
	public static class DecodedOp {
		public final OpParameterType[] parameterTypes;
		public final int[] parameterValues;
		
		public DecodedOp(OpParameterType[] parameterTypes) {
			this.parameterTypes = parameterTypes;
			this.parameterValues = new int[parameterTypes.length];
		}
		
		public DecodedOp(OpParameterType[] parameterTypes, int[] parameterValues) {
			if(parameterTypes.length != parameterValues.length)
				throw new IllegalArgumentException();
			this.parameterTypes = parameterTypes;
			this.parameterValues = parameterValues;
		}
	}
	
	public static class DecodedLocalVariableOp extends DecodedOp {
		protected LocalVariable localVariable;
		protected boolean load;
		
		public DecodedLocalVariableOp(OpParameterType[] parameterTypes, int[] parameterValues) {
			super(parameterTypes, parameterValues);
		}
	}
	
	public static abstract class Op {
		protected final int code;
		protected final OpParameterType[] parameterTypes;
		private String name;
		public Op(int code, OpParameterType... opParameterTypes) {
			this.code = code;
			this.parameterTypes = opParameterTypes;
		}
		
		public abstract void simulate(Stack stack);
		public abstract DecodedOp decode(CtBehavior behavior, CodeIterator iterator, int index);
		
		public int getCode() {
			return code;
		}
		
		public OpParameterType[] getParameterTypes() {
			return parameterTypes;
		}
		
		public String getName() {
			if(name == null)
				name = findOpName(code);
			return name;
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
	
	public static class BasicOpcode extends Op {
		private StackElementLength[] pops, pushes;
		public BasicOpcode(int code, OpParameterType... opParameterTypes) {
			this(code, new StackElementLength[0], new StackElementLength[0], opParameterTypes);
		}
		public BasicOpcode(int code, StackElementLength[] pops, StackElementLength[] pushes, OpParameterType... opParameterTypes) {
			super(code, opParameterTypes);
			this.pops = pops;
			this.pushes = pushes;
		}
		public StackElementLength[] getPops() {
			return pops;
		}
		public StackElementLength[] getPushes() {
			return pushes;
		}
		@Override
		public DecodedOp decode(CtBehavior behavior, CodeIterator iterator, int index) {
			return new DecodedOp(parameterTypes, decodeValues(iterator, index));
		}
		@Override
		public void simulate(Stack stack) {
			// TODO
		}
		public BasicOpcode setPops(StackElementLength... pops) {
			if(pops != null)
				this.pops = pops;
			return this;
		}
		public BasicOpcode setPushes(StackElementLength... pushes) {
			if(pushes != null)
				this.pushes = pushes;
			return this;
		}
	}
	
	public static class LocalVariableOpcode extends BasicOpcode {
		private int base;
		private boolean load;
		public LocalVariableOpcode(int code, boolean load, OpParameterType... opParameterTypes) {
			this(code, code, load, new StackElementLength[0], new StackElementLength[0], opParameterTypes);
		}
		public LocalVariableOpcode(int code, int base, boolean load, OpParameterType... opParameterTypes) {
			this(code, base, load, new StackElementLength[0], new StackElementLength[0], opParameterTypes);
		}
		public LocalVariableOpcode(int code, int base, boolean load, StackElementLength[] pops, StackElementLength[] pushes, OpParameterType... opParameterTypes) {
			super(code, pops, pushes, opParameterTypes);
			this.base = base;
			this.load = load;
		}
		@Override
		public DecodedLocalVariableOp decode(CtBehavior behavior, CodeIterator iterator, int index) {
			DecodedLocalVariableOp result = new DecodedLocalVariableOp(parameterTypes, decodeValues(iterator, index));
			LocalVariable.findVariables(behavior);
			int slot;
			if(parameterTypes.length > 0)
				slot = result.parameterValues[0];
			else slot = code - base;
			result.load = load;
			result.localVariable = LocalVariable.getLocalVariable(slot, index, LocalVariable.findVariables(behavior));
			return result;
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
		opcodes.put(Opcode.ICONST_M1, new BasicOpcode(Opcode.ICONST_M1).setPushes(ONE));
		opcodes.put(Opcode.ICONST_0, new BasicOpcode(Opcode.ICONST_0).setPushes(ONE));
		opcodes.put(Opcode.ICONST_1, new BasicOpcode(Opcode.ICONST_1).setPushes(ONE));
		opcodes.put(Opcode.ICONST_2, new BasicOpcode(Opcode.ICONST_2).setPushes(ONE));
		opcodes.put(Opcode.ICONST_3, new BasicOpcode(Opcode.ICONST_3).setPushes(ONE));
		opcodes.put(Opcode.ICONST_4, new BasicOpcode(Opcode.ICONST_4).setPushes(ONE));
		opcodes.put(Opcode.ICONST_5, new BasicOpcode(Opcode.ICONST_5).setPushes(ONE));
		opcodes.put(Opcode.LCONST_0, new BasicOpcode(Opcode.LCONST_0).setPushes(DOUBLE));
		opcodes.put(Opcode.LCONST_1, new BasicOpcode(Opcode.LCONST_1).setPushes(DOUBLE));
		opcodes.put(Opcode.FCONST_0, new BasicOpcode(Opcode.FCONST_0).setPushes(ONE));
		opcodes.put(Opcode.FCONST_1, new BasicOpcode(Opcode.FCONST_1).setPushes(ONE));
		opcodes.put(Opcode.FCONST_2, new BasicOpcode(Opcode.FCONST_2).setPushes(ONE));
		opcodes.put(Opcode.DCONST_0, new BasicOpcode(Opcode.DCONST_0).setPushes(DOUBLE));
		opcodes.put(Opcode.DCONST_1, new BasicOpcode(Opcode.DCONST_1).setPushes(DOUBLE));
		opcodes.put(Opcode.BIPUSH, new BasicOpcode(Opcode.BIPUSH, S1).setPushes(ONE));
		opcodes.put(Opcode.SIPUSH, new BasicOpcode(Opcode.SIPUSH, S2).setPushes(ONE));
		opcodes.put(Opcode.LDC, new BasicOpcode(Opcode.LDC, U1).setPushes(ONE));
		opcodes.put(Opcode.LDC_W, new BasicOpcode(Opcode.LDC_W, U2).setPushes(ONE));
		opcodes.put(Opcode.LDC2_W, new BasicOpcode(Opcode.LDC2_W, U2).setPushes(DOUBLE));
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
		opcodes.put(Opcode.IALOAD, new BasicOpcode(Opcode.IALOAD).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.LALOAD, new BasicOpcode(Opcode.LALOAD).setPops(ONE, ONE).setPushes(DOUBLE));
		opcodes.put(Opcode.FALOAD, new BasicOpcode(Opcode.FALOAD).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.DALOAD, new BasicOpcode(Opcode.DALOAD).setPops(ONE, ONE).setPushes(DOUBLE));
		opcodes.put(Opcode.AALOAD, new BasicOpcode(Opcode.AALOAD).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.BALOAD, new BasicOpcode(Opcode.BALOAD).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.CALOAD, new BasicOpcode(Opcode.CALOAD).setPops(ONE, ONE).setPushes(ONE));
		opcodes.put(Opcode.SALOAD, new BasicOpcode(Opcode.SALOAD).setPops(ONE, ONE).setPushes(ONE));
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
		opcodes.put(Opcode.IASTORE, new BasicOpcode(Opcode.IASTORE).setPops(ONE, ONE, ONE));
		opcodes.put(Opcode.LASTORE, new BasicOpcode(Opcode.LASTORE).setPops(DOUBLE, ONE, ONE));
		opcodes.put(Opcode.FASTORE, new BasicOpcode(Opcode.FASTORE).setPops(ONE, ONE, ONE));
		opcodes.put(Opcode.DASTORE, new BasicOpcode(Opcode.DASTORE).setPops(DOUBLE, ONE, ONE));
		opcodes.put(Opcode.AASTORE, new BasicOpcode(Opcode.AASTORE).setPops(ONE, ONE, ONE));
		opcodes.put(Opcode.BASTORE, new BasicOpcode(Opcode.BASTORE).setPops(ONE, ONE, ONE));
		opcodes.put(Opcode.CASTORE, new BasicOpcode(Opcode.CASTORE).setPops(ONE, ONE, ONE));
		opcodes.put(Opcode.SASTORE, new BasicOpcode(Opcode.SASTORE).setPops(ONE, ONE, ONE));
		opcodes.put(Opcode.POP, new BasicOpcode(Opcode.POP).setPops(ONE));
		opcodes.put(Opcode.POP2, new BasicOpcode(Opcode.POP2).setPops(DOUBLE));
		// DUP
		// DUP_X1
		// DUP_X2
		opcodes.put(Opcode.DUP2, new BasicOpcode(Opcode.DUP2).setPops(DOUBLE).setPushes(DOUBLE, DOUBLE));
		// DUP
		// DUP2_X1
		// DUP2_X2
		// SWAP
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
		opcodes.put(Opcode.IFEQ, new BasicOpcode(Opcode.IFEQ, S2).setPops(ONE));
		opcodes.put(Opcode.IFNE, new BasicOpcode(Opcode.IFNE, S2).setPops(ONE));
		opcodes.put(Opcode.IFLT, new BasicOpcode(Opcode.IFLT, S2).setPops(ONE));
		opcodes.put(Opcode.IFGE, new BasicOpcode(Opcode.IFGE, S2).setPops(ONE));
		opcodes.put(Opcode.IFGT, new BasicOpcode(Opcode.IFGT, S2).setPops(ONE));
		opcodes.put(Opcode.IFLE, new BasicOpcode(Opcode.IFLE, S2).setPops(ONE));
		opcodes.put(Opcode.IF_ICMPEQ, new BasicOpcode(Opcode.IF_ICMPEQ, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ICMPNE, new BasicOpcode(Opcode.IF_ICMPNE, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ICMPLT, new BasicOpcode(Opcode.IF_ICMPLT, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ICMPGE, new BasicOpcode(Opcode.IF_ICMPGE, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ICMPGT, new BasicOpcode(Opcode.IF_ICMPGT, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ICMPLE, new BasicOpcode(Opcode.IF_ICMPLE, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ACMPEQ, new BasicOpcode(Opcode.IF_ACMPEQ, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.IF_ACMPNE, new BasicOpcode(Opcode.IF_ACMPNE, S2).setPops(ONE, ONE));
		opcodes.put(Opcode.GOTO, new BasicOpcode(Opcode.GOTO, S2));
		opcodes.put(Opcode.JSR, new BasicOpcode(Opcode.JSR, S2));
		opcodes.put(Opcode.RET, new BasicOpcode(Opcode.RET, U1));
		// TABLESWITCH
		// LOOKUPSWITCH
		opcodes.put(Opcode.IRETURN, new BasicOpcode(Opcode.IRETURN).setPops(ONE));
		opcodes.put(Opcode.LRETURN, new BasicOpcode(Opcode.LRETURN).setPops(DOUBLE));
		opcodes.put(Opcode.FRETURN, new BasicOpcode(Opcode.FRETURN).setPops(ONE));
		opcodes.put(Opcode.DRETURN, new BasicOpcode(Opcode.DRETURN).setPops(DOUBLE));
		opcodes.put(Opcode.ARETURN, new BasicOpcode(Opcode.ARETURN).setPops(ONE));
		opcodes.put(Opcode.RETURN, new BasicOpcode(Opcode.RETURN));
		// GETSTATIC
		// PUTSTATIC
		// GETFIELD
		// PUTFIELD
		// INVOKEVIRTUAL
		// INVOKESPECIAL
		// INVOKESTATIC
		// INVOKEINTERFACE
		opcodes.put(Opcode.NEW, new BasicOpcode(Opcode.NEW, U2).setPushes(ONE));
		opcodes.put(Opcode.NEWARRAY, new BasicOpcode(Opcode.NEWARRAY, U1).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.ANEWARRAY, new BasicOpcode(Opcode.ANEWARRAY, U2).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.ARRAYLENGTH, new BasicOpcode(Opcode.ARRAYLENGTH).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.ATHROW, new BasicOpcode(Opcode.ATHROW).setPops(ONE));
		opcodes.put(Opcode.CHECKCAST, new BasicOpcode(Opcode.CHECKCAST, U2).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.INSTANCEOF, new BasicOpcode(Opcode.INSTANCEOF, U2).setPops(ONE).setPushes(ONE));
		opcodes.put(Opcode.MONITORENTER, new BasicOpcode(Opcode.MONITORENTER).setPops(ONE));
		opcodes.put(Opcode.MONITOREXIT, new BasicOpcode(Opcode.MONITOREXIT).setPops(ONE));
		opcodes.put(Opcode.WIDE, new BasicOpcode(Opcode.WIDE));
		// MULTINEWARRAY
		opcodes.put(Opcode.IFNULL, new BasicOpcode(Opcode.IFNULL, S2).setPops(ONE));
		opcodes.put(Opcode.IFNONNULL, new BasicOpcode(Opcode.IFNONNULL, S2).setPops(ONE));
		opcodes.put(Opcode.GOTO_W, new BasicOpcode(Opcode.GOTO_W, S4));
		opcodes.put(Opcode.JSR_W, new BasicOpcode(Opcode.JSR_W, S4).setPushes(ONE));
		
		OPCODES = Collections.unmodifiableMap(opcodes);
	}
}
