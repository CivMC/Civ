package com.untamedears.realisticbiomes.persist;

/**
 * Represents the abstract growth status of a single plant, without references to blocks
 * or any other minecraft stuff
 */
public class Plant {
	// time of the last update, in milliseconds
	long lastUpdateTime;
	// the growth amount of this plant
	// ranges from 0.0 to 1.0
	float growth;
	// the fruit growth amount from this plant (melons, cacti...)
	float fruitGrowth;
	
	public Plant(float growth, float fruitGrowth) {
		// divide by 1000 to get unix/epoch time, we don't need millisecond precision
		// also fixes bug where the timestamp would be too big for the mysql rb_plant date column
		this(System.currentTimeMillis() / 1000L, growth, fruitGrowth);
	}
	
	public Plant(long lastUpdateTime, float growth, float fruitGrowth) {
		this.lastUpdateTime = lastUpdateTime;
		this.growth = growth;
		if (growth < 1.0) {
			fruitGrowth = -1.0f;
		}
		this.fruitGrowth = fruitGrowth;
	}
	
	// update the time, return the time since the last update in ms
	private float updateTime() {
		long time = System.currentTimeMillis() / 1000L;
		float diff = time - lastUpdateTime;
		
		lastUpdateTime = time;
		return diff;
	}
	
	public long getUpdateTime() {
		return lastUpdateTime;
	}
	
	/**
	 * Sets last-update-time to now, sets growth for time diff since last update, and returns time diff.
	 * The time diff return value must be used for growFruit()
	 * @param rate
	 * @return time since last update/grow in seconds
	 */
	public double grow(double rate) {
		float diff = updateTime();
		double amount = rate * diff;
		
		growth = (float) Math.min(growth + amount, 1.0);
		
		return diff;
	}
	
	public void growFruit(double diff, double fruitRate) {
		double fruitAmount = fruitRate * diff;
		fruitGrowth = (float) Math.min(fruitGrowth + fruitAmount, 1.0);
	}

	public float getGrowth() {
		return growth;
	}
	
	public float getFruitGrowth() {
		return fruitGrowth;
	}
	
	public String toString() {
		return "<Plant lastUpdateTime: " + lastUpdateTime + " growth: " + growth + " fruit: " + fruitGrowth + ">";
	}

	/**
	 * @return true if fully grown, and either fruitless or fruit is also fully grown
	 */
	public boolean isFullyGrown() {
		return growth >= 1.0f && (fruitGrowth == -1.0f || fruitGrowth >= 1.0f);
	}

	public void setFruitGrowth(float fruitState) {
		this.fruitGrowth = fruitState;
	}

	public void setGrowth(double state) {
		this.growth = (float) state;
	}
}
