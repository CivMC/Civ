package vg.civcraft.mc.citadel.reinforcement;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Openable;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelConfigManager;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class PlayerReinforcement extends Reinforcement{
	private transient int gid;
	private Group g;
	private static GroupManager gm;
	private boolean isInsecure = false;
	private ItemStack stack;
	private String materialName;

	public PlayerReinforcement(Location loc, int health,
			int creation, int acid, Group g, ItemStack stack) {
		super(loc, stack.getType(), health, creation, acid);
		this.g = g;
		this.stack = stack;
		if (gm == null) {
			gm = NameAPI.getGroupManager();
		}
		this.gid = g.getGroupId();

		ItemMeta meta = this.stack.hasItemMeta() ? this.stack.getItemMeta(): null;
		String lore = meta != null && meta.hasLore() && meta.getLore().size() > 0 ? meta.getLore().get(0): null;

		this.materialName = lore != null && lore.length() > 0 ? "\"" + lore + "\"": stack.getType().name();
	}

	public boolean canBypass(Player p) {
		checkValid();
		if (g == null) {
			return false;
		}
		return gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("BYPASS_REINFORCEMENT"));
	}

	public boolean canAccessCrops(Player p) {
		checkValid();
		if (g == null) {
			return false;
		}
		return gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("CROPS"));
	}

	public boolean canAccessChests(Player p) {
		checkValid();
		if (g == null) {
			return false;
		}
		return gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("CHESTS"));
	}

	public boolean canAccessDoors(Player p) {
		checkValid();
		if (g == null) {
			return false;
		}
		return gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("DOORS"));
	}

	public boolean canViewInformation(Player p) {
		checkValid();
		if (g == null) {
			return false;
		}
		return gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("REINFORCEMENT_INFO"));
	}

	public boolean canMakeInsecure(Player p) {
		checkValid();
		if (g == null) {
			return false;
		}
		return gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("INSECURE_REINFORCEMENT"));
	}

	public boolean canAcid(Player p) {
		checkValid();
		if (g == null) {
			return false;
		}
		return gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("ACIDBLOCK"));
	}

	public boolean isSecurable() {
		return (isContainer() || isDoor());
	}

	public int getDamageMultiplier(){
		checkValid();
		if (g == null){
			return 1;
		}
		Timestamp ts = NameAPI.getGroupManager().getTimestamp(g.getName());

		long shiftMultiplier = ((System.currentTimeMillis() - ts.getTime()) / (long)86400000) / (long)Citadel.getReinforcementManager().getDayMultiplier();
		if (shiftMultiplier > 0) {
			return 1 << shiftMultiplier;
		}
		return 1;
	}

	/**
	 * @return return false if it is secure, return true if it is insecure.
	 */
	public boolean isInsecure(){
		return isInsecure;
	}
	/**
	 * Toggles whether or not the block is insecure.
	 */
	public void toggleInsecure() {
        this.setInsecure(!this.isInsecure());
    }
	/**
	 * Set the status of either insecure or secure.
	 * Mostly used with hoppers to allow or disallow the transfer of items
	 * from one hopper to another if they are on separate groups.
	 * @param bool
	 */
	public void setInsecure(boolean bool){
		isInsecure = bool;
		isDirty = true;
	}
	/**
	 * @return Returns the percent of the reinforcement.
	 */
    public double getHealth() {
        return (double)getDurability() / ((double)ReinforcementType.
        		getReinforcementType(stack).getHitPoints());
    }
    /**
     * @return Returns in textual form the health of the reinforcement.
     */
    public String getHealthText() {
        double health = getHealth();
        if (CitadelConfigManager.showHealthAsPercent()) {
        	DecimalFormat df = new DecimalFormat("#.##");
        	return df.format(health * 100) + "%";
        } else {
          if (health > 0.75) {
              return "excellently";
          } else if (health > 0.50) {
              return "well";
          } else if (health > 0.25) {
              return "decently";
          } else {
              return "poorly";
          }
        }
    }
    /**
     * Returns true if the block has an inventory that can be opened.
     * @return boolean
     */
    public boolean rable() {
        Block block = getLocation().getBlock();
        return block.getState() instanceof InventoryHolder
                || block.getState().getData() instanceof Openable || Utility.doorTypes.contains(block.getType());
    }

    /**
     * @return True if the reinforced block is a door/trapdoor etc. or part of one
     */
    public boolean isDoor() {
    	Block block = getLocation().getBlock();
    	return Utility.doorTypes.contains(block.getType()) || block.getState().getData() instanceof Openable;
    }

    public boolean isContainer() {
    	return getLocation().getBlock().getState() instanceof InventoryHolder;
    }


    /**
     * Returns the group this PlayerReinforcement is associated with.
     * @return group
     */
    public Group getGroup(){
    	checkValid();
    	return g;
    }
    /**
     * Sets the Group for this reinforcement
     * @param Group
     */
    public void setGroup(Group g){
    	this.g = g;
		this.gid = g.getGroupId();
    	isDirty = true;
    }
    /**
     * @return ItemStack associated with this reinforcement
     */
    public ItemStack getStackRepresentation(){
    	return stack;
    }
    /**
     * @return A string representation of a reinforcement's health, material, ect.
     */
    public String getStatus() {
        String verb;
        if (isSecurable()) {
            verb = "Locked";
        } else {
            verb = "Reinforced";
        }

        return String.format("%s %s with %s", verb, getHealthText(), this.materialName);
    }

    private void checkValid(){
    	if (g == null) {
    		Citadel.getInstance().getLogger().log(Level.WARNING, "CheckValid was called but the underlying group " + gid + " is gone for " + this.getLocation() + "!");
    		return;
    	}
    	if (!g.isValid()){ // incase it was recently merged/ deleted.
    		int groupId = g.getGroupId();
    		g = GroupManager.getGroup(groupId);
    		if (g == null) {
    			Citadel.getInstance().getLogger().log(Level.INFO, "Group " + groupId + " was deleted or merged but not marked invalid!");
    		}
    		isDirty = true;
    	}
    }
    /**
     * Note this is different to the group id of a Group.
     * If a reinforcement is made with Group 1 and then is merged into Group 0 then this will return the group_id
     * of Group 1.
     * @return Returns the value of the group_id from the group it was created with.
     */
    public int getGroupId(){
    	if (g == null) return gid;
    	return g.getGroupId();
    }

	public String getAgeStatus() {
		int d = this.getDamageMultiplier();
		if(d < 2){
			return "not decayed";
		}
		else if(d < 16){
			return "partially decayed";
		}
		else if(d < 256){
			return "highly decayed";
		}
		else if(d < 2048){
			return "heavily decayed";
		}
		else if(d > 2047){
			return "completely decayed";
		}
		else
			return "";
	}
}
