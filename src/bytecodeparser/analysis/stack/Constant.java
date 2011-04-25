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
