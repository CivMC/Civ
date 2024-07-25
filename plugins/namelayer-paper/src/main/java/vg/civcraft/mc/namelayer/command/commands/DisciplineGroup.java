package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

public class DisciplineGroup extends BaseCommandMiddle {

    @CommandAlias("nldig|disablegroup|disable|discipline")
    @CommandPermission("namelayer.admin")
    @Syntax("<group>")
    @Description("Disable a group from working.")
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
        if (!player.isOp() || !player.hasPermission("namelayer.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission for this op command.");
            return;
        }
        if (g.isDisciplined()) {
            g.setDisciplined(false);
            sender.sendMessage(ChatColor.GREEN + "Group has been enabled.");
        } else {
            g.setDisciplined(true);
            sender.sendMessage(ChatColor.GREEN + "Group has been disabled.");
        }
    }
}
