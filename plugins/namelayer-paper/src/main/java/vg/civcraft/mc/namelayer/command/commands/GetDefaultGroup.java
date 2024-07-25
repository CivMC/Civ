package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;

public class GetDefaultGroup extends BaseCommandMiddle {

    @CommandAlias("nlgdg")
    @Description("Get a players default group")
    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        UUID uuid = NameAPI.getUUID(player.getName());

        String x = gm.getDefaultGroup(uuid);
        if (x == null) {
            player.sendMessage(ChatColor.RED + "You do not currently have a default group use /nlsdg to set it");
        } else {
            player.sendMessage(ChatColor.GREEN + "Your current default group is " + x);
        }
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return null;

        if (args.length == 1)
            return GroupTabCompleter.complete(args[0], null, (Player) sender);
        else {
            return GroupTabCompleter.complete(null, null, (Player) sender);
        }
    }
}
