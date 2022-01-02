package com.untamedears.realisticbiomes.model.ltree;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class LTree {

	private LStepConfig startSymbol;
	private Vector intialVector;
	private String name;
	private int DEPTH_CAP = 100;

	public LTree(String name, String startingSymbol, Vector initialVector, Map<String, List<SpreadRule>> spreadRules,
			Function<String,LStepConfig> configGenerator) {
		this.intialVector = initialVector;
		this.name = name;
		Map<String, LStepConfig> configMap = new HashMap<>();
		configMap.put(startingSymbol, configGenerator.apply(startingSymbol));
		for (List<SpreadRule> ruleList : spreadRules.values()) {
			for (SpreadRule rule : ruleList) {
				for (String key : rule.getResultSteps()) {
					configMap.put(key, configGenerator.apply(key));
				}
			}
		}
		this.startSymbol = configMap.get(startingSymbol);
		if (this.startSymbol == null) {
			throw new IllegalArgumentException("Starting symbol can not be null");
		}
		for (Entry<String, List<SpreadRule>> entry : spreadRules.entrySet()) {
			LStepConfig currentStep = configMap.get(entry.getKey());
			for (SpreadRule rule : entry.getValue()) {
				List<LStepConfig> followupSteps = rule.getResultSteps().stream().map(configMap::get).collect(Collectors.toList());
				currentStep.addNextStep(followupSteps, rule.getChance(), rule.getDirections());
			}
		}
	}

	public String getName() {
		return name;
	}

	public void genAt(Location loc) {
		LStep startingStep = new LStep(startSymbol, loc, intialVector, 0);
		Deque<LStep> todo = new LinkedList<>();
		todo.add(startingStep);
		while (!todo.isEmpty()) {
			LStep item = todo.poll();
			System.out.println("Processing " + item.getConfig().getID());
			for (LStep next : item.apply()) {
				if (next.getDepth() <= DEPTH_CAP) {
					todo.push(next);
				}
			}
		}
	}

}
