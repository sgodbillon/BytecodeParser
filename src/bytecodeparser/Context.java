package bytecodeparser;

import java.util.Map;

import javassist.CtBehavior;
import javassist.NotFoundException;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ExceptionTable;
import bytecodeparser.analysis.LocalVariable;

public class Context {
	public final CtBehavior behavior;
	public final CodeIterator iterator;
	public final Map<Integer, LocalVariable> localVariables;
	public final int[] exceptionHandlers;
	
	public Context(CtBehavior behavior, CodeIterator iterator, Map<Integer, LocalVariable> localVariables) {
		this.behavior = behavior;
		this.iterator = iterator;
		this.localVariables = localVariables;
		ExceptionTable exceptionTable = this.behavior.getMethodInfo().getCodeAttribute().getExceptionTable();
		
		if(exceptionTable != null) {
			this.exceptionHandlers = new int[exceptionTable.size()];
			for(int i = 0; i < exceptionTable.size(); i++) {
				exceptionHandlers[i] = exceptionTable.handlerPc(i);
			}
		} else {
			this.exceptionHandlers = new int[0];
		}
	}
	
	public Context(CtBehavior behavior, CodeIterator iterator) {
		this(behavior, iterator, findLocalVariables(behavior));
	}
	
	public Context(CtBehavior behavior) {
		this(behavior, behavior.getMethodInfo().getCodeAttribute().iterator(), findLocalVariables(behavior));
	}
	
	public boolean isStartOfExceptionHandler(int index) {
		for(int i = 0; i < exceptionHandlers.length; i++)
			if(exceptionHandlers[i] == index)
				return true;
		return false;
	}
	
	private static Map<Integer, LocalVariable> findLocalVariables(CtBehavior behavior) {
		try {
			return LocalVariable.findVariables(behavior);
		} catch (NotFoundException e) {
			throw new RuntimeException("Error while retrieving the behavior's local variables!", e);
		}
	}
}
