package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
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
        final Player p = isPlayer ? (Player) s : null;
        final boolean isAdmin = !isPlayer || p.hasPermission("namelayer.admin");
        final Group group = GroupManager.getGroup(targetGroup);
        if (groupIsNull(s, targetGroup, group)) {
            return;
        }
        if (!isAdmin && group.isDisciplined()) {
            s.sendMessage(ChatColor.RED + "This group is disciplined.");
            return;
        }
        final UUID targetAccount = NameLayerAPI.getUUID(targetPlayer);
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
        if (NameLayerPlugin.getBlackList().isBlacklisted(group, targetAccount)) {
            s.sendMessage(ChatColor.RED + "This player is currently blacklisted, you have to unblacklist him with /removeblacklist before inviting him to the group");
            return;
        }
        final UUID actorUuid = isPlayer ? p.getUniqueId() : targetAccount;
        sendInvitation(group, pType, targetAccount, actorUuid, isAdmin, result -> {
            if (result.success()) {
                s.sendMessage(ChatColor.GREEN + "The invitation has been sent." + "\n Use /revoke to Revoke an invite.");
            } else {
                s.sendMessage(ChatColor.RED + result.message());
            }
        });
    }

    public static void sendInvitation(
        Group group,
        PlayerType pType,
        UUID invitedPlayer,
        UUID inviter,
        boolean adminOverride,
        java.util.function.Consumer<Group.MemberWriteResult> callback
    ) {
        group.addInviteAsync(inviter == null ? invitedPlayer : inviter, invitedPlayer, pType, adminOverride, inviter != null, callback);
    }
}
