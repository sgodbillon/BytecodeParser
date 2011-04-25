/**
 * 
 */
package bytecodeparser.analysis.stack;

import static bytecodeparser.analysis.stack.Stack.StackElementLength.DOUBLE;

import java.util.Arrays;
import java.util.LinkedList;

public class Stack {
	public static enum StackElementLength {
		ONE, // one word
		DOUBLE; // two words
		
		public static int add(StackElementLength... stackElementLengths) {
			int result = 0;
			for(StackElementLength sel : stackElementLengths) {
				result += sel == ONE ? 1 : 2;
			}
			return result;
		}
	}
	
	public LinkedList<StackElement> stack = new LinkedList<StackElement>();
	
	public boolean isEmpty() {
		return stack.size() == 0;
	}
	
	public StackElement pop() {
		StackElement se = stack.pop();
		if(se instanceof TOP)
			throw new RuntimeException("WARN: popped a TOP!");
		return se;
	}
	
	public StackElement pop2() {
		StackElement se = stack.pop();
		if( !(se instanceof TOP) )
			throw new RuntimeException("WARN: popped2 top is not a TOP! (is instanceof " + se.getClass() + ")");
		se = stack.pop();
		if(se instanceof TOP)
			throw new RuntimeException("WARN: popped2 a TOP!");
		return se;
	}
	
	public StackElement pop(StackElementLength length) {
		if(length == StackElementLength.DOUBLE)
			return pop2();
		return pop();
	}
	
	public StackElement peek() {
		StackElement se = stack.peek();
		if(se instanceof TOP)
			System.out.println("WARN: popped a TOP!");
		return se;
	}
	
	public StackElement peek2() {
		StackElement se = stack.get(stack.size() - 2);
		if(se instanceof TOP)
			System.out.println("WARN: peek2 a TOP!");
		return se;
	}
	
	public StackElement peek(StackElementLength length) {
		if(length == StackElementLength.DOUBLE)
			return peek2();
		return peek();
	}
	
	public Stack push(StackElement se) {
		stack.push(se);
		return this;
	}
	
	public Stack push2(StackElement se) {
		stack.push(se);
		stack.push(new TOP());
		return this;
	}
	
	public StackElement getFromTop(int i) {
		return stack.get(i);
	}
	
	public Stack copy() {
		Stack copy = new Stack();
		copy.stack = new LinkedList<StackElement>(this.stack);
		return copy;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("stack: [");
		for(int i = 0; i < stack.size(); i++) {
			if(i > 0)
				sb.append(", ");
			sb.append(stack.get(i));
		}
		return sb.append("]").toString();
	}
	
	public static void processBasicAlteration(Stack stack, StackElementLength[] pops, StackElementLength[] pushes) {
		System.out.println("process basic alteration with pops=" + Arrays.toString(pops) + " and pushes=" + Arrays.toString(pushes) + " on stack " + stack);
		for(int i = 0; i < pops.length; i++) {
			if(pops[i] == DOUBLE)
				stack.pop2();
			else stack.pop();
		}
		for(int i = 0; i < pushes.length; i++) {
			if(pushes[i] == DOUBLE)
				stack.push2(new Whatever());
			else stack.push(new Whatever());
		}
	}
}