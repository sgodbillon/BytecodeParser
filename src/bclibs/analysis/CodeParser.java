/**
 * 
 */
package bclibs.analysis;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import bclibs.analysis.opcodes.Op;

public class CodeParser {
	public final CtBehavior behavior;
	public final CodeIterator iterator;
	public CodeParser(CtBehavior behavior) {
		this.behavior = behavior;
		this.iterator = behavior.getMethodInfo().getCodeAttribute().iterator();
	}
	public void parse(OpHandler opHandler) throws BadBytecode {
		iterator.begin();
		while(iterator.hasNext()) {
			int index = iterator.next();
			Op op = Opcodes.OPCODES.get(iterator.byteAt(index)).init(behavior, iterator, index);
			opHandler.handle(op, index);
		}
	}
}