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
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class RemoveMember extends BaseCommandMiddle {

    @CommandAlias("nlrm|remove|removeplayer")
    @Syntax("<group> <player>")
    @Description("Remove a member from a group.")
    @CommandCompletion("@NL_Groups @allplayers")
    public void execute(CommandSender sender, String groupName, String targetPlayer) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        Group group = gm.getGroup(groupName);
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

        String playerName = NameAPI.getCurrentName(uuid);
        PlayerType toBeRemoved = group.getPlayerType(uuid);
        if (toBeRemoved == null) {
            //hides who is actually on the group
            toBeRemoved = PlayerType.MEMBERS;
        }
        boolean allowed = false;
        switch (toBeRemoved) { // depending on the type the executor wants to add the player to
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
        }

        if (!allowed && !(player.isOp() || player.hasPermission("namelayer.admin"))) {
            player.sendMessage(ChatColor.RED + "You do not have permissions to modify this group.");
            return;
        }

        if (!group.isMember(uuid)) {
            player.sendMessage(ChatColor.RED + "That player is not on the group.");
            return;
        }

        if (group.isOwner(uuid)) {
            player.sendMessage(ChatColor.RED + "That player owns the group, you cannot "
                + "remove the player.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + playerName + " has been removed from the group.");
        group.removeMember(uuid);
    }
}
