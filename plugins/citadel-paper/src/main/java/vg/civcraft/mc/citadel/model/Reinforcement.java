package vg.civcraft.mc.citadel.model;

import java.util.Random;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class Reinforcement extends TableBasedDataObject {

	private static Random rng = new Random();

	private long creationTime;
	private ReinforcementType type;
	private float health;
	private int groupId;
	private boolean insecure;

	public Reinforcement(Location loc, ReinforcementType type, Group group) {
		this(loc, type, group.getGroupId(), System.currentTimeMillis(), type.getHealth(), false, true);
	}

	public Reinforcement(Location loc, ReinforcementType type, int groupID, long creationTime, float health,
			boolean insecure, boolean isNew) {
		super(loc, isNew);
		if (type == null) {
			throw new IllegalArgumentException("Reinforcement type for reinforcement can not be null");
		}
		this.type = type;
		this.creationTime = creationTime;
		this.health = health;
		this.groupId = groupID;
		this.insecure = insecure;
	}

	/**
	 * @return Age of this reinforcement in milli seconds
	 */
	public long getAge() {
		return System.currentTimeMillis() - creationTime;
	}

	/**
	 * @return Unix time in ms when the reinforcement was created
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * @return Group this reinforcement is under
	 */
	public Group getGroup() {
		return GroupManager.getGroup(groupId);
	}

	/**
	 * @return Id of the group this reinforcement is under
	 */
	public int getGroupId() {
		return groupId;
	}

	/**
	 * @return Current health
	 */
	public float getHealth() {
		return health;
	}

	/**
	 * Gets the center of the block at the location of this reinforcement.
	 * getLocation() will return the integer coordinates of the reinforcement while
	 * this location is offset by 0.5 to the center
	 * 
	 * @return Center of the block
	 */
	public Location getBlockCenter() {
		Location copy = location.clone();
		copy.add(0.5, 0.5, 0.5);
		return copy;
	}

	/**
	 * @return Type of this reinforcement
	 */
	public ReinforcementType getType() {
		return type;
	}
	
	/**
	 * Checks whether the player with the given UUID has the given permission on the group this reinforcement belongs to
	 * @param uuid UUID of the player to check permissions for, not null
	 * @param permission Permission to check, not null
	 * @return True if the player has the given permission on this reinforcements group, false otherwise
	 */
	public boolean hasPermission(UUID uuid, PermissionType permission) {
		if (permission == null) {
			throw new IllegalArgumentException("PermissionType may not be null");
		}
		Group g = getGroup();
		if (g == null) {
			return false;
		}
		return NameAPI.getGroupManager().hasAccess(g, uuid, permission);
	}
	
	/**
	 * Checks whether the given player has the given permission on the group this reinforcement belongs to
	 * @param player Player to check permissions for, not null
	 * @param permission Permission to check, not null
	 * @return True if the player has the given permission on this reinforcements group, false otherwise
	 */
	public boolean hasPermission(Player player, PermissionType permission) {
		return hasPermission(player.getUniqueId(), permission);
	}

	/**
	 * After being broken reinforcements will no longer be accessible via lookup,
	 * but may still persist in the cache until their deletion is persisted into the
	 * database
	 * 
	 * @return True if the reinforcements health is equal to or less than 0
	 */
	public boolean isBroken() {
		return health <= 0;
	}

	/**
	 * @return Whether the reinforcement is insecure, meaning it ignores Citadel
	 *         restrictions on hoppers etc.
	 */
	public boolean isInsecure() {
		return insecure;
	}

	/**
	 * @return Whether reinforcement is mature, meaning the maturation time
	 *         configured for this reinforcements type has passed since the
	 *         reinforcements creation
	 */
	public boolean isMature() {
		return System.currentTimeMillis() - creationTime > type.getMaturationTime();
	}

	public void setGroup(Group group) {
		if (group == null) {
			throw new IllegalArgumentException("Group can not be set to null for a reinforcement");
		}
		this.groupId = group.getGroupId();
		setDirty();
	}

	/**
	 * Sets the health of a reinforcement.
	 * 
	 * @param health new health value
	 */
	public void setHealth(float health) {
		this.health = health;
		if (health <= 0) {
			if (getOwningCache() != null) {
				getOwningCache().remove(this);
			}
		}
		else {
			setDirty();
		}
	}

	public void setType(ReinforcementType type) {
		this.type = type;
		setDirty();
	}

	/**
	 * Switches the insecure flag of the reinforcement
	 */
	public void toggleInsecure() {
		insecure = !insecure;
		setDirty();
	}
	
	/**
	 * Resets the timestamp at which the reinforcement was created to the current one
	 */
	public void resetCreationTime() {
		this.creationTime = System.currentTimeMillis();
	}

	/**
	 * Does a randomness check based on current reinforcement health and
	 * reinforcement type to decide whether the reinforcement item should be
	 * returned
	 * 
	 * @return Whether to return the reinforcement item or not
	 */
	public boolean rollForItemReturn() {
		double baseChance = type.getReturnChance();
		double relativeHealth = health / type.getHealth();
		baseChance *= relativeHealth;
		return rng.nextDouble() <= baseChance;
	}
}
