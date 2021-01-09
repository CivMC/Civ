package vg.civcraft.mc.civmodcore.playersettings.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;

@CivCommand(id = "configsetany")
public class ConfigSetAnyCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length < 3) {
			return false;
		}
		UUID uuid = resolveName(args [0]);
		if (uuid == null) {
			sender.sendMessage(ChatColor.RED + "Could not resolve player " + args[0]);
			return false;
		}
		String settingName = args [1];
		PlayerSetting<?> setting = PlayerSettingAPI.getSetting(settingName);
		if (setting == null) {
			sender.sendMessage(ChatColor.RED + "Could not find setting with identifier " + args[1]);
			return true;
		}
		String value = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
		if (!setting.isValidValue(value)) {
			sender.sendMessage(ChatColor.RED + value + " is not a valid value for this setting");
			return true;
		}
		setting.setValueFromString(uuid, value);		
		sender.sendMessage(ChatColor.GREEN + "Set value for setting " + setting.getIdentifier() + " for player " + uuid + " to " + value);
		return true;
	}
	
	public static UUID resolveName(String name) {
		try {
			return UUID.fromString(name);
		}
		catch (IllegalArgumentException e) {
			OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(name);
			if (offline == null) {
				return null;
			}
			return offline.getUniqueId();
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return Collections.emptyList();
	}

}
