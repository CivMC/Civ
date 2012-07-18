package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.PlayerState;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 11:08 AM
 * 
 * Last modified by JonnyD
 * 7/18/12
 */
public class BypassCommand extends PlayerCommand {
    public BypassCommand() {
        super("Bypass Mode");
        setDescription("Toggles bypass mode");
        setUsage("/ctbypass");
		setIdentifiers(new String[] {"ctbypass", "ctb"});
    }

	public boolean execute(CommandSender sender, String[] args) {
		String status = PlayerState.get((Player)sender).toggleBypassMode() ? "enabled" : "disabled";
        sendMessage(sender, ChatColor.GREEN, "Bypass mode %s", status);
        return true;
	}

}
