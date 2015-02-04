package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.command.TabCompleters.MemberTypeCompleter;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.groups.PrivateGroup;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class InvitePlayer extends PlayerCommand{

	private GroupManagerDao db = NameLayerPlugin.getGroupManagerDao();
	public InvitePlayer(String name) {
		super(name);
		setIdentifier("nlip");
		setDescription("This command is used to invite a player to the PlayerType " + PlayerType.toStringName() + " of a group.");
		setUsage("/nlip <group> <player> (PlayerType- default MEMBERS)");
		setArguments(2,3);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "I'm sorry baby, please run this as a player :)");
			return true;
		}
		Player p = (Player) sender;
		Group group = gm.getGroup(args[0]);
		if (group == null){
			p.sendMessage(ChatColor.RED + "That group does not exist.");
			return true;
		}
		if (group.isDisciplined()){
			p.sendMessage(ChatColor.RED + "This group is disiplined.");
			return true;
		}
		UUID executor = NameAPI.getUUID(p.getName());
		PlayerType pType = PlayerType.MEMBERS;
		if (args.length == 3)
			pType = PlayerType.getPlayerType(args[2]);
		if (pType == null){
			PlayerType.displayPlayerTypes(p);
			return true;
		}
		UUID uuid = NameAPI.getUUID(args[1]);
		if (uuid == null){
			p.sendMessage(ChatColor.RED + "The player has never played before.");
			return true;
		}
		
		GroupPermission perm = gm.getPermissionforGroup(group);
		PlayerType t = group.getPlayerType(executor); // playertype for the player running the command.
		if (t == null){
			p.sendMessage(ChatColor.RED + "You are not on that group.");
			return true;
		}
		boolean allowed = false;
		switch (pType){ // depending on the type the executor wants to add the player to
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
		if (!allowed){
			p.sendMessage(ChatColor.RED + "You do not have permissions to modify this group.");
			return true;
		}
		
		if (group.isMember(uuid)){ // So a player can't demote someone who is above them.
			p.sendMessage(ChatColor.RED + "Player is already a member. They "
					+ "must be removed first before they can be change PlayerTypes.");
			return true;
		}
		
		group.addInvite(uuid, pType);
		OfflinePlayer invitee = Bukkit.getOfflinePlayer(uuid);
		boolean shouldAutoAccept = db.shouldAutoAcceptGroups(invitee.getUniqueId());
		if (invitee.isOnline() && !shouldAutoAccept){
			Player oInvitee = (Player) invitee;
			oInvitee.sendMessage(ChatColor.GREEN + "You have been invited to the group " + group.getName() +" by " + p.getName() +".\n" +
					"Use the command /nlag <group> to accept.\n"
					+ "If you wish to toggle invites so they always are accepted please run /nltaai");
			p.sendMessage(ChatColor.GREEN + "The invitation has been sent.");
		}
		else if(shouldAutoAccept && !invitee.isOnline()){
			PlayerListener.addNotification(uuid, group);
			p.sendMessage(ChatColor.GREEN + "Player is offline and will be notified on log in.");
		}
		else{
			p.sendMessage(ChatColor.GREEN + "Player is offline and will be notified on log in.");
		}
		PlayerListener.addNotification(uuid, group);
		if (db.shouldAutoAcceptGroups(invitee.getUniqueId())){
			group.addMember(uuid, pType);
			group.removeRemoveInvite(uuid);
			PlayerListener.removeNotification(uuid, group);
			if (group instanceof PrivateGroup){
				PrivateGroup priv = (PrivateGroup) group;
				List<Group> groups = priv.getSubGroups();
				for (Group g: groups){
					g.addMember(uuid, PlayerType.SUBGROUP);
				}
			}
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

		} else if (args.length == 2)
			return null;

		else if (args.length == 3)
			return MemberTypeCompleter.complete(args[2]);

		else return null;
	}
}
