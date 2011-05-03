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
		System.out.println("in trackable array, set " + i + " => " + element);
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