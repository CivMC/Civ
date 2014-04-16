package com.untamedears.citadel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MemberManager {
    public MemberManager() {}

	public static boolean isOnline(String playerName){
		return Bukkit.getPlayerExact(playerName) != null;
	}

	public static Player getOnlinePlayer(String playerName){
		return Bukkit.getPlayerExact(playerName);
	}
}
