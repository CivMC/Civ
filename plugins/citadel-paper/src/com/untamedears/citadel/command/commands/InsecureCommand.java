package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.getSecurityLevel;
import static com.untamedears.citadel.Utility.setMultiMode;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.PlayerState;

public class InsecureCommand extends PlayerCommand {

    public InsecureCommand() {
        super("Insecure Mode");
        setDescription("Toggle insecure mode");
        setUsage("/ctinsecure");
        setIdentifiers(new String[] {"ctinsecure", "ctis"});
    }

    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerState state = PlayerState.get(player);

        SecurityLevel securityLevel = getSecurityLevel(args, player);
        if (securityLevel == null) return false;

        setMultiMode(PlacementMode.INSECURE, SecurityLevel.PUBLIC, args, player, state);
        return true;
    }

}
