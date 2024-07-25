package com.untamedears.jukealert.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.gui.SnitchLogGUI;
import com.untamedears.jukealert.gui.SnitchOverviewGUI;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GUICommand extends BaseCommand {

    @CommandAlias("ja")
    @Description("Opens snitch log GUI")
    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        Snitch cursorSnitch = JAUtility.getSnitchUnderCursor(player);
        if (cursorSnitch != null && cursorSnitch.hasPermission(player, JukeAlertPermissionHandler.getReadLogs())) {
            SnitchLogGUI gui = new SnitchLogGUI(player, cursorSnitch);
            gui.showScreen();
            return;
        }
        // No snitch under cursor, so search around player
        Collection<Snitch> snitches = JukeAlert.getInstance().getSnitchManager()
            .getSnitchesCovering(player.getLocation());
        // Sort out the ones the player has no perms for
        Iterator<Snitch> iter = snitches.iterator();
        while (iter.hasNext()) {
            Snitch snitch = iter.next();
            if (!snitch.hasPermission(player, JukeAlertPermissionHandler.getReadLogs())) {
                iter.remove();
            }
        }
        if (snitches.isEmpty()) {
            player.sendMessage(
                ChatColor.RED + "You do not own any snitches nearby or lack permission to view their logs!");
            return;
        }
        if (snitches.size() == 1) {
            SnitchLogGUI gui = new SnitchLogGUI(player, snitches.iterator().next());
            gui.showScreen();
            return;
        }
        SnitchOverviewGUI gui = new SnitchOverviewGUI(player, new ArrayList<>(snitches), "Nearby snitches", true);
        gui.showScreen();
    }
}
