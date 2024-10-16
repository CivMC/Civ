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
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.BlackList;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class AddBlacklist extends BaseCommandMiddle {

	@CommandAlias("nlbl|blacklist|addblacklist")
	@Syntax("<group> <player>")
	@Description("Blacklist a player for a specific group")
	@CommandCompletion("@NL_Groups @allplayers")
	public void execute(Player sender, String groupName, String playerName) {
		Player p = (Player) sender;
		Group g = GroupManager.getGroup(groupName);
		if (g == null) {
			p.sendMessage(ChatColor.RED + "This group does not exist");
			return;
		}
		if (!gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("BLACKLIST"))
				&& !(p.isOp() || p.hasPermission("namelayer.admin"))) {
			p.sendMessage(ChatColor.RED + "You do not have the required permissions to do this");
			return;
		}
		UUID targetUUID = NameAPI.getUUID(playerName);
		if (targetUUID == null) {
			p.sendMessage(ChatColor.RED + "This player does not exist");
			return;
		}
		if (g.isMember(targetUUID)) {
			p.sendMessage(ChatColor.RED + "You can't blacklist members of a group");
			return;
		}
		BlackList bl = NameLayerPlugin.getBlackList();
		if (bl.isBlacklisted(g, targetUUID)) {
			p.sendMessage(ChatColor.RED + "This player is already blacklisted");
			return;
		}
		bl.addBlacklistMember(g, targetUUID, true);
		p.sendMessage(ChatColor.GREEN + NameAPI.getCurrentName(targetUUID) + " was successfully blacklisted on the group " + g.getName());
	}
}
