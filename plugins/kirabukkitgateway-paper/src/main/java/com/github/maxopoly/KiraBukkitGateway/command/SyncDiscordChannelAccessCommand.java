package com.github.maxopoly.KiraBukkitGateway.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class SyncDiscordChannelAccessCommand extends BaseCommand {

    @CommandAlias("syncdiscordchannel")
    @Description("Syncs access of the linked Discord channel")
    @Syntax("<group>")
    public void execute(CommandSender sender, String groupName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        Group group = GroupManager.getGroup(groupName);
        if (group == null) {
            player.sendMessage(ChatColor.RED + "That group does not exist");
            return;
        }
        if (!NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(),
            PermissionType.getPermission("KIRA_MANAGE_CHANNEL"))) {
            player.sendMessage(ChatColor.RED + "You do not have permission to do that");
            return;
        }
        GroupManager gm = NameAPI.getGroupManager();
        PermissionType perm = PermissionType.getPermission("READ_CHAT");
        Collection<UUID> members = new HashSet<>();
        group.getAllMembers().stream().filter(m -> gm.hasAccess(group, player.getUniqueId(), perm))
            .forEach(m -> members.add(m));
        KiraBukkitGatewayPlugin.getInstance().getRabbit().syncGroupChatAccess(group.getName(), members,
            player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Attempting to sync members...");
    }
}
