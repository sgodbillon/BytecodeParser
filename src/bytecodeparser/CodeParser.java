/**
 * 
 */
package bytecodeparser;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import bytecodeparser.analysis.Opcodes;
import bytecodeparser.analysis.opcodes.Op;

public class CodeParser {
	public final Context context;
	private boolean stop = false;
	
	public CodeParser(CtBehavior behavior) {
		this.context = new Context(behavior, behavior.getMethodInfo().getCodeAttribute().iterator());
	}
	public void parse(OpHandler opHandler) throws BadBytecode {
		while(context.iterator.hasNext()) {
			if(stop)
				break;
			int index = context.iterator.next();
			Op op = Opcodes.OPCODES.get(context.iterator.byteAt(index)).init(context, index);
			opHandler.handle(op, index);
		}
	}
	
	public void move(int index) {
		stop = false;
		context.iterator.move(index);
	}
	
	public int nextIndex() {
		return context.iterator.lookAhead();
	}
	
	public void begin() {
		stop = false;
		context.iterator.begin();
	}
	
	public void stop() {
		stop = true;
	}
}