package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;

public class GetDefaultGroup extends BaseCommandMiddle {

    @CommandAlias("nlgdg")
    @Description("Get a players default group")
    public void execute(Player sender) {
        Player p = (Player) sender;
        UUID uuid = NameAPI.getUUID(p.getName());

        String x = gm.getDefaultGroup(uuid);
        if (x == null) {
            p.sendMessage(ChatColor.RED + "You do not currently have a default group use /nlsdg to set it");
        } else {
            p.sendMessage(ChatColor.GREEN + "Your current default group is " + x);
        }
    }
}
