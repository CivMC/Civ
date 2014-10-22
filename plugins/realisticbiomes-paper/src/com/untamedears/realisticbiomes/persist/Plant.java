package com.untamedears.realisticbiomes.persist;

// represents the growth status of a single plant

public class Plant {
	// time of the last update, in milliseconds
	long lastUpdateTime;
	// the growth amount of this plant
	// ranges from 0.0 to 1.0
	float growth;
	
	public Plant(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
		growth = 0.0f;
	}
	
	public Plant(long lastUpdateTime, float growth) {
		this.lastUpdateTime = lastUpdateTime;
		this.growth = growth;
	}
	
	// update the time, return the time since the last update in ms
	public float setUpdateTime(long time) {
		float diff = (float)(time-lastUpdateTime);
		
		lastUpdateTime = time;
		return diff;
	}
	
	long getUpdateTime() {
		return lastUpdateTime;
	}
	
	public void addGrowth(float amount) {
		growth += amount;
		if (growth > 1.0)
			growth = 1.0f;
	}
	
	public float getGrowth() {
		return growth;
	}
	
	public String toString() {
	
		return "<Plant lastUpdateTime: " + lastUpdateTime + " growth: " + growth + ">";
	
	}
}
