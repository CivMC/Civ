package com.untamedears.JukeAlert.command.commands;

import static com.untamedears.JukeAlert.util.Utility.findLookingAtOrClosestSnitch;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;

public class JaToggleLeversCommand extends PlayerCommand {

	 public JaToggleLeversCommand() {

		super("ToggleLevers");
		setDescription("Sets flag indicating if this juke will toggle levers on certain actions.");
		setUsage("/JaToggleLevers <1|0>");
		setArguments(1, 1);
		setIdentifier("jatogglelevers");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		if (!JukeAlert.getInstance().getConfigManager().getAllowTriggeringLevers()) {
			sender.sendMessage(ChatColor.RED + "JukeAlert - ToggleLevers feature disabled!");
			return false;
		}

		if (sender instanceof Player) {
			Player player = (Player) sender;
			int flagValue;
			Boolean flag;
			try {
				flagValue = Integer.parseInt(args[0]);

				if (0 == flagValue) {
					flag = false;
				} else {
					flag = true;
				}
			} catch (Exception e) {
				sender.sendMessage(ChatColor.RED + "Invalid Usage - /JaToggleLevers 1 or /JaToggleLevers 0.");
				return false;
			}

			Snitch snitch = findLookingAtOrClosestSnitch(player, PermissionType.getPermission("SNITCH_TOGGLE_LEVER"));

			if (snitch != null) {
				if (!snitch.shouldLog()) {
					sender.sendMessage(
						ChatColor.RED + "Toggle Lever Settings can only be applied to logging jukeboxes.");
					return false;
				}
				JukeAlert plugin = JukeAlert.getInstance();
				plugin.getJaLogger().updateSnitchToggleLevers(snitch, flag);
				snitch.setShouldToggleLevers(flag);
				sender.sendMessage(
					ChatColor.AQUA + "Changed the ToggleLevers setting to " + (flag ? "True" : "False") + ".");

				return true;
			} else {
				sender.sendMessage(
					ChatColor.RED + "You do not own any snitches nearby or do not have permission to modify them!");
				return false;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "You do not own any snitches nearby!");
			return false;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		return null;
	}
}
