package com.biggestnerd.namecolors;

import java.util.UUID;

import org.bukkit.Bukkit;

import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.namelayer.NameAPI;

public class CivChatManager {
	
	private static boolean enable;
	
	public static void init() {
		enable = Bukkit.getPluginManager().isPluginEnabled("CivChat2");
	}
	
	public static void updatePlayerName(UUID player, String name) {
		if(enable) {
			if(name == NameAPI.getCurrentName(player)) {
				CivChat2Manager.removeCustomName(player);
			} else {
				CivChat2Manager.setCustomName(player, name);
			}
		}
	}
}
