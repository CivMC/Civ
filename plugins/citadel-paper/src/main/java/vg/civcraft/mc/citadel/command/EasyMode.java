package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;

public class EasyMode extends BaseCommand {

	@CommandAlias("cte|easymode")
	@Description("Reinforces to your default group using materials from your offhand.")
	public void execute(Player player) {
		BooleanSetting setting = Citadel.getInstance().getSettingManager().getEasyMode();
		boolean enabled = setting.getValue(player);
		if (enabled) {
			CitadelUtility.sendAndLog(player, ChatColor.GREEN, "Easy reinforcing mode has been disabled.");
		} else {
			CitadelUtility.sendAndLog(player, ChatColor.GREEN,
					"Easy reinforcing mode has been enabled. You will be able to reinforce to your default group by placing blocks while having a reinforcement material in your off hand.");
		}
		setting.toggleValue(player.getUniqueId());
	}
}
