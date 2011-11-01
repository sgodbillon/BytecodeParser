/*
 *  Copyright (C) 2011 Stephane Godbillon
 *  
 *  This file is part of BytecodeParser. See the README file in the root
 *  directory of this project.
 *
 *  BytecodeParser is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  BytecodeParser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with BytecodeParser.  If not, see <http://www.gnu.org/licenses/>.
 */
package bytecodeparser.analysis.stack;

import static bytecodeparser.analysis.stack.Stack.StackElementLength.DOUBLE;

import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * Represents the current stack's state.
 * 
 * @author Stephane Godbillon
 *
 */
public class Stack {
	private static final Logger LOGGER = Logger.getLogger(Stack.class);
	
	/**
	 * Length of a stack element. Can be one or two words.
	 * @author Stephane Godbillon
	 *
	 */
	public static enum StackElementLength {
		/**
		 * One word length
		 */
		ONE, // one word
		/**
		 * Two words length
		 */
		DOUBLE; // two words
		
		/**
		 * Computes the total length of the stackElementLengths.
		 * @param stackElementLengths
		 * @return The total length of the stackElementLengths.
		 */
		public static int add(StackElementLength... stackElementLengths) {
			int result = 0;
			for(StackElementLength sel : stackElementLengths) {
				result += sel == ONE ? 1 : 2;
			}
			return result;
		}
	}
	
	/**
	 * Linked List holding the references of this stack's elements.
	 * It is preferable to use the methods of this class instead of accessing this field.
	 */
	public LinkedList<StackElement> stack = new LinkedList<StackElement>();
	
	/**
	 * States if this stack is empty.
	 * @return true if this stack is empty, false if not.
	 */
	public boolean isEmpty() {
		return stack.size() == 0;
	}
	
	/**
	 * Removes the top stackElement from this stack.
	 * The stackElement must be one-word length.
	 * @throws java.util.NoSuchElementException if this stack is empty.
	 * @throws RuntimeException if the stackElement is a part of a two-words element.
	 * @return the removed stackElement.
	 */
	public StackElement pop() {
		StackElement se = stack.pop();
		if(se instanceof TOP)
			throw new RuntimeException("WARN: popped a TOP!");
		return se;
	}
	
	/**
	 * Removes the top stackElement from this stack.
	 * The stackElement must be two-words length.
	 * @throws java.util.NoSuchElementException if this stack is empty.
	 * @throws RuntimeException if the stackElement is not a two-words element.
	 * @return the removed stackElement.
	 */
	public StackElement pop2() {
		StackElement se = stack.pop();
		if( !(se instanceof TOP) )
			throw new RuntimeException("WARN: popped2 top is not a TOP! (is instanceof " + se.getClass() + ")");
		se = stack.pop();
		if(se instanceof TOP)
			throw new RuntimeException("WARN: popped2 a TOP!");
		return se;
	}
	
	/**
	 * Removes the top stackElement of the given length from this stack.
	 * @throws java.util.NoSuchElementException if this stack is empty.
	 * @throws RuntimeException if the top stackElement is not of the given length.
	 * @return the removed stackElement.
	 */
	public StackElement pop(StackElementLength length) {
		if(length == StackElementLength.DOUBLE)
			return pop2();
		return pop();
	}
	
	/**
	 * Returns the top stackElement from this stack.
	 * The stackElement might be a a TOP (part of a two-words element).
	 * @throws java.util.NoSuchElementException if this stack is empty.
	 * @return the stackElement.
	 */
	public StackElement peek() {
		StackElement se = stack.peek();
		if(se instanceof TOP)
			LOGGER.warn("WARN: popped a TOP!");
		return se;
	}
	
	/**
	 * Returns the top-1 stackElement from this stack.
	 * The stackElement might be a a TOP (part of a two-words element).
	 * @throws java.util.NoSuchElementException if this stack is empty.
	 * @return the stackElement.
	 */
	public StackElement peek2() {
		StackElement se = stack.get(stack.size() - 2);
		if(se instanceof TOP)
			LOGGER.warn("WARN: peek2 a TOP!");
		return se;
	}
	
	/**
	 * Returns the top or top-1 stackElement from this stack, depending on the given length.
	 * The stackElement might be a a TOP (part of a two-words element).
	 * @throws java.util.NoSuchElementException if this stack is empty.
	 * @return the stackElement.
	 */
	public StackElement peek(StackElementLength length) {
		if(length == StackElementLength.DOUBLE)
			return peek2();
		return peek();
	}
	
	/**
	 * Pushes the given one-word stackElement on the stack.
	 * @param se The element to push on.
	 * @return the current Stack instance for chaining.
	 */
	public Stack push(StackElement se) {
		stack.push(se);
		return this;
	}
	
	/**
	 * Pushes the given two-words stackElement on the stack.
	 * @param se The element to push on.
	 * @return the current Stack instance for chaining.
	 */
	public Stack push2(StackElement se) {
		stack.push(se);
		stack.push(new TOP());
		return this;
	}
	
	/**
	 * Get the n-ith element from the stack.
	 * @param i
	 * @return the stackElement. Can be a TOP (part of a two-words element).
	 */
	public StackElement getFromTop(int i) {
		return stack.get(i);
	}
	
	/**
	 * Makes a copy of this Stack instance.
	 * @return a new Stack instance containing the same elements.
	 */
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
	
	/**
	 * Pops some elements from the given stack, then pushes some Whatever elements onto it.
	 * @param stack
	 * @param pops
	 * @param pushes
	 */
	// TODO
	public static void processBasicAlteration(Stack stack, StackElementLength[] pops, StackElementLength[] pushes) {
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