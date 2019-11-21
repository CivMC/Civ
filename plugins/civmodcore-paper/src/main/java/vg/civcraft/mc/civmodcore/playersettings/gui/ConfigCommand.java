package vg.civcraft.mc.civmodcore.playersettings.gui;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;

import java.util.Collections;
import java.util.List;

public class ConfigCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You are not a player");
			return true;
		}
		PlayerSettingAPI.getMainMenu().showScreen((Player) sender);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return Collections.emptyList();
	}
	
	@Override
	public String getIdentifier() {
		return "config";
	}

}
