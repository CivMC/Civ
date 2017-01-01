package com.programmerdan.minecraft.banstick.containers;

import java.util.Map;

import org.bukkit.command.CommandSender;

import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSPlayer;

public class BanResult {
	private Map<BSPlayer, BSBan> banned;

	public void informCommandSender(CommandSender sender) {
		
		for (Map.Entry<BSPlayer, BSBan> entry : banned.entrySet()) {
			
			
		}
	}
	
	
}
