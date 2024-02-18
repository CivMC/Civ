package com.untamedears.realisticbiomes.model.ltree;

import java.util.List;
import org.bukkit.util.Vector;

public class SpreadRule {
	
	private final double chance;
	private final List<String> resultSteps;
	private final List<Vector> directions;
	
	public SpreadRule(double chance, List<String> resultSteps, List<Vector> directions) {
		this.chance = chance;
		this.resultSteps = resultSteps;
		this.directions = directions;
	}
	
	public double getChance() {
		return chance;
	}
	
	public List<Vector> getDirections() {
		return directions;
	}
	
 	public List<String> getResultSteps() {
		return resultSteps;
	}

}
