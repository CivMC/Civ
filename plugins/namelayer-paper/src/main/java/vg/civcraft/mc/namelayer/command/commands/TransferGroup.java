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
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

public class TransferGroup extends BaseCommandMiddle {

    @CommandAlias("nltg|transfer|transfergroup")
    @Syntax("<group> <player>")
    @Description("Transfer one group to another person.")
    @CommandCompletion("@NL_Groups @allplayers")
    public void execute(CommandSender sender, String groupName, String playerName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nope?");
            return;
        }
        Player p = (Player) sender;
        Group g = GroupManager.getGroup(groupName);
        if (groupIsNull(sender, groupName, g)) {
            return;
        }

        UUID oPlayer = NameLayerAPI.getUUID(playerName); // uuid of the second player

        if (oPlayer == null) {
            p.sendMessage(ChatColor.RED + "This player has never played before and cannot be given the group.");
            return;
        }

        attemptTransfer(g, p, oPlayer);
    }

    public static boolean attemptTransfer(Group g, Player owner, UUID futureOwner) {
        GroupManager gm = NameLayerAPI.getGroupManager();
        if (!g.isOwner(owner.getUniqueId()) && !(owner.isOp() || owner.hasPermission("namelayer.admin"))) {
            owner.sendMessage(ChatColor.RED
                + "You don't own this group");
            return false;
        }
        if (g.isDisciplined()) {
            owner.sendMessage(ChatColor.RED
                + "This group is disciplined.");
            return false;
        }
        if (NameLayerPlugin.getInstance().getGroupLimit() < gm
            .countGroups(futureOwner) + 1) {
            owner.sendMessage(ChatColor.RED
                + NameLayerAPI.getCurrentName(futureOwner)
                + " cannot receive the group! This player has already reached the group limit count.");
            return false;
        }
        if (!g.isMember(futureOwner)) {
            owner.sendMessage(ChatColor.RED
                + NameLayerAPI.getCurrentName(futureOwner)
                + " is not a member of the group and can't be made primary owner!");
            return false;
        }
        g.removeMember(futureOwner);
        g.addMember(futureOwner, GroupManager.PlayerType.OWNER);
        g.setOwner(futureOwner);
        owner.sendMessage(ChatColor.GREEN + NameLayerAPI.getCurrentName(futureOwner)
            + " has been given ownership of the group and promoted to OWNER role.");
        return true;
    }
}
