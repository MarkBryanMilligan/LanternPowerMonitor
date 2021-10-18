package com.lanternsoftware.util.mutable;

public class MutableDouble {
	private double value;

	public MutableDouble() {
	}

	public MutableDouble(double _value) {
		value = _value;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double _value) {
		value = _value;
	}

	public void add(double _value) {
		value += _value;
	}

	public void subtract(double _value) {
		value -= _value;
	}
}
