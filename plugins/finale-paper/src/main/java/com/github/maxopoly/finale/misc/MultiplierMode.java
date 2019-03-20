package com.github.maxopoly.finale.misc;

public enum MultiplierMode {

	LINEAR, EXPONENTIAL;

	public double apply(double basevalue, double multiplier, int level) {
		switch (this) {
		case LINEAR:
			return basevalue * (1.0 + ((multiplier - 1.0) * level));
		case EXPONENTIAL:
			return basevalue * Math.pow(multiplier, level);
		}
		throw new IllegalStateException();
	}

}
