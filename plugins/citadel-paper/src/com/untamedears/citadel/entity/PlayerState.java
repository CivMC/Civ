package com.untamedears.citadel.entity;

import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.SecurityLevel;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PlayerState {

    private static final HashMap<Player, PlayerState> PLAYER_STATES = new HashMap<Player, PlayerState>();

    public static PlayerState get(Player player) {
        PlayerState state = PLAYER_STATES.get(player);
        if (state == null) {
            state = new PlayerState();
            PLAYER_STATES.put(player, state);
        }
        return state;
    }

    public static void remove(Player player) {
        PLAYER_STATES.remove(player);
    }

    private PlacementMode mode;
    private ReinforcementMaterial fortificationMaterial;
    private SecurityLevel securityLevel;
    private boolean bypassMode;
    private long lastThrottledMessage;

    public PlayerState() {
        reset();
        bypassMode = false;
    }

    public void reset() {
        mode = PlacementMode.NORMAL;
        fortificationMaterial = null;
        securityLevel = SecurityLevel.PUBLIC;
    }

    public PlacementMode getMode() {
        return mode;
    }

    public void setMode(PlacementMode mode) {
        this.mode = mode;
    }

    public ReinforcementMaterial getReinforcementMaterial() {
        return fortificationMaterial;
    }

    public void setFortificationMaterial(ReinforcementMaterial fortificationMaterial) {
        this.fortificationMaterial = fortificationMaterial;
    }

    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(SecurityLevel securityLevel) {
        this.securityLevel = securityLevel;
    }

    public boolean isBypassMode() {
        return bypassMode;
    }

    public boolean toggleBypassMode() {
        bypassMode = !bypassMode;
        return bypassMode;
    }

    public long getLastThrottledMessage() {
        return lastThrottledMessage;
    }

    public void setLastThrottledMessage(long lastThrottledMessage) {
        this.lastThrottledMessage = lastThrottledMessage;
    }
}
