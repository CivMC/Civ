package com.untamedears.realisticbiomes.growthconfig;

public abstract class AbstractGrowthConfig {

	protected final String name;

	public AbstractGrowthConfig(String name) {
		this.name = name;
	}

	/**
	 * @return Identifiying unique name of this config
	 */
	public String getName() {
		return name;
	}
}
