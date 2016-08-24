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

	class Key {
		private Integer chunkX;
		private Integer chunkZ;
		private UUID player;

		@Override
		public boolean equals(Object o) {
			if (o instanceof Key) {
				Key k = (Key) o;
				return this.chunkX == k.chunkX && this.chunkZ == k.chunkZ && this.player == k.player;
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 1;
			hash = 19 * hash + (player != null ? player.hashCode() : 0);
			hash = 31 * hash + (chunkX != null ? chunkX.hashCode() : 0);
			hash = 19 * hash + (chunkZ != null ? chunkZ.hashCode() : 0);
			return hash;
		}
}
