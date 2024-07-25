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
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

public class SetDefaultGroup extends BaseCommandMiddle {

    @CommandAlias("nlsdg")
    @Syntax("<group>")
    @Description("Set or change a default group")
    @CommandCompletion("@NL_Groups")
    public void execute(CommandSender sender, String groupName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        UUID uuid = NameAPI.getUUID(player.getName());
        Group g = gm.getGroup(groupName);
        if (groupIsNull(sender, groupName, g)) {
            return;
        }

        PlayerType pType = g.getPlayerType(uuid);
        if (pType == null) {
            player.sendMessage(ChatColor.RED + "You do not have access to that group.");
            return;
        }

        String x = gm.getDefaultGroup(uuid);
        if (x == null) {
            g.setDefaultGroup(uuid);
            player.sendMessage(ChatColor.GREEN + "You have set your default group to " + g.getName());
        } else {
            g.changeDefaultGroup(uuid);
            player.sendMessage(ChatColor.GREEN + "You changed your default group from " + x + " to " + gm.getDefaultGroup(uuid));
        }
    }
}
