package bclibs.analysis;

import java.util.Map;

import bclibs.LocalVariable;
import javassist.CtBehavior;
import javassist.NotFoundException;
import javassist.bytecode.CodeIterator;

public class Context {
	public final CtBehavior behavior;
	public final CodeIterator iterator;
	public final Map<Integer, LocalVariable> localVariables;
	
	public Context(CtBehavior behavior, CodeIterator iterator, Map<Integer, LocalVariable> localVariables) {
		this.behavior = behavior;
		this.iterator = iterator;
		this.localVariables = localVariables;
	}
	
	public Context(CtBehavior behavior, CodeIterator iterator) {
		this(behavior, iterator, findLocalVariables(behavior));
	}
	
	private static Map<Integer, LocalVariable> findLocalVariables(CtBehavior behavior) {
		try {
			return LocalVariable.findVariables(behavior);
		} catch (NotFoundException e) {
			throw new RuntimeException("Error while retrieving the behavior's local variables!", e);
		}
	}
}
