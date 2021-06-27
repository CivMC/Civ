package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.InviteTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

@CommandAlias("nlrg")
public class RejectInvite extends BaseCommandMiddle {

	@Syntax("/nlrg <group>")
	@Description("Reject an invitation to a group.")
	public void execute(CommandSender sender, String targetGroup) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Component.text("Be gone console thot").color(NamedTextColor.RED));
			return;
		}
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

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length > 0) {
			return InviteTabCompleter.complete(args[0], (Player) sender);
		}
		else {
			return InviteTabCompleter.complete(null, (Player) sender);
		}
	}

}
