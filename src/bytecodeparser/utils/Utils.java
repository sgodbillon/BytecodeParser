package bytecodeparser.utils;

import javassist.CtBehavior;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;

public class Utils {
	public static LocalVariableAttribute getLocalVariableAttribute(CtBehavior behavior) {
		return (LocalVariableAttribute) behavior.getMethodInfo().getCodeAttribute().getAttribute("LocalVariableTable");
	}
	public static LineNumberAttribute getLineNumberAttribute(CtBehavior behavior) {
		return (LineNumberAttribute) behavior.getMethodInfo().getCodeAttribute().getAttribute("LineNumberTable");
	}
	public static ConstPool getConstPool(CtBehavior behavior) {
		return behavior.getMethodInfo().getConstPool();
	}
	public static void debugExceptionTable(ExceptionTable et, ConstPool cp) {
		for(int i = 0; i < et.size(); i++) {
			System.out.println(et.startPc(i) + " -> " + et.endPc(i) + " : " + et.handlerPc(i) + ", type=" + (et.catchType(i) != 0 ? cp.getClassInfo(et.catchType(i)) : "any"));
		}
	}
}
