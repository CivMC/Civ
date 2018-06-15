package vg.civcraft.mc.citadel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class PlayerState {

    private static final Map<UUID, PlayerState> PLAYER_STATES = new HashMap<UUID, PlayerState>();

    /**
     * Method used to get the PlayerState of the specified player.
     * Creates a new instance of a PlayerState if none is found.
     * @param The Player you want to get the PlayerState for.
     * @return The PlayerState of that player.
     */
    public static PlayerState get(Player player) {
    	if (player == null) {
    		Citadel.getInstance().getLogger().log(Level.WARNING, "PlayerState get called with null player");
    	}
        UUID id = NameAPI.getUUID(player.getName());
        PlayerState state = PLAYER_STATES.get(id);
        if (state == null) {
            state = new PlayerState(player);
            PLAYER_STATES.put(id, state);
        }
        return state;
    }
    
    public static PlayerState get(UUID id) {
    	if (id == null) {
    		Citadel.getInstance().getLogger().log(Level.WARNING, "PlayerState get called with null id");
    	}
    	PlayerState state = PLAYER_STATES.get(id);
        if (state == null) {
            state = new PlayerState(id);
            PLAYER_STATES.put(id, state);
        }
        return state;
    }
    
    /**
     * Removes a PlayerState from the list.
     * @param The Player to remove from the list.
     */
    public static void remove(Player player) {
        PLAYER_STATES.remove(player.getUniqueId());
    }

    private UUID accountId;
    private ReinforcementMode mode;
    private Group faction;
    private boolean bypassMode;
    private Integer cancelModePid;
    private ItemStack fortificationStack;
    private boolean easyMode;

    public PlayerState(Player player) {
        this(player.getUniqueId());
    }
    
    public PlayerState(UUID uuid) {
    	reset();
    	this.accountId = uuid;
    	bypassMode = false;
    	this.easyMode = true;
    }

    /**
     * Sets the placement mode to NORMAL and resets other properties.
     */
    public void reset() {
        mode = ReinforcementMode.NORMAL;
        fortificationStack = null;
        faction = null;
    }
    /**
     * @return Returns the ReinforceMent Mode associated with the given
     * Player.
     */
    public ReinforcementMode getMode() {
        return mode;
    }
    /**
     * Sets the ReinforcementMode of a given Player.
     * @param ReinforcementMode.
     */
    public void setMode(ReinforcementMode mode) {
        this.mode = mode;
    }
    /**
     * Sets the fortificationStack.  Make sure this is a ReinforcementType
     * or its pretty useless.
     * @param ItemStack.
     */
    public void setFortificationItemStack(ItemStack fortificationStack) {
    	if (fortificationStack == null) {
    		Citadel.getInstance().getLogger().log(Level.WARNING, "PlayerState setFortificationItemStack called with null stack");
    	}
        this.fortificationStack = fortificationStack;
    }
    /**
     * Gets the Group for a given PlayerState.
     * @return The group belonging to the PlayerState.
     */
    public Group getGroup() {
        return faction;
    }
    /**
     * Sets the Group for a PlayerState.
     * @param The group.
     */
    public void setGroup(Group faction) {
    	if (faction == null) {
    		Citadel.getInstance().getLogger().log(Level.WARNING, "PlayerState setGroup called with null faction");
    	}
        this.faction = faction;
    }
    /**
     * Returns if a player is in BypassMode.
     * @return True if in Bypass Mode.
     */
    public boolean isBypassMode() {
        return bypassMode;
    }
    /**
     * Toggles Bypass Mode.
     * @return Returns the new value.
     */
    public boolean toggleBypassMode() {
        bypassMode = !bypassMode;
        return bypassMode;
    }
    
    public boolean getEasyMode() {
    	return easyMode;
    }
    
    /**
     * Toggles Easy Mode.
     * @return Returns the new value.
     */
    public boolean toggleEasyMode() {
    	easyMode = !easyMode;
    	return easyMode;
    }
    
    /**
     * Prepares a scheduled task to reset the mode after a configured amount
     * of time.
     * 
     * If a task is already scheduled it is canceled first.
     */
    public void checkResetMode() {
    	Citadel plugin = Citadel.getInstance();
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        if (cancelModePid != null && scheduler.isQueued(cancelModePid))
            scheduler.cancelTask(cancelModePid);
        
        cancelModePid = scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
            	try {
	                Player player = Bukkit.getPlayer(accountId);
	                if (player != null && mode.name() != "NORMAL") {
	                    player.sendMessage(ChatColor.YELLOW + mode.name() + " mode off");
	                }
	                reset();
            	} catch (Exception e) {
               		Citadel.getInstance().getLogger().log(Level.WARNING, "PlayerState checkResetMode Failed", e);
            	}
            }
        }, 20L * CitadelConfigManager.getPlayerStateReset());
    }
    /**
     * Returns the ReinforcementType for a PlayerState.
     * @return ReinforcementType.
     */
    public ReinforcementType getReinforcementType(){
    	return ReinforcementType.getReinforcementType(fortificationStack);
    }
}
