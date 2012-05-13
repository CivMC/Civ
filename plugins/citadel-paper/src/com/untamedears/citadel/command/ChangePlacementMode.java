package com.untamedears.citadel.command;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.entity.ReinforcementMaterial;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;

import java.util.Arrays;
import java.util.List;

import static com.untamedears.citadel.Utility.sendMessage;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 10:54 AM
 */
public class ChangePlacementMode extends PlayerCommand {

    private static List<PlacementMode> MULTI_MODE = Arrays.asList(PlacementMode.FORTIFICATION, PlacementMode.INFO, PlacementMode.REINFORCEMENT);

    public ChangePlacementMode(Citadel plugin) {
        super(plugin, 0);
    }

    public boolean onCommand(Command command, String[] args) {
        String cmd = command.getName().toLowerCase();

        SecurityLevel securityLevel = getSecurityLevel(args);
        if (securityLevel == null) return false;

        if (cmd.equals("ctfortify")) {
            ReinforcementMaterial material = ReinforcementMaterial.get(player.getItemInHand().getType());
            if (material == null) {
                sendMessage(player, ChatColor.YELLOW, "Invalid reinforcement material %s", player.getItemInHand().getType().name());
            } else {
                state.setFortificationMaterial(material);
                setMultiMode(PlacementMode.FORTIFICATION, securityLevel, args);
            }
        } else if (cmd.equals("ctreinforce")) {
            setMultiMode(PlacementMode.REINFORCEMENT, securityLevel, args);
        } else if (cmd.equals("ctinfo")) {
            setMultiMode(PlacementMode.INFO, SecurityLevel.PUBLIC, args);
        } else if (cmd.equals("ctoff")) {
            state.reset();
            if (state.isBypassMode()) state.toggleBypassMode();
            sendMessage(player, ChatColor.GREEN, "All Citadel modes set to normal");
            return true;
        } else if (cmd.equals("ctpublic")) {
            setSingleMode(SecurityLevel.PUBLIC);
            return true;
        } else if (cmd.equals("ctprivate")) {
            setSingleMode(SecurityLevel.PRIVATE);
            return true;
        } else if (cmd.equals("ctgroup")) {
            setSingleMode(SecurityLevel.GROUP);
            return true;
        }
        
        return true;
    }

    private void setSingleMode(SecurityLevel securityLevel) {
        if (state.getMode() != PlacementMode.REINFORCEMENT_SINGLE_BLOCK && state.getSecurityLevel() != securityLevel) {
            state.setSecurityLevel(securityLevel);
            state.setMode(PlacementMode.REINFORCEMENT_SINGLE_BLOCK);
            sendMessage(player, ChatColor.GREEN, "Single block reinforcement mode %s", securityLevel.name() + ".");
        }
    }
    
    private void setMultiMode(PlacementMode mode, SecurityLevel securityLevel, String[] args) {
        if (!MULTI_MODE.contains(mode)) return;

        if (state.getMode() == mode && state.getSecurityLevel() == securityLevel) {
            state.reset();
            sendMessage(player, ChatColor.GREEN, "%s mode off", mode.name());
        } else {
            state.setMode(mode);
            state.setSecurityLevel(securityLevel);
            switch (mode) {
                case REINFORCEMENT:
                    sendMessage(player, ChatColor.GREEN, "%s mode %s", mode.name(), securityLevel.name());
                    break;
                case FORTIFICATION:
                    sendMessage(player, ChatColor.GREEN, "%s mode %s, %s", mode.name(), state.getReinforcementMaterial().getMaterial().name(), securityLevel.name());
                    break;
                case INFO:
                    sendMessage(player, ChatColor.GREEN, "%s mode on", mode.name());
                    break;
            }
            state.checkResetMode();
        }
    }

    private SecurityLevel getSecurityLevel(String[] args) {
        if (args.length > 0) {
            try {
                return SecurityLevel.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                sendMessage(player, ChatColor.RED, "Invalid access level %s", args[0]);
                return null;
            }
        }
        return SecurityLevel.PRIVATE;
    }
}
