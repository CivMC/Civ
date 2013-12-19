package com.untamedears.JukeAlert.command.commands;

import org.bukkit.command.CommandSender;

import com.untamedears.JukeAlert.command.PlayerCommand;
import com.untamedears.JukeAlert.util.RateLimiter;

public class ConfigCommand extends PlayerCommand {

    public ConfigCommand() {
        super("Config");
        setDescription("Run-time configuration");
        setUsage("/jaconfig");
        setArgumentRange(2, 2);
        setIdentifier("jaconfig");
    }

    @Override
    public boolean execute(final CommandSender sender, String[] args) {
        if (args.length != 2) {
            return false;
        }
        String setting = args[0];
        String value = args[1];
        if (setting.equalsIgnoreCase("ratelimit")) {
            int rate;
            try {
                rate = Integer.parseInt(value);
            } catch(Exception ex) {
                sender.sendMessage("Specify an integer value.");
                return true;
            }
            RateLimiter.setMaxRate(rate);
            sender.sendMessage(String.format("Rate set to %d", RateLimiter.getMaxRate()));
        } else {
            sender.sendMessage("Unknown config option " + setting);
        }
        return true;
    }
}
