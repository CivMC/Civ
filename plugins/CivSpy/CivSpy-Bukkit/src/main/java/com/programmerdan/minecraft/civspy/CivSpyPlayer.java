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

	class Key implements Comparable<Key> {
		private Integer chunkX;
		private Integer chunkY;
		private UUID player;

		@Override
		public int compareTo(Key o) {
			if ((this.chunkX == null && o.chunkX != null) || this.chunkX < o.chunkX) {
				return -1;
			} else if ((this.chunkX == this.chunkX > o.chunkX) {
				return 1;
			} else {
				if (this.chunkZ < o.chunkZ) {
					return -1;
				} else if (this.chunkZ > o.chunkZ) {
					return 1;
				} else {
					return this.player.compareTo(o);
				}

			}
		}

		@Override
		public int hashCode() {

		}
}
