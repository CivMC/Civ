package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.google.common.collect.Lists;
import java.util.List;
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
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ListMembers extends BaseCommandMiddle {

    @CommandAlias("nllm|listmembers|members")
    @Syntax("<group> [rank (e.g: MEMBERS)]")
    @Description("List the members in a group")
    @CommandCompletion("@NL_Groups @NL_Ranks")
    public void execute(CommandSender sender, String groupName, @Optional String playerType, @Optional String playerName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        UUID uuid = NameAPI.getUUID(player.getName());
        String groupname = groupName;

        Group group = GroupManager.getGroup(groupname);
        if (groupIsNull(sender, groupname, group)) {
            return;
        }

        if (!player.hasPermission("namelayer.admin")) {
            if (!group.isMember(uuid)) {
                player.sendMessage(ChatColor.RED + "You're not on this group.");
                return;
            }

            if (!gm.hasAccess(group, uuid, PermissionType.getPermission("GROUPSTATS"))) {
                player.sendMessage(ChatColor.RED
                    + "You don't have permission to run that command.");
                return;
            }
        }

        List<UUID> uuids = null;
        if (playerType != null && playerName != null) {
            String nameMin = playerType, nameMax = playerName;

            List<UUID> members = group.getAllMembers();
            uuids = Lists.newArrayList();

            for (UUID member : members) {
                String name = NameAPI.getCurrentName(member);
                if (name.compareToIgnoreCase(nameMin) >= 0
                    && name.compareToIgnoreCase(nameMax) <= 0) {
                    uuids.add(member);
                }
            }
        } else if (playerType != null && playerName == null) {
            String playerRank = playerType;
            PlayerType filterType = PlayerType.getPlayerType(playerRank);

            if (filterType == null) {
                // user entered invalid type, show them
                PlayerType.displayPlayerTypes(player);
                return;
            }

            uuids = group.getAllMembers(filterType);
        } else {
            uuids = group.getAllMembers();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GREEN);
        sb.append("Members are as follows:\n");
        for (UUID uu : uuids) {
            sb.append(NameAPI.getCurrentName(uu));
            sb.append(" (");
            sb.append(group.getPlayerType(uu));
            sb.append(")\n");
        }

        player.sendMessage(sb.toString());
    }
}
