package com.untamedears.realisticbiomes.growth;

import com.google.common.base.Preconditions;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.Plant;

public class StemGrower extends AgeableGrower {

	private String fruitConfigName;
	private PlantGrowthConfig fruitConfig;
	
	public StemGrower(String fruitConfig) {
		super(7, 1);
		Preconditions.checkNotNull(fruitConfig);
		this.fruitConfigName = fruitConfig;
	}
	
	public String getFruitConfigName() {
		return fruitConfigName;
	}
	
	public void setFruitConfig(PlantGrowthConfig fruitConfig) {
		this.fruitConfig = fruitConfig;
	}
	
	@Override
	public void setStage(Plant plant, int stage) {
		super.setStage(plant, stage);
		if (getMaxStage() == stage) {
			plant.setGrowthConfig(fruitConfig);
		}
	}

}
