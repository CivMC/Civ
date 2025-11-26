package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class RevokeInvite extends BaseCommandMiddle {

    @CommandAlias("nlri|revokeinvite|revoke")
    @Syntax("<group> <player>")
    @Description("Revoke an Invite.")
    @CommandCompletion("@NL_Groups @allplayers")
    public void execute(CommandSender sender, String groupName, String targetPlayer) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "I'm sorry baby, please run this as a player :)");
            return;
        }
        Player p = (Player) sender;
        Group group = GroupManager.getGroup(groupName);
        if (groupIsNull(sender, groupName, group)) {
            return;
        }
        if (group.isDisciplined()) {
            p.sendMessage(ChatColor.RED + "This group is disiplined.");
            return;
        }
        UUID executor = NameLayerAPI.getUUID(p.getName());
        UUID uuid = NameLayerAPI.getUUID(targetPlayer);

        if (uuid == null) {
            p.sendMessage(ChatColor.RED + "The player has never played before.");
            return;
        }

        PlayerType t = group.getPlayerType(executor); // playertype for the player running the command.
        if (t == null) {
            p.sendMessage(ChatColor.RED + "You are not on that group.");
            return;
        }

        //get invitee PlayerType
        PlayerType pType = group.getInvite(uuid);
        if (pType == null) {
            p.sendMessage(ChatColor.RED + NameLayerAPI.getCurrentName(uuid) + " does not have an invite to that group.");
            return;
        }

        //check invitee has invite
        if (group.isMember(uuid)) {
            p.sendMessage(ChatColor.RED + NameLayerAPI.getCurrentName(uuid) + " is already part of that group, "
                + "use /remove to remove them.");
            return;
        }

        boolean allowed = switch (pType) { // depending on the type the executor wants to add the player to
            case MEMBERS -> gm.hasAccess(group, executor, PermissionType.getPermission("MEMBERS"));
            case MODS -> gm.hasAccess(group, executor, PermissionType.getPermission("MODS"));
            case ADMINS -> gm.hasAccess(group, executor, PermissionType.getPermission("ADMINS"));
            case OWNER -> gm.hasAccess(group, executor, PermissionType.getPermission("OWNER"));
            default -> false;
        };
        if (!allowed) {
            p.sendMessage(ChatColor.RED + "You do not have permissions to modify this group.");
            return;
        }

        group.removeInvite(uuid, true);
        PlayerListener.removeNotification(uuid, group);

        p.sendMessage(ChatColor.GREEN + NameLayerAPI.getCurrentName(uuid) + "'s invitation has been revoked.");
    }
}
