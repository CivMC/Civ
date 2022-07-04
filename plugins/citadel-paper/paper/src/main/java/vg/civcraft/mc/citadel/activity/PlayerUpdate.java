package vg.civcraft.mc.citadel.activity;

import java.util.UUID;

class PlayerUpdate {
	public final RegionCoord regionCoord;
	public final UUID playerId;

	public PlayerUpdate(RegionCoord regionCoord, UUID playerId) {
		this.regionCoord = regionCoord;
		this.playerId = playerId;
	}
}
