package bclibs.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javassist.bytecode.Opcode;

public class StackOpcodes {

	public static Map<Integer, Integer> pushes = new HashMap<Integer, Integer>();
	public static Map<Integer, Integer> pops = new HashMap<Integer, Integer>();
	
	static {
		pushes.put(Opcode.BIPUSH, 1);
		pushes.put(Opcode.SIPUSH, 1);
		pushes.put(Opcode.LDC, 1);
		pushes.put(Opcode.LDC_W, 1);
		pushes.put(Opcode.LDC2_W, 2);
		pushes.put(Opcode.ACONST_NULL, 1);
		pushes.put(Opcode.ICONST_M1, 1);
		pushes.put(Opcode.ICONST_0, 1);
		pushes.put(Opcode.ICONST_1, 1);
		pushes.put(Opcode.ICONST_2, 1);
		pushes.put(Opcode.ICONST_3, 1);
		pushes.put(Opcode.ICONST_4, 1);
		pushes.put(Opcode.ICONST_5, 1);
		pushes.put(Opcode.LCONST_0, 2);
		pushes.put(Opcode.LCONST_1, 2);
		pushes.put(Opcode.FCONST_0, 1);
		pushes.put(Opcode.FCONST_1, 1);
		pushes.put(Opcode.FCONST_2, 1);
		pushes.put(Opcode.DCONST_0, 2);
		pushes.put(Opcode.DCONST_1, 2);
		
		pushes.put(Opcode.DUP, 1);
		pushes.put(Opcode.DUP, 2);
		
		pushes.put(Opcode.NOP, 0);
		
		pops.put(Opcode.POP, 1);
		pops.put(Opcode.POP2, 2);
	}
}
