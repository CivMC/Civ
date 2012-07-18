package com.untamedears.citadel.entity;

import static com.untamedears.citadel.Utility.sendMessage;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.SecurityLevel;

public class PlayerState {

    private static final HashMap<Player, PlayerState> PLAYER_STATES = new HashMap<Player, PlayerState>();

    public static PlayerState get(Player player) {
        PlayerState state = PLAYER_STATES.get(player);
        if (state == null) {
            state = new PlayerState(player);
            PLAYER_STATES.put(player, state);
        }
        return state;
    }

    public static void remove(Player player) {
        PLAYER_STATES.remove(player);
    }

    private Player player;
    private PlacementMode mode;
    private ReinforcementMaterial fortificationMaterial;
    private SecurityLevel securityLevel;
    private Faction faction;
    private boolean bypassMode;
    private long lastThrottledMessage;
    private Integer cancelModePid;

    public PlayerState(Player player) {
        reset();
        this.player = player;
        bypassMode = false;
    }
    
    /**
     * Sets the placement mode to NORMAL and resets other properties.
     */
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

    public Faction getFaction() {
        return faction;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
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
    
    /**
     * Prepares a scheduled task to reset the mode after a configured amount
     * of time.
     * 
     * If a task is already scheduled it is canceled first.
     */
    public void checkResetMode() {
    	Citadel plugin = Citadel.getPlugin();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        if (cancelModePid != null && scheduler.isQueued(cancelModePid))
            scheduler.cancelTask(cancelModePid);
        
        cancelModePid = scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                sendMessage(player, ChatColor.YELLOW, "%s mode off", mode.name());
                reset();
            }
        }, 20L * Citadel.getConfigManager().getAutoModeReset());
    }
}
