package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.commands.TabComplete;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.InviteTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

public class AcceptInvite extends BaseCommandMiddle {

    @CommandAlias("nlacceptinvite|nlacceptgroup|acceptinvite|acceptgroup|accept|join|ctjoin|ctj|ag|nlag")
    @Syntax("<group>")
    @Description("Accept an invitation to a group.")
    @CommandCompletion("@NL_Invites")
    public void execute(CommandSender sender, String groupName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        Group group = gm.getGroup(groupName);
        if (groupIsNull(sender, groupName, group)) {
            return;
        }
        UUID uuid = NameAPI.getUUID(player.getName());
        PlayerType type = group.getInvite(uuid);
        if (type == null) {
            player.sendMessage(Component.text("You were not invited to that group.").color(NamedTextColor.RED));
            return;
        }
        if (group.isDisciplined()) {
            player.sendMessage(Component.text("That group is disiplined.").color(NamedTextColor.RED));
            return;
        }
        if (group.isMember(uuid)) {
            player.sendMessage(Component.text("You are already a member you cannot join again.").color(NamedTextColor.RED));
            group.removeInvite(uuid, true);
            return;
        }
        group.addMember(uuid, type);
        group.removeInvite(uuid, true);
        PlayerListener.removeNotification(uuid, group);
        player.sendMessage(Component.text("You have successfully been added to the group as a " + type.name() + ".").color(NamedTextColor.GREEN));
    }

    @TabComplete("NL_Invites")
    public List<String> tabComplete(BukkitCommandCompletionContext context) {
        return InviteTabCompleter.complete(context.getInput(), context.getPlayer());
    }
}
