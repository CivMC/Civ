package vg.civcraft.mc.civmodcore.players.settings.gui;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;

@CivCommand(id = "configgetany")
public class ConfigGetAnyCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length < 2) {
			return false;
		}
		UUID uuid = ConfigSetAnyCommand.resolveName(args [0]);
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
		String value = setting.getSerializedValueFor(uuid);
		sender.sendMessage(ChatColor.GREEN + "Value for setting " + setting.getIdentifier() + " for player " + uuid + " is " + value);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return Collections.emptyList();
	}

}
