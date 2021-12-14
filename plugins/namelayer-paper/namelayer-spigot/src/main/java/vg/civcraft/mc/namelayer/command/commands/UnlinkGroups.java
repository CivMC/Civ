package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class UnlinkGroups extends BaseCommandMiddle {

	@CommandAlias("nlunlink|unlink|unlinkgroups")
	@Syntax("<super_group> <sub_group>")
	@Description("Unlinks two groups from each other.")
	@CommandCompletion("@NL_Groups @NL_Groups")
	public void execute(CommandSender sender, String parentGroup, String childGroup) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "Sorry bruh, no can do.");
			return;
		}
		Player p = (Player) sender;
		
		// check if groups exist
		
		String supername = parentGroup, subname = childGroup;
		
		Group supergroup = GroupManager.getGroup(supername);
		if (groupIsNull(sender, supername, supergroup)) {
		    return;
		}
		
		Group subgroup = GroupManager.getGroup(subname);
		if (groupIsNull(sender, subname, subgroup)) {
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
		
		if (!gm.hasAccess(supergroup, uuid, PermissionType.getPermission("LINKING"))) {
			p.sendMessage(ChatColor.RED 
					+ "You don't have permission to do that on the super group.");
			return;
		}
		
		if (!Group.areLinked(supergroup, subgroup)) {
			p.sendMessage(ChatColor.RED + "These groups are not linked.");
			return;
		}
		
		boolean success = Group.unlink(supergroup, subgroup);
		
		String message;
		if (success) {
			message = ChatColor.GREEN + "The groups have been successfully unlinked.";
		} else {
			message = ChatColor.RED + "Failed to unlink the groups.";
		}
		p.sendMessage(message);
	}
}
