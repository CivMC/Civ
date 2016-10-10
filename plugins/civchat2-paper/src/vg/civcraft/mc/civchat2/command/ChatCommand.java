package vg.civcraft.mc.civchat2.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public abstract class ChatCommand extends PlayerCommand {

	public ChatCommand(String name) {
		super(name);
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}	
}
