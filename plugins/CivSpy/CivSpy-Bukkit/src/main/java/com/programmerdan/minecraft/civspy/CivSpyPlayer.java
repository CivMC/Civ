package com.programmerdan.minecraft.civspy;

public class CivSpyPlayer implements Cloneable {
	private UUID who;
	private Long[] stats;

	
	static enum Tracking {
		Drop("hiddenore.drop"),
		Generate("hiddenore.generate"),
		Kill("hunter.slay"),
		KillDrop("hunter.drop"),
		Mine("miner.mine"),
		Build("builder.place"),
		Plant("farmer.plant"),
		Harvest("farmer.harvest"),

		private String key;

		public Tracking(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}
}
