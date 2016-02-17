package com.github.devotedmc.hiddenore;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DropLimitsConfig {
	public Set<String> tools;
	public int minY;
	public int maxY;
	public double chance;
	public int minAmount;
	public int maxAmount;
	
	public DropLimitsConfig() {
		tools = new HashSet<String>();
		minY = 1;
		maxY = 255;
		chance = 0.0;
		minAmount = 0;
		maxAmount = 0;
	}
	
	public void setTools(Collection<String> tools) {
		this.tools.clear();
		if (tools != null) {
			this.tools.addAll(tools);
		}
	}
}
