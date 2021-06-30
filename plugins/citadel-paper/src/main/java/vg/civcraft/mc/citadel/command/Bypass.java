package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;

@CommandAlias("ctb")
public class Bypass extends BaseCommand {

	@Syntax("/ctb")
	@Description("Toggles bypass state. In bypass state you can break blocks reinforced on groups you have access to in a single break")
	public void execute(CommandSender sender) {
		Player player = (Player) sender;
		BooleanSetting setting = Citadel.getInstance().getSettingManager().getBypass();
		boolean enabled = setting.getValue(player);
		if (enabled) {
			CitadelUtility.sendAndLog(player, ChatColor.GREEN, "Bypass mode has been disabled.");
		} else {
			CitadelUtility.sendAndLog(player, ChatColor.GREEN,
					"Bypass mode has been enabled. You will be able to break reinforced blocks if you are on the group.");
		}
		setting.toggleValue(player.getUniqueId());
	}
}
