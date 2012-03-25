package com.untamedears.citadel.command;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.PlayerState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import static com.untamedears.citadel.Utility.sendMessage;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 11:08 AM
 */
public class ToggleBypass extends PlayerCommand implements CommandExecutor {
    public ToggleBypass(Citadel plugin) {
        super(plugin, 0);
    }

    public boolean onCommand(Command command, String[] args) {
        String status = PlayerState.get(player).toggleBypassMode() ? "enabled" : "disabled";
        sendMessage(player, ChatColor.GREEN, "Bypass mode %s", status);
        return true;
    }
}
