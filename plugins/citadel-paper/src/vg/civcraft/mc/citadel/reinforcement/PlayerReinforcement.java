package vg.civcraft.mc.citadel.reinforcement;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Openable;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.groups.PublicGroup;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class PlayerReinforcement extends Reinforcement{

	private Group g;
	private GroupPermission gp;
	private boolean isInsecure = false;
	private ItemStack stack;
	
	public PlayerReinforcement(Location loc, int health,
			int creation, int acid, Group g, ItemStack stack) {
		super(loc, stack.getType(), health, creation, acid);
		this.g = g;
		this.stack = stack;
		gp = NameAPI.getGroupManager().getPermissionforGroup(g);
	}
	
	/**
	 * Returns true if the player has access to do the specified 
	 * PermissionType on this reinforcement.
	 * @param PermissionType
	 * @param Player
	 * @return true if the player has access or false if the player
	 * doesn't have access to that specified permission or isn't on the
	 * group.
	 */
	public boolean isAccessible(Player p, PermissionType... pType){
		return isAccessible(p.getUniqueId(), pType);
	}
	
	public boolean isAccessible(UUID u, PermissionType... pType){
		checkValid();
		PlayerType type = g.getPlayerType(u);
		// if it is a public group we want it to check even if no
				// PlayerType
				
		if (type == null && !(g instanceof PublicGroup))
			return false;
		return gp.isAccessible(type, pType);
	}
	/**
	 * Is used to see if a player has permission to bypass a reinforcement.
	 * @param The Player who wants to bypass a Reinforcemet.
	 * @return Returns if the Player can or not.
	 */
	public boolean isBypassable(Player p){
		checkValid();
		PlayerType type = g.getPlayerType(p.getUniqueId());
		if (type == null)
			return false;
		return gp.isAccessible(type, PermissionType.BLOCKS);
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
    /**
     * Returns true if the block has an inventory that can be opened.
     * @return boolean
     */
    public boolean isSecurable() {
        Block block = getLocation().getBlock();
        return block.getState() instanceof InventoryHolder
                || block.getState().getData() instanceof Openable || Utility.doorTypes.contains(block.getType());
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
    	this.gp = NameAPI.getGroupManager().getPermissionforGroup(g);
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
        return String.format("%s %s with %s",
                verb,
                getHealthText(),
                getMaterial().name());
    }
    
    private void checkValid(){
    	if (!g.isValid()){ // incase it was recently merged/ deleted.
    		g = NameAPI.getGroupManager().getGroup(g.getGroupId());
    		gp = NameAPI.getGroupManager().getPermissionforGroup(g);
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
    	return g.getGroupId();
    }
}
