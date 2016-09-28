package com.github.devotedmc.hiddenore;


public class XPConfig {
	public double chance;
	public double minAmount;
	public double maxAmount;

	public XPConfig() {
		chance = 0.0;
		minAmount = 0.0;
		maxAmount = 0.0;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("chance: ").append(chance)
			.append(" min: ").append(minAmount)
			.append(" max: ").append(maxAmount);
		
		return sb.toString();
	}
}
