package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;

@CommandAlias("cti")
public class Information extends BaseCommand {

	@Syntax("/cti")
	@Description("Enters information mode. Interacting with blocks in information mode will show information on their reinforcement")
	public void execute(CommandSender sender) {
		Player player = (Player) sender;
		BooleanSetting ctiSetting = Citadel.getInstance().getSettingManager().getInformationMode();
		ctiSetting.toggleValue(player.getUniqueId());
		player.sendMessage(ChatColor.GREEN + "Toggled reinforcement information mode " + ChatColor.YELLOW
				+ (ctiSetting.getValue(player.getUniqueId()) ? "on" : "off"));
	}
}
