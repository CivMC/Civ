package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

public class LeaveGroup extends BaseCommandMiddle {

	@CommandAlias("nlleg|leave|leavegroup")
	@Syntax("<group>")
	@Description("Leave a group")
	@CommandCompletion("@NL_Groups")
	public void execute(Player sender, String groupName) {
		Player p = (Player) sender;
		Group g = gm.getGroup(groupName);
		if (groupIsNull(sender, groupName, g)) {
			return;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		if (!g.isCurrentMember(uuid)){
			p.sendMessage(ChatColor.RED + "You are not a member of this group.");
			return;
		}
		if (g.isDisciplined()){
			p.sendMessage(ChatColor.RED + "This group is disciplined.");
			return;
		}
		g.removeMember(uuid);
		p.sendMessage(ChatColor.GREEN + "You have been removed from the group.");
	}
}
