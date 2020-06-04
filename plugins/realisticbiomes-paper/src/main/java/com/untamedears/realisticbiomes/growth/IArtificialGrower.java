package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;

public abstract class IArtificialGrower {

	public void fullyGrow(Plant plant) {
		setStage(plant, getMaxStage());
	}

	public abstract int getIncrementPerStage();

	public abstract int getMaxStage();

	public double getProgressGrowthStage(Plant plant) {
		return (double) getStage(plant) / getMaxStage();
	}

	public abstract int getStage(Plant plant);

	public abstract void setStage(Plant plant, int stage);

}
