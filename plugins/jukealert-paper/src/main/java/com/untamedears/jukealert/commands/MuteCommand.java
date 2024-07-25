package com.untamedears.jukealert.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.util.JASettingsManager;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

public class MuteCommand extends BaseCommand {

    @CommandAlias("jamute")
    @Syntax("<group>")
    @Description("Adds or removes from a snitch notification ignore list.")
    public void execute(CommandSender sender, String targetGroup) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        if (targetGroup == null) {
            return;
        }
        Group group = GroupManager.getGroup(targetGroup);
        if (group == null) {
            player.sendMessage(ChatColor.RED + "The group " + targetGroup + " does not exist");
            return;
        }
        JASettingsManager settingsManager = JukeAlert.getInstance().getSettingsManager();
        if (settingsManager.doesIgnoreAlert(group.getName(), player.getUniqueId())) {
            settingsManager.getIgnoredGroupAlerts().removeElement(player.getUniqueId(), group.getName());
            player.sendMessage(ChatColor.GREEN + "You have unmuted " + group.getName());
            return;
        }
        settingsManager.getIgnoredGroupAlerts().addElement(player.getUniqueId(), group.getName());
        player.sendMessage(ChatColor.GREEN + "You have muted " + group.getName());
    }
}
