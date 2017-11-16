package vg.civcraft.mc.citadel.command.commands;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelConfigManager;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public abstract class PlayerCommandMiddle extends PlayerCommand {

	public PlayerCommandMiddle(String name) {
		super(name);
	}

	protected void sendAndLog(CommandSender receiver, ChatColor color, String message) {
		receiver.sendMessage(color + message);
		if (CitadelConfigManager.shouldLogPlayerCommands()) {
			Citadel.getInstance().getLogger().log(Level.INFO, "Sent {0} reply {1}", new Object[]{receiver.getName(), message});
		}
	}
}