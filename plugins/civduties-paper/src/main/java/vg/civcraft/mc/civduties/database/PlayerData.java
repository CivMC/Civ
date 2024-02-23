package vg.civcraft.mc.civduties.database;

import net.minecraft.nbt.CompoundTag;

public class PlayerData {
	private CompoundTag data;
	private String serverName;
	private String tierName;
	
	public PlayerData(CompoundTag data, String serverName, String tierName) {
		this.data = data;
		this.serverName = serverName;
		this.tierName = tierName;
	}

	public CompoundTag getData() {
		return data;
	}

	public String getServerName() {
		return serverName;
	}

	public String getTierName() {
		return tierName;
	}
}
