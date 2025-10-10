package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import java.util.List;
import java.util.UUID;

public class ListInvites extends BaseCommandMiddle {

    @CommandAlias("nllim|listinvites|listinvitedmembers")
    @Syntax("<group> [rank (e.g: MEMBERS)]")
    @Description("List the invitees of a group")
    @CommandCompletion("@NL_Groups @NL_Ranks")
    public void execute(Player sender, String groupName, @Optional String playerType, @Optional String playerName) {
        UUID uuid = NameLayerAPI.getUUID(sender.getName());

        Group group = GroupManager.getGroup(groupName);
        if (groupIsNull(sender, groupName, group)) {
            return;
        }

        assert group != null;
        if (!sender.hasPermission("namelayer.admin")) {
            if (!group.isMember(uuid)) {
                sender.sendMessage(ChatColor.RED + "You're not on this group.");
                return;
            }

            if (!gm.hasAccess(group, uuid, PermissionType.getPermission("GROUPSTATS"))) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run that command.");
                return;
            }
        }

        List<UUID> uuids;
        if (playerType != null && playerName != null) {
            List<UUID> invitees = group.getAllInvites();
            uuids = Lists.newArrayList();

            for (UUID invitee : invitees) {
                String name = NameLayerAPI.getCurrentName(invitee);
                if (name.compareToIgnoreCase(playerType) >= 0
                    && name.compareToIgnoreCase(playerName) <= 0) {
                    uuids.add(invitee);
                }
            }
        } else if (playerType != null) {
            PlayerType filterType = PlayerType.getPlayerType(playerType);
            if (filterType == null) {
                // user entered invalid type, show them
                PlayerType.displayPlayerTypes(sender);
                return;
            }

            uuids = group.getAllInvites(filterType);
        } else {
            uuids = group.getAllInvites();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GREEN);

        if (!uuids.isEmpty()) {
            sb.append("Invites are as follows:\n");
            for (UUID uu : uuids) {
                sb.append(NameLayerAPI.getCurrentName(uu));
                sb.append(" (");
                sb.append(group.getPlayerInviteType(uu));
                sb.append(")\n");
            }
        } else sb.append("No invites found");

        sender.sendMessage(sb.toString());
    }
}
