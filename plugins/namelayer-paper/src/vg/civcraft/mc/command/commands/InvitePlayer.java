package vg.civcraft.mc.command.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.GroupManager.PlayerType;
import vg.civcraft.mc.NameAPI;
import vg.civcraft.mc.command.PlayerCommand;
import vg.civcraft.mc.group.Group;
import vg.civcraft.mc.permission.GroupPermission;
import vg.civcraft.mc.permission.PermissionType;

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
		UUID executor = NameAPI.getUUID(p.getName());
		PlayerType pType = PlayerType.valueOf(args[1]);
		if (pType == null){
			String types = "";
			for (PlayerType type: PlayerType.values())
				types += type.name() + " ";
			p.sendMessage(ChatColor.RED +"That PlayerType does not exists.\n" +
					"The current types are: " + types);
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
			allowed = perm.isAccessible(PermissionType.MEMBERS, t);
			break;
		case MODS:
			allowed = perm.isAccessible(PermissionType.MODS, t);
			break;
		case ADMINS:
			allowed = perm.isAccessible(PermissionType.ADMINS, t);
			break;
		case OWNER:
			allowed = perm.isAccessible(PermissionType.OWNER, t);
			break;
		}
		if (!allowed){
			p.sendMessage(ChatColor.RED + "You do not have permissions to modify this group.");
			return true;
		}
		
		group.addInvite(uuid, pType);
		OfflinePlayer invitee = Bukkit.getOfflinePlayer(uuid);
		if (!invitee.isOnline())
			return true;
		Player oInvitee = (Player) invitee;
		oInvitee.sendMessage(ChatColor.GREEN + "You have been invited to the group " + group.getName() +" by " + p.getName() +".\n" +
				"Use the command /groupsacceptinvite <group> to accept.");
		return true;
	}

}
