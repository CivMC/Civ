package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;

public class StemGrower extends AgeableGrower {

	private String fruitConfigName;
	private PlantGrowthConfig fruitConfig;
	
	public StemGrower(String fruitConfig) {
		super(7, 1);
		this.fruitConfigName = fruitConfig;
	}
	
	public String getFruitConfigName() {
		return fruitConfigName;
	}
	
	public void setFruitConfig(PlantGrowthConfig fruitConfig) {
		this.fruitConfig = fruitConfig;
	}

}
