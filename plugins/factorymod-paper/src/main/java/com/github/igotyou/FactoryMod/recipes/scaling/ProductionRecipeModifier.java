package com.github.igotyou.FactoryMod.recipes.scaling;

import java.util.TreeMap;

public class ProductionRecipeModifier {
	
	private TreeMap <Integer, ProductionRecipeModifierConfig> configs;
	
	public ProductionRecipeModifier() {
		this.configs = new TreeMap<Integer, ProductionRecipeModifier.ProductionRecipeModifierConfig>();
	}

	public double getFactor(int rank, int runAmount) {
		ProductionRecipeModifierConfig config = configs.get(rank);
		if (config == null) {
			//no entry exists, so don't change
			return 1.0;
		}
		return config.getModifier(runAmount);
	}

	public void addConfig(int minimumRuns, int maximumRuns, double baseIncrease, double maximumIncrease, int rank) {
		configs.put(rank, new ProductionRecipeModifierConfig(minimumRuns, maximumRuns, baseIncrease, maximumIncrease, rank));
	}
	
	
	public ProductionRecipeModifier clone() {
		ProductionRecipeModifier modi = new ProductionRecipeModifier();
		for(ProductionRecipeModifierConfig config : this.configs.values()) {
			modi.addConfig(config.getMinimumRuns(), config.getMaximumRuns(), config.getBaseIncrease(), config.getMaximumIncrease(), config.getRank());
		}
		return modi;
	}
	
	public double getMaximumMultiplierForRank(int rank) {
		ProductionRecipeModifierConfig config = configs.get(rank);
		if (config == null) {
			//no entry exists, so don't change
			return 1.0;
		}
		return config.getMaximumIncrease();
	}
	
	private class ProductionRecipeModifierConfig {

		private int minimumRuns;
		private int maximumRuns;
		private double baseIncrease;
		private double maximumIncrease;
		private int rank;

		public ProductionRecipeModifierConfig(int minimumRuns, int maximumRuns, double baseIncrease,
				double maximumIncrease, int rank) {
			this.minimumRuns = minimumRuns;
			this.maximumIncrease = maximumIncrease;
			this.maximumRuns = maximumRuns;
			this.baseIncrease = baseIncrease;
			this.rank = rank;
		}

		public int getMinimumRuns() {
			return minimumRuns;
		}

		public int getMaximumRuns() {
			return maximumRuns;
		}

		public double getBaseIncrease() {
			return baseIncrease;
		}

		public double getMaximumIncrease() {
			return maximumIncrease;
		}
		
		public int getRank() {
			return rank;
		}

		public double getModifier(int runs) {
			if (runs > maximumRuns) {
				return maximumIncrease;
			}
			if (runs < minimumRuns) {
				return baseIncrease;
			}
			return (((double) (runs - minimumRuns)) / ((double) (maximumRuns - minimumRuns)) * (maximumIncrease - baseIncrease))
					+ baseIncrease;
		}
	}
}
