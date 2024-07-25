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
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.BlackList;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class RemoveBlacklist extends BaseCommandMiddle {

    @CommandAlias("nlubl|unblacklist")
    @Syntax("<group> <player>")
    @Description("Removes a player from the blacklist for a specific group")
    @CommandCompletion("@NL_Groups @allplayers")
    public void execute(CommandSender sender, String groupName, String targetPlayer) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        Group g = GroupManager.getGroup(groupName);
        if (g == null) {
            player.sendMessage(ChatColor.RED + "This group does not exist");
            return;
        }
        if (!gm.hasAccess(g, player.getUniqueId(),
            PermissionType.getPermission("BLACKLIST"))
            && !(player.isOp() || player.hasPermission("namelayer.admin"))) {
            player.sendMessage(ChatColor.RED + "You do not have the required permissions to do this");
            return;
        }
        UUID targetUUID = NameAPI.getUUID(targetPlayer);
        if (targetUUID == null) {
            player.sendMessage(ChatColor.RED + "This player does not exist");
            return;
        }
        BlackList bl = NameLayerPlugin.getBlackList();
        if (!bl.isBlacklisted(g, targetUUID)) {
            player.sendMessage(ChatColor.RED + "This player is not blacklisted");
            return;
        }
        bl.removeBlacklistMember(g, targetUUID, true);
        player.sendMessage(ChatColor.GREEN + NameAPI.getCurrentName(targetUUID) + " was successfully removed from the blacklist for the group " + g.getName());
    }
}
