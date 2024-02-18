package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;

/**
 * Parent class for any block specific growth logic. Growth of a block is
 * subdivided into steps, where the initial state is 0 and the final (fully
 * grown) state is a positive integer. Growth happens in increments by a static
 * amount in between these numbers.
 * 
 * Growth stages may, but must not necessarily map to Ageable BlockStates, how
 * exactly stages are interpreted is entirely up to the implementation
 *
 */
public abstract class IArtificialGrower {

	/**
	 * Grows the given plant to its maximum growth stage possible
	 * 
	 * @param plant Plant to grow
	 */
	public void fullyGrow(Plant plant) {
		setStage(plant, getMaxStage());
	}

	/**
	 * 
	 * @return How many growth stages should be progressed at once
	 */
	public abstract int getIncrementPerStage();

	/**
	 * @return Maximum growth stage achievable
	 */
	public abstract int getMaxStage();

	/**
	 * How far the plant has grown as a fraction from 0 to 1
	 * 
	 * @param plant Plant to check growth for
	 * @return Growth on a scale from 0 to 1
	 */
	public double getProgressGrowthStage(Plant plant) {
		return (double) getStage(plant) / getMaxStage();
	}

	/**
	 * Gets the current growth stage of the given plant
	 * 
	 * @param plant Plant to get growth stage for
	 * @return Current growth stage of the plant between 0 and maxStage (inclusive). -1 if the plant is completely broken/gone
	 */
	public abstract int getStage(Plant plant);

	/**
	 * Sets the growth stage of the given plant to the given number
	 * 
	 * @param plant Plant to set growth stage for
	 * @param stage Stage to set to
	 * @return True if the stage was set successfully.
	 *         Now used only for VerticalGrower, helps to determine if CACTUS grown but was broken by adjacent blocks
	 */
	public abstract boolean setStage(Plant plant, int stage);

	/**
	 * @return Should a plant instance be deleted entirely once fully grown
	 */
	public abstract boolean deleteOnFullGrowth();
	
	/**
	 * Usually we assume plants are permanently broken if a stage update fails and don't update them anymore. Settings this to true will ignore that
	 * @return
	 */
	public boolean ignoreGrowthFailure() {
		return false;
	}

}
