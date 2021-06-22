package vg.civcraft.mc.civmodcore.players.settings.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;

@CommandAlias("config")
public final class ConfigCommand extends BaseCommand {

	private static final String MANAGE_PERMISSION = "cmc.config.manage";

	@Default
	@Description("Allows configuring player specific settings")
	public void openConfig(final Player sender) {
		PlayerSettingAPI.getMainMenu().showScreen(sender);
	}

	@Subcommand("save")
	@Description("Save all settings to the file.")
	@CommandPermission(MANAGE_PERMISSION)
	public void saveAllConfigs(final CommandSender sender) {
		PlayerSettingAPI.saveAll();
		sender.sendMessage(ChatColor.GREEN + "/config has been saved.");
	}

	@Subcommand("get")
	@Description("Lets you read any config setting for any player")
	@Syntax("config get <player|uuid> <setting>")
	@CommandPermission(MANAGE_PERMISSION)
	public void readConfigValue(final CommandSender sender,
								final String player,
								final String setting) {
		final UUID playerUUID = INTERNAL_resolvePlayer(player);
		if (playerUUID == null) {
			sender.sendMessage(ChatColor.RED + "Could not resolve player " + player);
			return;
		}
		final PlayerSetting<?> foundSetting = PlayerSettingAPI.getSetting(setting);
		if (foundSetting == null) {
			sender.sendMessage(ChatColor.RED + "Could not find setting with identifier " + setting);
			return;
		}
		final String currentValue = foundSetting.getSerializedValueFor(playerUUID);
		sender.sendMessage(ChatColor.GREEN + "Value for setting [" + foundSetting.getIdentifier() + "] " +
				"for player [" + playerUUID + "] is: " + currentValue);
	}

	@Subcommand("set")
	@Description("Lets you write any config setting for any player")
	@Syntax("config set <player|uuid> <setting> <value>")
	@CommandPermission(MANAGE_PERMISSION)
	public void writeConfigValue(final CommandSender sender,
								 final String player,
								 final String setting,
								 final String value) {
		final UUID playerUUID = INTERNAL_resolvePlayer(player);
		if (playerUUID == null) {
			sender.sendMessage(ChatColor.RED + "Could not resolve player " + player);
			return;
		}
		final PlayerSetting<?> foundSetting = PlayerSettingAPI.getSetting(setting);
		if (foundSetting == null) {
			sender.sendMessage(ChatColor.RED + "Could not find setting with identifier " + setting);
			return;
		}
		if (!foundSetting.isValidValue(value)) {
			sender.sendMessage(ChatColor.RED + "[" + value + "] is not a valid value for this setting");
			return;
		}
		foundSetting.setValueFromString(playerUUID, value);
		sender.sendMessage(ChatColor.GREEN + "Set value for setting [" + foundSetting.getIdentifier() + "] " +
				"for player [" + playerUUID + "] to: " + value);
	}

	private static UUID INTERNAL_resolvePlayer(final String value) {
		try {
			return UUID.fromString(value);
		}
		catch (final IllegalArgumentException e) {
			final OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(value);
			if (offline == null) {
				return null;
			}
			return offline.getUniqueId();
		}
	}

}
