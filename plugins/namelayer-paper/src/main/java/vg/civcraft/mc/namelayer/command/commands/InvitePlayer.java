package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class InvitePlayer extends BaseCommandMiddle {

	@CommandAlias("nlip|invite|inviteplayer")
	@Syntax("<group> <player> [rank (eg: MEMBERS)]")
	@Description("Invite a player to a group.")
	@CommandCompletion("@NL_Groups @allplayers @NL_Ranks")
	public void execute(CommandSender s, String groupName, String playerName, @Optional String playerRank) {
		final String targetGroup = groupName;
		final String targetPlayer = playerName;
		final String targetType = playerRank;
		final boolean isPlayer = s instanceof Player;
		final Player p = isPlayer ? (Player)s : null;
		final boolean isAdmin = !isPlayer || p.hasPermission("namelayer.admin");
		final Group group = GroupManager.getGroup(targetGroup);
		if (groupIsNull(s, targetGroup, group)) {
			return;
		}
		if (!isAdmin && group.isDisciplined()) {
			s.sendMessage(ChatColor.RED + "This group is disiplined.");
			return;
		}
		final UUID targetAccount = NameAPI.getUUID(targetPlayer);
		if (targetAccount == null) {
			s.sendMessage(ChatColor.RED + "The player has never played before.");
			return;
		}
		final PlayerType pType = targetType != null ? PlayerType.getPlayerType(targetType) : PlayerType.MEMBERS;
		if (pType == null) {
			if (p != null) {
				PlayerType.displayPlayerTypes(p);
			} else {
				s.sendMessage("Invalid player type");
			}
			return;
		}
		if (pType == PlayerType.NOT_BLACKLISTED) {
			p.sendMessage(ChatColor.RED + "I think we both know that this shouldn't be possible.");
			return;
		}
		if (!isAdmin) {
			// Perform access check
			final UUID executor = p.getUniqueId();
			final PlayerType t = group.getPlayerType(executor); // playertype for the player running the command.
			if (t == null) {
				s.sendMessage(ChatColor.RED + "You are not on that group.");
				return;
			}
			boolean allowed = false;
			switch (pType) { // depending on the type the executor wants to add the player to
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
			if (!allowed) {
				s.sendMessage(ChatColor.RED + "You do not have permissions to modify this group.");
				return;
			}
		}
		
		if (group.isCurrentMember(targetAccount)) { // So a player can't demote someone who is above them.
			s.sendMessage(ChatColor.RED + "Player is already a member. "
					+ "Use /promoteplayer to change their PlayerType.");
			return;
		}
		if(NameLayerPlugin.getBlackList().isBlacklisted(group, targetAccount)) {
			s.sendMessage(ChatColor.RED + "This player is currently blacklisted, you have to unblacklist him with /removeblacklist before inviting him to the group");
			return;
		}
		if (!isAdmin) {
			sendInvitation(group, pType, targetAccount, p.getUniqueId(), true);
		}
		else {
			sendInvitation(group, pType, targetAccount, null, true);
		}
		
		s.sendMessage(ChatColor.GREEN + "The invitation has been sent." + "\n Use /revoke to Revoke an invite.");
	}

	public static void sendInvitation(Group group, PlayerType pType, UUID invitedPlayer, UUID inviter, boolean saveToDB){
		Player invitee = Bukkit.getPlayer(invitedPlayer);
		boolean shouldAutoAccept = NameLayerPlugin.getAutoAcceptHandler().getAutoAccept(invitedPlayer);
		if (invitee != null) {
			// invitee is online
			if (shouldAutoAccept) {
				// player auto accepts invite
				if (saveToDB) {
					group.addMember(invitedPlayer, pType);
				}
				else {
					group.addMember(invitedPlayer, pType, false);
				}
				invitee.sendMessage(
						ChatColor.GREEN + " You have auto-accepted invite to the group: " + group.getName());
			} else {
				group.addInvite(invitedPlayer, pType, saveToDB);
				PlayerListener.addNotification(invitedPlayer, group);
				String msg;
				if(inviter != null){
					String inviterName = NameAPI.getCurrentName(inviter);
					msg = "You have been invited to the group " + group.getName()
							+ " by " + inviterName + ".\n";
				} else {
					msg = "You have been invited to the group " + group.getName()+ ".\n";
				}
				TextComponent message = new TextComponent(msg + "Click this message to accept. If you wish to toggle invites "
						+ "so they always are accepted please run /autoaccept");
				message.setColor(net.md_5.bungee.api.ChatColor.GREEN);
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nlag " + group.getName()));
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("  ---  Click to accept").create()));
				invitee.spigot().sendMessage(message);
			}
		} else {
			// invitee is offline or on a different shard
			if (shouldAutoAccept) {
				if (saveToDB) {
					group.addMember(invitedPlayer, pType);
				}
				else {
					group.addMember(invitedPlayer, pType, false);
				}
			} else {
				// Player did not auto accept
				group.addInvite(invitedPlayer, pType, saveToDB);
			}
			PlayerListener.addNotification(invitedPlayer, group);
		}
	}
}
