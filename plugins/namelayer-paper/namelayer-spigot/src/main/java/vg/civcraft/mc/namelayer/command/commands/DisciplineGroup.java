package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

@CommandAlias("nldig")
@CommandPermission("namelayer.admin")
public class DisciplineGroup extends BaseCommandMiddle {

	@Syntax("/nldig <group>")
	@Description("Disable a group from working.")
	public void execute(CommandSender sender, String groupName) {
		if (!(sender instanceof Player))
			sender.sendMessage(ChatColor.AQUA + "Meh, fine, just this one.");
		// checks and stuff should be in plugin.yml so going to assume that sender has perms
		// naaaaaaa
		Player p = (Player) sender;
		Group g = gm.getGroup(groupName);
		if (groupIsNull(sender, groupName, g)) {
			return;
		}
		if (!p.isOp() || !p.hasPermission("namelayer.admin")){
			p.sendMessage(ChatColor.RED + "You do not have permission for this op command.");
			return;
		}
		if (g.isDisciplined()){
			g.setDisciplined(false);
			sender.sendMessage(ChatColor.GREEN + "Group has been enabled.");
		}
		else{
			g.setDisciplined(true);
		sender.sendMessage(ChatColor.GREEN + "Group has been disabled.");
		}
	}
}
