package com.github.maxopoly.KiraBukkitGateway;

import org.bukkit.ChatColor;

public class KiraUtil {
	
	public static String cleanUp(String s) {
		return ChatColor.stripColor(s).replaceAll("[^\\p{InBasic_Latin}\\p{InLatin-1Supplement}]", "");
	}

}
