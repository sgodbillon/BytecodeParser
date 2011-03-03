package bclibs.utils;

import java.lang.reflect.Field;
import java.util.Map;

import javassist.CtBehavior;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Opcode;

public class Utils {
	public static LocalVariableAttribute getLocalVariableAttribute(CtBehavior behavior) {
		return (LocalVariableAttribute) behavior.getMethodInfo().getCodeAttribute().getAttribute("LocalVariableTable");
	}
	public static LineNumberAttribute getLineNumberAttribute(CtBehavior behavior) {
		return (LineNumberAttribute) behavior.getMethodInfo().getCodeAttribute().getAttribute("LineNumberTable");
	}
	/*
	public static int prevIndex(CodeIterator it) {
		CodeIterator codeIterator = it.get().iterator();
		it.lookAhead();
		Opcode.
	}
	
	public static boolean isOpcode(int op) {
		for(Field field : Opcode.class.getFields()) {
			if(!"class".equals(field.getName())) {
				try {
					field.getInt(null);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}*/
}
