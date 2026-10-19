package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.commands.TabComplete;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.InviteTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

public class RejectInvite extends BaseCommandMiddle {

    @CommandAlias("nlrg|reject|rejectinvite")
    @Syntax("<group>")
    @Description("Reject an invitation to a group.")
    @CommandCompletion("@NL_Invites")
    public void execute(Player sender, String targetGroup) {
        String groupName = targetGroup;
        Group group = GroupManager.getGroup(groupName);
        if (groupIsNull(sender, groupName, group)) {
            return;
        }
        UUID uuid = NameLayerAPI.getUUID(sender.getName());
        GroupManager.PlayerType type = group.getInvite(uuid);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "You were not invited to that group.");
            return;
        }
        if (group.isMember(uuid)) {
            sender.sendMessage(ChatColor.RED + "You cannot reject an invite to a group that you're already a member of.");
            return;
        }
        group.removeInviteAsync(uuid, uuid, false, result -> {
            if (result.success()) {
                sender.sendMessage(ChatColor.GREEN + "You've successfully declined that group invitation.");
            } else {
                sender.sendMessage(ChatColor.RED + result.message());
            }
        });
    }

    @TabComplete("NL_Invites")
    public List<String> tabComplete(BukkitCommandCompletionContext context) {
        return InviteTabCompleter.complete(context.getInput(), context.getPlayer());
    }
}
