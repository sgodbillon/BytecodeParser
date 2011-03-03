package bclibs.utils;

import java.util.HashMap;
import java.util.Map;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;

public class LocalVariableOpcodes {
	private static Map<Integer, Integer> storeByCode = new HashMap<Integer, Integer>();
	private static Map<Integer, Integer> loadByCode = new HashMap<Integer, Integer>();
	
	public static enum LocalVariableOpType {
		STORE,
		LOAD
	}
	
	public static class LocalVariableOp {
		public LocalVariableOpType type;
		public int varIndex;
	}
	
	public static LocalVariableOp getLocalVariableOp(final CodeAttribute codeAttribute, int index) throws BadBytecode {
		CodeIterator it = codeAttribute.iterator();
		it.move(index);
		it.next();
		
		LocalVariableOpType type = LocalVariableOpType.STORE;
		Integer varIndex = storeByCode.get(it.byteAt(index));
		if(varIndex == null) {
			varIndex = loadByCode.get(it.byteAt(index));
			type = LocalVariableOpType.LOAD;
		}
		if(varIndex != null) {
			if(varIndex == -2)
				varIndex = it.byteAt(index + 1);
			LocalVariableOp result = new LocalVariableOp();
			result.type = type;
			result.varIndex = varIndex;
			return result;
		}
		return null;
	}
	
	static {
		storeByCode.put(Opcode.ASTORE_0, 0);
		storeByCode.put(Opcode.ASTORE_1, 1);
		storeByCode.put(Opcode.ASTORE_2, 2);
		storeByCode.put(Opcode.ASTORE_3, 3);
		storeByCode.put(Opcode.ASTORE, -2);

		storeByCode.put(Opcode.ISTORE_0, 0);
		storeByCode.put(Opcode.ISTORE_1, 1);
		storeByCode.put(Opcode.ISTORE_2, 2);
		storeByCode.put(Opcode.ISTORE_3, 3);
		storeByCode.put(Opcode.ISTORE, -2);
		storeByCode.put(Opcode.IINC, -2);

		storeByCode.put(Opcode.LSTORE_0, 0);
		storeByCode.put(Opcode.LSTORE_1, 1);
		storeByCode.put(Opcode.LSTORE_2, 2);
		storeByCode.put(Opcode.LSTORE_3, 3);
		storeByCode.put(Opcode.LSTORE, -2);

		storeByCode.put(Opcode.FSTORE_0, 0);
		storeByCode.put(Opcode.FSTORE_1, 1);
		storeByCode.put(Opcode.FSTORE_2, 2);
		storeByCode.put(Opcode.FSTORE_3, 3);
		storeByCode.put(Opcode.FSTORE, -2);

		storeByCode.put(Opcode.DSTORE_0, 0);
		storeByCode.put(Opcode.DSTORE_1, 1);
		storeByCode.put(Opcode.DSTORE_2, 2);
		storeByCode.put(Opcode.DSTORE_3, 3);
		storeByCode.put(Opcode.DSTORE, -2);
		
		// loads
		loadByCode.put(Opcode.ALOAD_0, 0);
		loadByCode.put(Opcode.ALOAD_1, 1);
		loadByCode.put(Opcode.ALOAD_2, 2);
		loadByCode.put(Opcode.ALOAD_3, 3);
		loadByCode.put(Opcode.ALOAD, -2);
		
		loadByCode.put(Opcode.ILOAD_0, 0);
		loadByCode.put(Opcode.ILOAD_1, 1);
		loadByCode.put(Opcode.ILOAD_2, 2);
		loadByCode.put(Opcode.ILOAD_3, 3);
		loadByCode.put(Opcode.ILOAD, -2);

		loadByCode.put(Opcode.LLOAD_0, 0);
		loadByCode.put(Opcode.LLOAD_1, 1);
		loadByCode.put(Opcode.LLOAD_2, 2);
		loadByCode.put(Opcode.LLOAD_3, 3);
		loadByCode.put(Opcode.LLOAD, -2);

		loadByCode.put(Opcode.FLOAD_0, 0);
		loadByCode.put(Opcode.FLOAD_1, 1);
		loadByCode.put(Opcode.FLOAD_2, 2);
		loadByCode.put(Opcode.FLOAD_3, 3);
		loadByCode.put(Opcode.FLOAD, -2);

		loadByCode.put(Opcode.DLOAD_0, 0);
		loadByCode.put(Opcode.DLOAD_1, 1);
		loadByCode.put(Opcode.DLOAD_2, 2);
		loadByCode.put(Opcode.DLOAD_3, 3);
		loadByCode.put(Opcode.DLOAD, -2);
	}
}
