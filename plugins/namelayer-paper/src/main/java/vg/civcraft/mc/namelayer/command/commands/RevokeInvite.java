package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        Group group = GroupManager.getGroup(groupName);
        if (groupIsNull(sender, groupName, group)) {
            return;
        }
        if (group.isDisciplined()) {
            player.sendMessage(ChatColor.RED + "This group is disiplined.");
            return;
        }
        UUID executor = NameAPI.getUUID(player.getName());
        UUID uuid = NameAPI.getUUID(targetPlayer);

        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "The player has never played before.");
            return;
        }

        //check invitee has invite
        if (group.getInvite(uuid) == null) {
            if (group.isMember(uuid)) {
                player.sendMessage(ChatColor.RED + NameAPI.getCurrentName(uuid) + " is already part of that group, "
                    + "use /remove to remove them.");
                return;
            }
            player.sendMessage(ChatColor.RED + NameAPI.getCurrentName(uuid) + " does not have an invite to that group.");
            return;
        }

        //get invitee PlayerType
        PlayerType pType = group.getInvite(uuid);

        PlayerType t = group.getPlayerType(executor); // playertype for the player running the command.
        if (t == null) {
            player.sendMessage(ChatColor.RED + "You are not on that group.");
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
            player.sendMessage(ChatColor.RED + "You do not have permissions to modify this group.");
            return;
        }

        group.removeInvite(uuid, true);
        PlayerListener.removeNotification(uuid, group);

        player.sendMessage(ChatColor.GREEN + NameAPI.getCurrentName(uuid) + "'s invitation has been revoked.");
    }
}
