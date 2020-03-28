package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.InviteTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

import java.util.List;
import java.util.UUID;

public class RejectInvite extends PlayerCommandMiddle {

	public RejectInvite(String name) {
		super(name);
		setIdentifier("nlrg");
		setDescription("Reject an invitation to a group.");
		setUsage("/nlrg <group>");
		setArguments(1,1);
		setSenderMustBePlayer(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		String groupName = args[0];
		Group group = GroupManager.getGroup(groupName);
		if (groupIsNull(sender, groupName, group)) {
			return true;
		}
		UUID uuid = NameAPI.getUUID(player.getName());
		// The IDE is highlighting this as a potention NullReferenceException
		// but this is checked for in the above groupIsNull() call.
		GroupManager.PlayerType type = group.getInvite(uuid);
		if (type == null) {
			player.sendMessage(ChatColor.RED + "You were not invited to that group.");
			return true;
		}
		if (group.isMember(uuid)) {
			player.sendMessage(ChatColor.RED + "You cannot reject an invite to a group that you're already a member of.");
			group.removeInvite(uuid, true);
			return true;
		}
		group.removeInvite(uuid, true);
		PlayerListener.removeNotification(uuid, group);
		player.sendMessage(ChatColor.GREEN + "You've successfully declined that group invitation.");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length > 0) {
			return InviteTabCompleter.complete(args[0], (Player) sender);
		}
		else {
			return InviteTabCompleter.complete(null, (Player) sender);
		}
	}

}
