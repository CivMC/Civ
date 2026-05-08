package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

public class DisciplineGroup extends BaseCommandMiddle {

    @CommandAlias("nldig|disablegroup|disable|discipline")
    @CommandPermission("namelayer.admin")
    @Syntax("<group>")
    @Description("Disable a group from working.")
    @CommandCompletion("@NL_Groups")
    public void execute(Player sender, String groupName) {
        Player p = (Player) sender;
        Group g = gm.getGroup(groupName);
        if (groupIsNull(sender, groupName, g)) {
            return;
        }
        if (!p.isOp() || !p.hasPermission("namelayer.admin")) {
            p.sendMessage(ChatColor.RED + "You do not have permission for this op command.");
            return;
        }
        final boolean disciplined = !g.isDisciplined();
        g.setDisciplinedAsync(p.getUniqueId(), disciplined, true, result -> {
            if (result.success()) {
                sender.sendMessage(ChatColor.GREEN + (disciplined ? "Group has been disabled." : "Group has been enabled."));
            } else {
                sender.sendMessage(ChatColor.RED + result.message());
            }
        });
    }
}
