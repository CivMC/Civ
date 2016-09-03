package com.programmerdan.minecraft.civspy;

import java.util.HashMap;
import java.util.Map;

/**
 * A data aggregation. Includes base timestamp, and summations.
 * For the moment, the only aggregation we will support is summation.
 *  
 * @author ProgrammerDan
 */
public class DataAggregate {

	private long timestamp;
	
	Map<String, Double> namedSums;

	Double sum;

	public DataAggregate(long timestamp) {
		this.timestamp = timestamp;
		this.namedSums = new HashMap<String, Double>();
		sum = null;
	}

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Includes a new sample into this aggregation.
	 * Follows a few simple rules:
	 * If neither string value nor number value, simply add 1 to "base" aggregate (increment)
	 * If string value but no number value, simply add 1 to that string's aggregate
	 * If number value but no string, add that number to "base" aggregate
	 * If both are present, add the number to that string's aggregate.
	 */
	public synchronized void include(DataSample sample) {
		String valueString = sample.getValueString();
		Number valueNumber = sample.getValueNumber();
		double value = 1.0d;
		if (valueNumber != null) value = valueNumber.doubleValue();
		if (valueString == null) {
			if (sum == null) {
				sum = value;
			} else {
				sum += value;
			}
		} else {
			Double priorValue = namedSums.get(valueString);
			if (priorValue != null) {
				value += priorValue;
			}
			namedSums.put(valueString, value);
		}
	}

}

