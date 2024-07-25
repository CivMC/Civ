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
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

public class LeaveGroup extends BaseCommandMiddle {

    @CommandAlias("nlleg|leave|leavegroup")
    @Syntax("<group>")
    @Description("Leave a group")
    @CommandCompletion("@NL_Groups")
    public void execute(CommandSender sender, String groupName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        Group g = gm.getGroup(groupName);
        if (groupIsNull(sender, groupName, g)) {
            return;
        }
        UUID uuid = NameAPI.getUUID(player.getName());
        if (!g.isCurrentMember(uuid)) {
            player.sendMessage(ChatColor.RED + "You are not a member of this group.");
            return;
        }
        if (g.isDisciplined()) {
            player.sendMessage(ChatColor.RED + "This group is disciplined.");
            return;
        }
        g.removeMember(uuid);
        player.sendMessage(ChatColor.GREEN + "You have been removed from the group.");
    }
}
