package vg.civcraft.mc.namelayer.command.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class InvitePlayer extends PlayerCommand{

	public InvitePlayer(String name) {
		super(name);
		setDescription("This command is used to invite a player to the PlayerType of a group.");
		setUsage("/groupsinviteplayer <group> <PlayerType> <player>");
		setIdentifier("groupsinviteplayer");
		setArguments(3,3);
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
		PlayerType pType = PlayerType.getPlayerType(args[1]);
		if (pType == null){
			PlayerType.displayPlayerTypes(p);
			return true;
		}
		UUID uuid = NameAPI.getUUID(args[2]);
		if (uuid == null){
			p.sendMessage(ChatColor.RED + "The player has never played before.");
			return true;
		}
		
		GroupPermission perm = gm.getPermissionforGroup(group);
		PlayerType t = group.getPlayerType(executor); // playertype for the player running the command.
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
		p.sendMessage(ChatColor.GREEN + "The invitation has been sent.");
		
		if (invitee.isOnline()){
			Player oInvitee = (Player) invitee;
			oInvitee.sendMessage(ChatColor.GREEN + "You have been invited to the group " + group.getName() +" by " + p.getName() +".\n" +
					"Use the command /groupsaccept <group> to accept.");
			p.sendMessage(ChatColor.GREEN + "Invite sent.");
		}
		else
			p.sendMessage(ChatColor.GREEN + "Player is offline and cannot be added right now.");
		return true;
	}

}
