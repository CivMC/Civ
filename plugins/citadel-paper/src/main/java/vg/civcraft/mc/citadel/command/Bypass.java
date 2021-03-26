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

@CivCommand(id = "ctb")
public class Bypass extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
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
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
