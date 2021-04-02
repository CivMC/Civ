package com.untamedears.jukealert.commands;

import static com.untamedears.jukealert.util.JAUtility.findLookingAtOrClosestSnitch;


import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.LeverToggleAppender;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.core.PermissionType;

@CivCommand(id = "jatogglelevers")
public class ToggleLeverCommand extends StandaloneCommand {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Players only");
			return true;
		}
		Player player = (Player) sender;
		if (args.length >= 1) {
			return false;
		}
		PermissionType togglePerm = JukeAlert.getInstance().getPermissionHandler().getToggleLevers();
		Snitch snitch = findLookingAtOrClosestSnitch(player, togglePerm);
		if (snitch == null) {
			player.sendMessage(
					org.bukkit.ChatColor.RED + "You do not own any snitches nearby or lack permission to view their logs!");
			return true;
		}
		if (!snitch.hasAppender(LeverToggleAppender.class)) {
			player.sendMessage(ChatColor.RED + "This snitch does not have Toggle lever functionality!");
			return true;
		}
		LeverToggleAppender toggleAppender = (LeverToggleAppender) snitch.getAppender(LeverToggleAppender.class);
		toggleAppender.switchState();
		player.sendMessage(ChatColor.GREEN + "Toggled lever activation " + (toggleAppender.shouldToggle() ? "on" : "off"));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}
}
