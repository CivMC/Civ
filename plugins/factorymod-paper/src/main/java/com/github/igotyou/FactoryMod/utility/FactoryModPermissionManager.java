package com.github.igotyou.FactoryMod.utility;

import java.util.Arrays;
import java.util.List;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class FactoryModPermissionManager {

	private PermissionType useFactory;
	private PermissionType upgradeFactory;

	public FactoryModPermissionManager() {
		List<PlayerType> memberAndAbove = Arrays.asList(PlayerType.MEMBERS, PlayerType.MODS, PlayerType.ADMINS,
				PlayerType.OWNER);
		List<PlayerType> modAndAbove = Arrays.asList(PlayerType.MODS, PlayerType.ADMINS,
				PlayerType.OWNER);
		useFactory = PermissionType.registerPermission("USE_FACTORY", memberAndAbove, "Allows a player to use factories reinforced under this group.");
		upgradeFactory = PermissionType.registerPermission("UPGRADE_FACTORY", modAndAbove, "Allows a player to upgrade/make changes to a factory.");
	}
	
	public PermissionType getUseFactory() {
		return useFactory;
	}
	
	public PermissionType getUpgradeFactory() {
		return upgradeFactory;
	}

}
