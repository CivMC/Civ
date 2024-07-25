package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class MergeGroups extends BaseCommandMiddle {

    @CommandAlias("nlmg|merge|mergegroups")
    @Syntax("<group_to_keep> <group_to_delete>")
    @Description("Merge two groups together.")
    @CommandCompletion("@NL_Groups @NL_Groups")
    public void execute(CommandSender sender, String groupToKeep, String groupToDelete) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        final Group g = GroupManager.getGroup(groupToKeep);
        if (groupIsNull(sender, groupToKeep, g)) {
            return;
        }

        final Group toMerge = GroupManager.getGroup(groupToDelete);
        if (groupIsNull(sender, groupToDelete, toMerge)) {
            return;
        }

        if (g.isDisciplined() || toMerge.isDisciplined()) {
            player.sendMessage(ChatColor.RED + "One of the groups is disiplined.");
            return;
        }

        if (g == toMerge) {
            player.sendMessage(ChatColor.RED + "You cannot merge a group into itself");
            return;
        }

        UUID uuid = NameAPI.getUUID(player.getName());
        if (!gm.hasAccess(g, uuid, PermissionType.getPermission("MERGE"))) {
            player.sendMessage(ChatColor.RED + "You don't have permission on group " + g.getName() + ".");
            return;
        }
        if (!gm.hasAccess(toMerge, uuid, PermissionType.getPermission("MERGE"))) {
            player.sendMessage(ChatColor.RED + "You don't have permission on group " + toMerge.getName() + ".");
            return;
        }
        try {
            gm.mergeGroup(g, toMerge);
            player.sendMessage(ChatColor.GREEN + "Group merging is completed.");
        } catch (Exception e) {
            NameLayerPlugin.getInstance().getLogger().log(Level.SEVERE, "Group merging failed", e);
            player.sendMessage(ChatColor.GREEN + "Group merging may have failed.");
        }
        player.sendMessage(ChatColor.GREEN + "Group is under going merge.");
    }
}
