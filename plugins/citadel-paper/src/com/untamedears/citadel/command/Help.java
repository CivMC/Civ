package com.untamedears.citadel.command;

import com.untamedears.citadel.Citadel;
import org.bukkit.Material;
import org.bukkit.command.Command;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 4:09 PM
 */
public class Help extends PlayerCommand {
    public Help(Citadel plugin) {
        super(plugin, 0);
    }

    @Override
    public boolean onCommand(Command command, String[] args) {
        String commandName = null;
        if (args.length > 0) {
            commandName = args[0];
        }

        //TODO: display in depth help

        return true;
    }
}
