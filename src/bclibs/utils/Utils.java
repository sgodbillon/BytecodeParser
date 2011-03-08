package bclibs.utils;

import javassist.CtBehavior;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;

public class Utils {
	public static LocalVariableAttribute getLocalVariableAttribute(CtBehavior behavior) {
		return (LocalVariableAttribute) behavior.getMethodInfo().getCodeAttribute().getAttribute("LocalVariableTable");
	}
	public static LineNumberAttribute getLineNumberAttribute(CtBehavior behavior) {
		return (LineNumberAttribute) behavior.getMethodInfo().getCodeAttribute().getAttribute("LineNumberTable");
	}
}
