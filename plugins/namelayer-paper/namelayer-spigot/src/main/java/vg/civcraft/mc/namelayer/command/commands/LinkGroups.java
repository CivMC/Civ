package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class LinkGroups extends BaseCommandMiddle {

	@CommandAlias("nllink|linkgroups")
	@Syntax("<super group> <sub group>")
	@Description("Links two groups to each other as nested groups.")
	@CommandCompletion("@NL_Groups @NL_Groups")
	public void execute(Player sender, String parentGroup, String childGroup) {
		Player p = (Player) sender;
			
		String supername = parentGroup, subname = childGroup;
		
		Group supergroup = GroupManager.getGroup(supername);
		if (groupIsNull(sender, supername, supergroup)) { 
		    return;
		}
		
		Group subgroup = GroupManager.getGroup(subname);
		if (groupIsNull(sender, subname, subgroup)) { 
		    return;
		}
		
		if(subgroup.getName().equalsIgnoreCase(supergroup.getName())) {
			p.sendMessage(ChatColor.RED + "Not today");
			return;
		}
		
		// check if groups are accessible
		
		UUID uuid = NameAPI.getUUID(p.getName());
		
		if (!supergroup.isMember(uuid) || !subgroup.isMember(uuid)) {
			p.sendMessage(ChatColor.RED + "You're not on one of the groups.");
			return;
		}
		
		if (supergroup.isDisciplined() || subgroup.isDisciplined()) {
			p.sendMessage(ChatColor.RED + "One of the groups is disciplined.");
			return;
		}		
		
		if (!gm.hasAccess(subgroup, uuid, PermissionType.getPermission("LINKING"))) {
			p.sendMessage(ChatColor.RED 
					+ "You don't have permission to do that on the sub group.");
			return;
		}
		if (!gm.hasAccess(supergroup, uuid, PermissionType.getPermission("LINKING"))) {
			p.sendMessage(ChatColor.RED 
					+ "You don't have permission to do that on the super group.");
			return;
		}
		
		if (Group.areLinked(supergroup, subgroup)) {
			p.sendMessage(ChatColor.RED + "These groups are already linked.");
			return;
		}
		
		boolean success = Group.link(supergroup, subgroup, true);
		
		String message;
		if (success) {
			message = ChatColor.GREEN + "The groups have been successfully linked.";
		} else {
			message = ChatColor.RED + "Failed to link the groups.";
		}
		p.sendMessage(message);
	}
}
