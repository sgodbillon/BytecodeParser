/**
 * 
 */
package bclibs.analysis;

import java.util.LinkedList;

import javassist.bytecode.BadBytecode;
import bclibs.analysis.opcodes.Op;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.StackElement;

public class StackParser {
	public final CodeParser parser;
	final Stack stack;
	
	public StackParser(CodeParser parser) {
		this.parser = parser;
		this.stack = new Stack();
	}
	
	public void parse(final StackOpHandler stackOpHandler) throws BadBytecode {
		parser.parse(new OpHandler() {
			@Override
			public void handle(Op op, int index) {
				stackOpHandler.beforeComputeStack(op, index);
				op.simulate(stack, parser.behavior, parser.iterator, index);
				stackOpHandler.afterComputeStack(op, index);
			}
		});
	}
	
	public LinkedList<StackElement> getCurrentStack() {
		return stack.stack;
	}
}