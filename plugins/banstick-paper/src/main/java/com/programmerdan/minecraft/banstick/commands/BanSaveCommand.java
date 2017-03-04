package com.programmerdan.minecraft.banstick.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.programmerdan.minecraft.banstick.BanStick;

public class BanSaveCommand  implements CommandExecutor{

	public static String name = "bansave";
		
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdString, String[] arguments) {
		BanStick.getPlugin().saveCache();
		
		return true;
	}

}
