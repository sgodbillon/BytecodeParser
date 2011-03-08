/**
 * 
 */
package bclibs.analysis;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import bclibs.analysis.opcodes.Op;

public class CodeParser {
	public final Context context;
	
	public CodeParser(CtBehavior behavior) {
		this.context = new Context(behavior, behavior.getMethodInfo().getCodeAttribute().iterator());
	}
	public void parse(OpHandler opHandler) throws BadBytecode {
		context.iterator.begin();
		while(context.iterator.hasNext()) {
			int index = context.iterator.next();
			Op op = Opcodes.OPCODES.get(context.iterator.byteAt(index)).init(context, index);
			opHandler.handle(op, index);
		}
	}
}