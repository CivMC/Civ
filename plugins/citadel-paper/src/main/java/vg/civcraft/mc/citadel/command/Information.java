package vg.civcraft.mc.citadel.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;

@CivCommand(id = "cti")
public class Information extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		BooleanSetting ctiSetting = Citadel.getInstance().getSettingManager().getInformationMode();
		ctiSetting.toggleValue(player.getUniqueId());
		player.sendMessage(ChatColor.GREEN + "Toggled reinforcement information mode " + ChatColor.YELLOW
				+ (ctiSetting.getValue(player.getUniqueId()) ? "on" : "off"));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
