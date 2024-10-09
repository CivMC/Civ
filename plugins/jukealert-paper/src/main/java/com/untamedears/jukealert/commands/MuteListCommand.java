package com.untamedears.jukealert.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.util.JASettingsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import java.util.Set;

public class MuteListCommand extends BaseCommand {

    @CommandAlias("jamutelist")
    @Description("Lists all groups you have snitch notifications ignored.")
    public void execute(Player sender){
        StringBuilder content = new StringBuilder();
        JASettingsManager settingsManager = JukeAlert.getInstance().getSettingsManager();
        Set<String> ignoredGroups = settingsManager.getIgnoredGroupAlerts().getValue(sender.getUniqueId());
        if (ignoredGroups == null || ignoredGroups.isEmpty()) {
            sender.sendMessage(Component.text("You do not have any groups ignored.", NamedTextColor.YELLOW));
            return;
        }
        for (String group : ignoredGroups) {
            content.append(group + "\n ");
        }
        sender.sendMessage(Component.text("You have the following groups ignored: \n" + content, NamedTextColor.YELLOW));
    }
}
