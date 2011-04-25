/**
 * 
 */
package bytecodeparser.analysis.stack;

import java.util.Arrays;

import bytecodeparser.analysis.stack.Stack.StackElementLength;


public class TrackableArray extends Array {
	public final StackElement[] elements;
	public final StackElementLength componentLength;
	public boolean isDirty;
	public TrackableArray(String signature, int size) {
		this(signature, makeNewArray(size), parseSignature(signature));
	}
	public TrackableArray(String signature, StackElement[] elements, StackElementLength componentLength) {
		super(signature);
		this.elements = elements;
		this.componentLength = componentLength;
	}
	@Override
	public Array copy() {
		return this;
	}
	public TrackableArray set(int i, StackElement element) {
		elements[i] = element;
		return this;
	}
	@Override
	public String toString() {
		return "TrackableArray of '" + signature + "'";
	}
	private static StackElement[] makeNewArray(int size) {
		StackElement[] result = new StackElement[size];
		Arrays.fill(result, new VoidElement());
		return result;
	}
	
	private static StackElementLength parseSignature(String signature) {
		return StackElementLength.ONE;
	}
}