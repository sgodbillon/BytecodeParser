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

public abstract class Constant<T> extends StackElement {
	protected final T value;

	public Constant(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + value;
	}
	
	public static class StringConstant extends Constant<String> {
		public StringConstant(String value) {
			super(value);
		}
		@Override
		public StackElement copy() {
			return new StringConstant(value);
		}
	}

	public static class IntegerConstant extends Constant<Integer> {
		public IntegerConstant(Integer value) {
			super(value);
		}
		@Override
		public StackElement copy() {
			return new IntegerConstant(value);
		}
	}
	
	public static class FloatConstant extends Constant<Float> {
		public FloatConstant(Float value) {
			super(value);
		}
		@Override
		public StackElement copy() {
			return new FloatConstant(value);
		}
	}
	
	public static class LongConstant extends Constant<Long> {
		public LongConstant(Long value) {
			super(value);
		}
		@Override
		public StackElement copy() {
			return new LongConstant(value);
		}
	}
	
	public static class DoubleConstant extends Constant<Double> {
		public DoubleConstant(Double value) {
			super(value);
		}
		@Override
		public StackElement copy() {
			return new DoubleConstant(value);
		}
	}
	
	public static class WhateverConstant extends Constant<Object> {
		public WhateverConstant(Object value) {
			super(value);
		}
		@Override
		public StackElement copy() {
			return new WhateverConstant(value);
		}
	}
}
