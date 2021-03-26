package vg.civcraft.mc.citadel.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;

@CivCommand(id = "cte")
public class EasyMode extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		BooleanSetting setting = Citadel.getInstance().getSettingManager().getEasyMode();
		boolean enabled = setting.getValue(player);
		if (enabled) {
			CitadelUtility.sendAndLog(player, ChatColor.GREEN, "Easy reinforcing mode has been disabled.");
		} else {
			CitadelUtility.sendAndLog(player, ChatColor.GREEN,
					"Easy reinforcing mode has been enabled. You will be able to reinforce to your default group by placing blocks while having a reinforcement material in your off hand.");
		}
		setting.toggleValue(player.getUniqueId());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
