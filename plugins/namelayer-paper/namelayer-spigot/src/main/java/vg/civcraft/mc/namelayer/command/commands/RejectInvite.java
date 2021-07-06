package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.commands.TabComplete;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.InviteTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

public class RejectInvite extends BaseCommandMiddle {

	@CommandAlias("nlrg|reject|rejectinvite|revoke")
	@Syntax("<group>")
	@Description("Reject an invitation to a group.")
	@CommandCompletion("@NL_Invites")
	public void execute(Player sender, String targetGroup) {
		Player player = (Player) sender;
		String groupName = targetGroup;
		Group group = GroupManager.getGroup(groupName);
		if (groupIsNull(sender, groupName, group)) {
			return;
		}
		UUID uuid = NameAPI.getUUID(player.getName());
		// The IDE is highlighting this as a potention NullReferenceException
		// but this is checked for in the above groupIsNull() call.
		GroupManager.PlayerType type = group.getInvite(uuid);
		if (type == null) {
			player.sendMessage(ChatColor.RED + "You were not invited to that group.");
			return;
		}
		if (group.isMember(uuid)) {
			player.sendMessage(ChatColor.RED + "You cannot reject an invite to a group that you're already a member of.");
			group.removeInvite(uuid, true);
			return;
		}
		group.removeInvite(uuid, true);
		PlayerListener.removeNotification(uuid, group);
		player.sendMessage(ChatColor.GREEN + "You've successfully declined that group invitation.");
	}

	@TabComplete("NL_Invites")
	public List<String> tabComplete(BukkitCommandCompletionContext context) {
		return InviteTabCompleter.complete(context.getInput(), context.getPlayer());
	}
}
