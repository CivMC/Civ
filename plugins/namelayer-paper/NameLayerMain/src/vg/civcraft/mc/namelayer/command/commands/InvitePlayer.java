package vg.civcraft.mc.namelayer.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.command.TabCompleters.MemberTypeCompleter;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.groups.PrivateGroup;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.misc.Mercury;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class InvitePlayer extends PlayerCommandMiddle{

	private GroupManagerDao db = NameLayerPlugin.getGroupManagerDao();
	public InvitePlayer(String name) {
		super(name);
		setIdentifier("nlip");
		setDescription("Invite a player to the PlayerType " + PlayerType.toStringName() + " of a group.");
		setUsage("/nlip <group> <player> (PlayerType- default MEMBERS)");
		setArguments(2,3);
	}

	@Override
	public boolean execute(CommandSender s, String[] args) {
		final String targetGroup = args[0];
		final String targetPlayer = args[1];
		final String targetType = args.length >= 3 ? args[2] : null;
		final boolean isPlayer = s instanceof Player;
		final Player p = isPlayer ? (Player)s : null;
		final boolean isAdmin = !isPlayer || p.hasPermission("namelayer.admin");
		final Group group = gm.getGroup(targetGroup);
		if (group == null){
			s.sendMessage(ChatColor.RED + "That group does not exist.");
			return true;
		}
		if (!isAdmin && group.isDisciplined()) {
			s.sendMessage(ChatColor.RED + "This group is disiplined.");
			return true;
		}
		final UUID targetAccount = NameAPI.getUUID(targetPlayer);
		if (targetAccount == null) {
			s.sendMessage(ChatColor.RED + "The player has never played before.");
			return true;
		}
		if (group.isMember(targetAccount)) { // So a player can't demote someone who is above them.
			s.sendMessage(ChatColor.RED + "Player is already a member."
					+ "Use /nlpp to change their PlayerType.");
			return true;
		}
		final PlayerType pType = targetType != null ? PlayerType.getPlayerType(targetType) : PlayerType.MEMBERS;
		if (pType == null) {
			if (p != null) {
				PlayerType.displayPlayerTypes(p);
			} else {
				s.sendMessage("Invalid player type");
			}
			return true;
		}
		if (!isAdmin) {
			// Perform access check
			final GroupPermission perm = gm.getPermissionforGroup(group);
			final UUID executor = p.getUniqueId();
			final PlayerType t = group.getPlayerType(executor); // playertype for the player running the command.
			if (t == null) {
				s.sendMessage(ChatColor.RED + "You are not on that group.");
				return true;
			}
			boolean allowed = false;
			switch (pType) { // depending on the type the executor wants to add the player to
				case MEMBERS:
					allowed = perm.isAccessible(t, PermissionType.MEMBERS);
					break;
				case MODS:
					allowed = perm.isAccessible(t, PermissionType.MODS);
					break;
				case ADMINS:
					allowed = perm.isAccessible(t, PermissionType.ADMINS);
					break;
				case OWNER:
					allowed = perm.isAccessible(t, PermissionType.OWNER);
					break;
				default:
					allowed = false;
					break;
			}
			if (!allowed) {
				s.sendMessage(ChatColor.RED + "You do not have permissions to modify this group.");
				return true;
			}
		}
		group.addInvite(targetAccount, pType);
		final boolean shouldAutoAccept = db.shouldAutoAcceptGroups(targetAccount);
		final Player oInvitee = Bukkit.getPlayer(targetAccount);
		if (oInvitee != null) {
			//invitee is online make them a player
			if (shouldAutoAccept) {
				//player auto accepts invite
				group.addMember(targetAccount, pType);
				group.removeRemoveInvite(targetAccount);
				if (group instanceof PrivateGroup) {
					PrivateGroup priv = (PrivateGroup) group;
					List<Group> groups = priv.getSubGroups();
					for (Group g: groups) {
						g.addMember(targetAccount, PlayerType.SUBGROUP);
					}
				}
				s.sendMessage(ChatColor.GREEN + "The invitation has been sent." + "\n Use /nlri to Revoke an invite.");
				oInvitee.sendMessage(ChatColor.GREEN + " You have auto-accepted invite to the group: " + group.getName());
			} else {
				PlayerListener.addNotification(targetAccount, group);
				if (isPlayer) {
					oInvitee.sendMessage(ChatColor.GREEN + "You have been invited to the group " + group.getName() +" by " + p.getName() +".\n" +
							"Use the command /nlag <group> to accept.\n"
							+ "If you wish to toggle invites so they always are accepted please run /nltaai");
				} else {
					oInvitee.sendMessage(ChatColor.GREEN + "You have been invited to the group " + group.getName() + ".\n" +
							"Use the command /nlag <group> to accept.\n"
							+ "If you wish to toggle invites so they always are accepted please run /nltaai");
				}
				s.sendMessage(ChatColor.GREEN + "The invitation has been sent." + "\n Use /nlri to Revoke an invite.");
			}
		} else {
			//invitee is offline
			if (shouldAutoAccept) {
				group.addMember(targetAccount, pType);
				group.removeRemoveInvite(targetAccount);
			}
			PlayerListener.addNotification(targetAccount, group);
			s.sendMessage(ChatColor.GREEN + "The invitation has been sent." + "\n Use /nlri to Revoke an invite.");
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "I'm sorry baby, please run this as a player :)");
			return null;
		}
		if (args.length < 2) {
			if (args.length == 0)
				return GroupTabCompleter.complete(null, null, (Player) sender);
			else
				return GroupTabCompleter.complete(args[0], null, (Player)sender);

		} else if (args.length == 2) {
			List<String> namesToReturn = new ArrayList<String>();
			if (NameLayerPlugin.isMercuryEnabled()) {
				Set<String> players = MercuryAPI.instance.getAllPlayers();
				for (String x: players) {
					if (x.toLowerCase().startsWith(args[1].toLowerCase()))
						namesToReturn.add(x);
				}
			}
			else {
				for (Player p: Bukkit.getOnlinePlayers()) {
					if (p.getName().toLowerCase().startsWith(args[0].toLowerCase()))
						namesToReturn.add(p.getName());
				}
			}
			return namesToReturn;
		}
		else if (args.length == 3)
			return MemberTypeCompleter.complete(args[2]);

		else return null;
	}
}
