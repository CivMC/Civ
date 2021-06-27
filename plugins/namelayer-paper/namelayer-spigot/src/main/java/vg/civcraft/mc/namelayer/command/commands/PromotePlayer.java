package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.command.TabCompleters.MemberTypeCompleter;
import vg.civcraft.mc.namelayer.events.PromotePlayerEvent;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CommandAlias("nlpp")
public class PromotePlayer extends BaseCommandMiddle {

	@Syntax("/nlpp <group> <player> <playertype>")
	@Description("Promote/Demote a Player in a Group")
	public void execute(CommandSender sender, String groupName, String playerName, String playerType) {
		if (!(sender instanceof Player)){
			sender.sendMessage("How about No?");
			return;
		}
		
		Player p = (Player) sender;
		
		UUID executor = NameAPI.getUUID(p.getName());
		
		UUID promotee = NameAPI.getUUID(playerName);
		
		if(promotee ==null){
			p.sendMessage(ChatColor.RED + "That player does not exist");
			return;
		}
		
		if(promotee.equals(executor)){
			p.sendMessage(ChatColor.RED + "You cannot promote yourself");
			return;
		}
		
		Group group = gm.getGroup(groupName);
		if (groupIsNull(sender, groupName, group)) {
			return;
		}
		if (group.isDisciplined()){
			p.sendMessage(ChatColor.RED + "This group is disiplined.");
			return;
		}
		
		PlayerType promoteecurrentType = group.getPlayerType(promotee);
		PlayerType promoteeType = PlayerType.getPlayerType(playerType);
		if(promoteeType == null){
			PlayerType.displayPlayerTypes(p);
			return;
		}
		if(promoteeType == PlayerType.NOT_BLACKLISTED) {
			p.sendMessage(ChatColor.RED + "Nice try");
			return;
		}
		
		PlayerType t = group.getPlayerType(executor); // playertype for the player running the command.
		
		if (t == null){
			p.sendMessage(ChatColor.RED + "You are not on that group.");
			return;
		}
		
		boolean allowed = false;
		switch (promoteeType){ // depending on the type the executor wants to add the player to
		case MEMBERS:
			allowed = gm.hasAccess(group, executor, PermissionType.getPermission("MEMBERS"));
			break;
		case MODS:
			allowed = gm.hasAccess(group, executor, PermissionType.getPermission("MODS"));
			break;
		case ADMINS:
			allowed = gm.hasAccess(group, executor, PermissionType.getPermission("ADMINS"));
			break;
		case OWNER:
			allowed = gm.hasAccess(group, executor, PermissionType.getPermission("OWNER"));
			break;
		default:
			allowed = false;
			break;
		}
		
		if (!allowed){
			p.sendMessage(ChatColor.RED + "You do not have permissions to promote to this rank");
			return;
		}
		if (promoteecurrentType != null) {
			switch (promoteecurrentType){ // depending on the type the executor wants to add the player to
			case MEMBERS:
				allowed = gm.hasAccess(group, executor, PermissionType.getPermission("MEMBERS"));
				break;
			case MODS:
				allowed = gm.hasAccess(group, executor, PermissionType.getPermission("MODS"));
				break;
			case ADMINS:
				allowed = gm.hasAccess(group, executor, PermissionType.getPermission("ADMINS"));
				break;
			case OWNER:
				allowed = gm.hasAccess(group, executor, PermissionType.getPermission("OWNER"));
				break;
			default:
				allowed = false;
				break;
			}
		}
		else {
			allowed = false;
		}
		
		if (!allowed || !group.isMember(promotee)){ //can't edit a player who isn't in the group
			p.sendMessage(ChatColor.RED + NameAPI.getCurrentName(promotee) + " is not a member of this group or you do not have permission to edit their rank");
			return;
		}
		
		if (group.isOwner(promotee)){
			p.sendMessage(ChatColor.RED + "That player owns the group, you cannot "
					+ "demote the player.");
			return;
		}
		
		OfflinePlayer prom = Bukkit.getOfflinePlayer(promotee);
		if(prom.isOnline()){
			Player oProm = (Player) prom;
			PromotePlayerEvent event = new PromotePlayerEvent(oProm, group, promoteecurrentType, promoteeType);
			Bukkit.getPluginManager().callEvent(event);
			if(event.isCancelled()){
				return;
			}
			group.removeMember(promotee);
			group.addMember(promotee, promoteeType);
			p.sendMessage(ChatColor.GREEN + NameAPI.getCurrentName(promotee) + " has been added as (PlayerType) " +
					promoteeType.toString() + " in (Group) " + group.getName());
			oProm.sendMessage(ChatColor.GREEN + "You have been promoted to (PlayerType) " +
					promoteeType.toString() + " in (Group) " + group.getName());
		}
		else{
			//player is offline change their perms
			group.removeMember(promotee);
			group.addMember(promotee, promoteeType);
			p.sendMessage(ChatColor.GREEN + NameAPI.getCurrentName(promotee) + " has been added as (PlayerType) " +
					promoteeType.toString() + " in (Group) " + group.getName());
		}
	}

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
			//return GroupMemberTabCompleter.complete(args[0], args[1], (Player) sender);
		else if (args.length == 3)
			return MemberTypeCompleter.complete(args[2]);

		else return null;
	}

}
