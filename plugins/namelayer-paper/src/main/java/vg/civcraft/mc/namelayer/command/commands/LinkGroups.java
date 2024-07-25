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
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class LinkGroups extends BaseCommandMiddle {

    @CommandAlias("nllink|linkgroups")
    @Syntax("<super group> <sub group>")
    @Description("Links two groups to each other as nested groups.")
    @CommandCompletion("@NL_Groups @NL_Groups")
    public void execute(CommandSender sender, String parentGroup, String childGroup) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }

        String supername = parentGroup, subname = childGroup;

        Group supergroup = GroupManager.getGroup(supername);
        if (groupIsNull(sender, supername, supergroup)) {
            return;
        }

        Group subgroup = GroupManager.getGroup(subname);
        if (groupIsNull(sender, subname, subgroup)) {
            return;
        }

        if (subgroup.getName().equalsIgnoreCase(supergroup.getName())) {
            player.sendMessage(ChatColor.RED + "Not today");
            return;
        }

        // check if groups are accessible

        UUID uuid = NameAPI.getUUID(player.getName());

        if (!supergroup.isMember(uuid) || !subgroup.isMember(uuid)) {
            player.sendMessage(ChatColor.RED + "You're not on one of the groups.");
            return;
        }

        if (supergroup.isDisciplined() || subgroup.isDisciplined()) {
            player.sendMessage(ChatColor.RED + "One of the groups is disciplined.");
            return;
        }

        if (!gm.hasAccess(subgroup, uuid, PermissionType.getPermission("LINKING"))) {
            player.sendMessage(ChatColor.RED
                + "You don't have permission to do that on the sub group.");
            return;
        }
        if (!gm.hasAccess(supergroup, uuid, PermissionType.getPermission("LINKING"))) {
            player.sendMessage(ChatColor.RED
                + "You don't have permission to do that on the super group.");
            return;
        }

        if (Group.areLinked(supergroup, subgroup)) {
            player.sendMessage(ChatColor.RED + "These groups are already linked.");
            return;
        }

        boolean success = Group.link(supergroup, subgroup, true);

        String message;
        if (success) {
            message = ChatColor.GREEN + "The groups have been successfully linked.";
        } else {
            message = ChatColor.RED + "Failed to link the groups.";
        }
        player.sendMessage(message);
    }
}
