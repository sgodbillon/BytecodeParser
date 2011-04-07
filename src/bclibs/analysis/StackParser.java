/**
 * 
 */
package bclibs.analysis;

import java.util.LinkedList;

import javassist.CtBehavior;
import javassist.bytecode.BadBytecode;
import bclibs.analysis.opcodes.BranchOpCode;
import bclibs.analysis.opcodes.ExitOpcode;
import bclibs.analysis.opcodes.Op;
import bclibs.analysis.stack.Stack;
import bclibs.analysis.stack.StackElement;
import bclibs.analysis.stack.Whatever;

public class StackParser {
	public final Context context;
	final Stack stack;
	final Frame[] frames;
	
	public StackParser(CtBehavior behavior) {
		this.context = new Context(behavior);
		this.stack = new Stack();
		this.frames = new Frame[context.behavior.getMethodInfo().getCodeAttribute().getCodeLength()];
	}
	
	public Frame[] parse() throws BadBytecode {
		if(frames[0] == null) {
			init();
			parse(0, new Stack());
			parseCatchBlocks();
		}
		return frames;
	}
	
	void init() throws BadBytecode {
		new CodeParser(context.behavior).parse(new OpHandler() {
			@Override
			public void handle(Op op, int index) {
				Frame frame = frames[index] = new Frame();
				frame.index = index;
				frame.op = op;
				//System.out.println("init: " + frame);
			}
		});
	}
	
	void parseCatchBlocks() throws BadBytecode {
		for(int index : context.exceptionHandlers) {
			//System.out.println("parse catch block " + index + ", " + frames[index]);
			parse(index, new Stack().push(new Whatever()));
		}
	}
	
	void parse(int from, Stack stack) throws BadBytecode {
		//System.out.println("parse from " + from + " with stack " + stack);
		if(frames[from].isAccessible) // already parsed
			return;
		context.iterator.move(from);
		final Stack[] currentStack = new Stack[] { stack };
		while(context.iterator.hasNext()) {
			int index = context.iterator.next();
			Op op = Opcodes.OPCODES.get(context.iterator.byteAt(index)).init(context, index);
			Frame frame = frames[index];
			frame.isAccessible = true;
			frame.stackBefore = currentStack[0].copy();
			op.simulate(currentStack[0], context, index);
			frame.stackAfter = currentStack[0].copy();
			if(op instanceof ExitOpcode) {
				break;
			}
			if(op instanceof BranchOpCode) {
				BranchOpCode branchOpCode = op.as(BranchOpCode.class);
				try {
					int jump = branchOpCode.decode(context, index).getJump();
					//System.out.println(op + " will jump to " + jump);
					if(branchOpCode.isConditional())
						parse(context.iterator.lookAhead(), frame.stackAfter);
					parse(jump, frame.stackAfter);
				} catch (BadBytecode b) {
					throw new RuntimeException(b);
				}
				break;
			}
		}
	}
	
	public LinkedList<StackElement> getCurrentStack() {
		return stack.stack;
	}
	
	public static class Frame {
		public Stack stackBefore;
		public Stack stackAfter;
		public int index;
		public Op op;
		public boolean isAccessible = false;
		
		@Override
		public String toString() {
			return "Frame " + index + " (" + op.getName() + "):" + stackBefore + " -> " + stackAfter + " " + (isAccessible ? "" : " NOT ACCESSIBLE");
		}
	}
}