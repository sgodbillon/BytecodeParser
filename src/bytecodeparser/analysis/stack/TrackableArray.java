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

import org.apache.log4j.Logger;

import bytecodeparser.analysis.stack.Stack.StackElementLength;

/**
 * A trackable Array is an array of known elements at compile time: its content can be "guessed" by the analyzer.
 * When this array gets updated with a value that is unknown at compile-time, this value is an instance of WhateverElement.
 * 
 * This is particularly useful to guess the names of the varargs, for example.
 * 
 * If a trackable array contains some elements that cannot be guessed
 * 
 * @author Stephane Godbillon
 *
 */
public class TrackableArray extends Array {
	private static final Logger LOGGER = Logger.getLogger(TrackableArray.class);
	
	/**
	 * The StackElements of this array.
	 */
	public final StackElement[] elements;
	/**
	 * The length of each component.
	 */
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
		LOGGER.trace("in trackable array, set " + i + " => " + element);
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