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

public class AddBlacklist extends BaseCommandMiddle {

    @CommandAlias("nlbl|blacklist|addblacklist")
    @Syntax("<group> <player>")
    @Description("Blacklist a player for a specific group")
    @CommandCompletion("@NL_Groups @allplayers")
    public void execute(CommandSender sender, String groupName, String playerName) {
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
        UUID targetUUID = NameAPI.getUUID(playerName);
        if (targetUUID == null) {
            player.sendMessage(ChatColor.RED + "This player does not exist");
            return;
        }
        if (g.isMember(targetUUID)) {
            player.sendMessage(ChatColor.RED + "You can't blacklist members of a group");
            return;
        }
        BlackList bl = NameLayerPlugin.getBlackList();
        if (bl.isBlacklisted(g, targetUUID)) {
            player.sendMessage(ChatColor.RED + "This player is already blacklisted");
            return;
        }
        bl.addBlacklistMember(g, targetUUID, true);
        player.sendMessage(ChatColor.GREEN + NameAPI.getCurrentName(targetUUID) + " was successfully blacklisted on the group " + g.getName());
    }
}
