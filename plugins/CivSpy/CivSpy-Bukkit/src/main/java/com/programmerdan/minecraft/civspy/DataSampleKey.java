package com.programmerdan.minecraft.civspy;

import java.util.UUID;

/**
 * Immutable key, used for aggregations
 */
public class DataSampleKey implements Comparable<DataSampleKey>{
	private String server;
	private String world;
	private UUID player;
	private Integer chunkX;
	private Integer chunkZ;

	private String key;

	private final int hash;

	/**
	 * This computes a hash at create, which isn't changed, so future hash-based comparisons are very fast.
	 * 
	 * @param server
	 * @param world
	 * @param player
	 * @param chunkX
	 * @param chunkZ
	 * @param key
	 */
	public DataSampleKey(final String server, final String world, final UUID player, final Integer chunkX, final Integer chunkZ, final String key) {
		this.server = server;
		this.world = world;
		this.player = player;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;

		this.key = key;
		
		int hash = 1;
		hash = 19 * hash + (server != null ? server.hashCode() : 0);
		hash = 31 * hash + (world != null ? world.hashCode() : 0);
		hash = 19 * hash + (player != null ? player.hashCode() : 0);
		hash = 31 * hash + (chunkX != null ? chunkX.hashCode() : 0);
		hash = 19 * hash + (chunkZ != null ? chunkZ.hashCode() : 0);
		hash = 31 * hash + (key != null ? key.hashCode() : 0);

		this.hash = hash;
	}

	// == accessors ==
	public String getKey() {
		return this.key;
	}
	
	public UUID getPlayer() {
		return this.player;
	}
	
	public Integer getChunkX() {
		return this.chunkX;
	}
	
	public Integer getChunkZ() {
		return this.chunkZ;
	}
	
	public String getWorld() {
		return this.world;
	}
	
	public String getServer() {
		return this.server;
	}


	// == useful methods ==

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof DataSampleKey) {
			if ( this.hashCode() == ((DataSampleKey) o).hashCode() ) {
				return this.compareTo((DataSampleKey) o) == 0;
			}
		}
		return false;
	}

	@Override
	public int compareTo(DataSampleKey k) {
		int comp = 0;

		if (this.key != null) {
			comp = this.key.compareTo(k.key);
		} else if (k.key != null) {
			comp = -k.key.compareTo(this.key);
		}
		if (comp != 0) return comp;

		if (this.player != null) {
			comp = this.player.compareTo(k.player);
		} else if (k.player != null) {
			comp = -k.player.compareTo(this.player);
		}
		if (comp != 0) return comp;

		if (this.chunkX != null) {
			comp = this.chunkX.compareTo(k.chunkX);
		} else if (k.chunkX != null) {
			comp = -k.chunkX.compareTo(this.chunkX);
		}
		if (comp != 0) return comp;

		if (this.chunkZ != null) {
			comp = this.chunkZ.compareTo(k.chunkZ);
		} else if (k.chunkZ != null) {
			comp = -k.chunkZ.compareTo(this.chunkZ);
		}
		if (comp != 0) return comp;

		if (this.world != null) {
			comp = this.world.compareTo(k.world);
		} else if (k.world != null) {
			comp = -k.world.compareTo(this.world);
		}
		if (comp != 0) return comp;
		
		if (this.server != null) {
			comp = this.server.compareTo(k.server);
		} else if (k.server != null) {
			comp = -k.server.compareTo(this.server);
		}
		return comp;
	}


	@Override
	public int hashCode() {
		return this.hash;
	}
}
