package com.github.devotedmc.hiddenore;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DropLimitsConfig {
	public List<String> tools;
	public int minY;
	public int maxY;
	public double chance;
	public double minAmount;
	public double maxAmount;
	public XPConfig xp;
	public String state;

	public DropLimitsConfig() {
		tools = new LinkedList<String>();
		minY = 1;
		maxY = 255;
		chance = 0.0;
		minAmount = 0.0;
		maxAmount = 0.0;
		xp = null;
		state = null;
	}

	public void setTools(Collection<String> tools) {
		this.tools.clear();
		if (tools != null) {
			this.tools.addAll(tools);
		}
	}
}
