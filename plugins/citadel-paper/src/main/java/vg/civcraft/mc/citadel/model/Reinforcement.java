package vg.civcraft.mc.citadel.model;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class Reinforcement {

	private static Random rng = new Random();

	private final long creationTime;
	private ReinforcementType type;
	private final Location loc;
	private double health;
	protected boolean isDirty;
	protected boolean isNew;
	private int groupId;
	private boolean insecure;
	private ChunkCache owningCache;

	public Reinforcement(Location loc, ReinforcementType type, Group group) {
		this(loc, type, group.getGroupId(), System.currentTimeMillis(), type.getHealth(), true, true, false);
	}

	public Reinforcement(Location loc, ReinforcementType type, int groupID, long creationTime, double health,
			boolean isDirty, boolean isNew, boolean insecure) {
		if (loc == null) {
			throw new IllegalArgumentException("Location for reinforcement can not be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("Reinforcement type for reinforcement can not be null");
		}
		this.loc = loc;
		this.type = type;
		this.creationTime = creationTime;
		this.health = health;
		this.isDirty = isDirty;
		this.groupId = groupID;
		this.isNew = isNew;
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
	public double getHealth() {
		return health;
	}

	/**
	 * @return Location of the Reinforcement.
	 */
	public Location getLocation() {
		return loc;
	}

	/**
	 * @return Type of this reinforcement
	 */
	public ReinforcementType getType() {
		return type;
	}

	public boolean hasPermission(Player p, String permission) {
		return hasPermission(p.getUniqueId(), permission);
	}

	public boolean hasPermission(UUID uuid, String permission) {
		Group g = getGroup();
		if (g == null) {
			return false;
		}
		return NameAPI.getGroupManager().hasAccess(g, uuid, PermissionType.getPermission(permission));
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
	 * @return Whether this reinforcement needs to be saved to the database
	 */
	public boolean isDirty() {
		return isDirty;
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

	/**
	 * @return True if the reinforcement has not been written to the database since
	 *         its creation
	 */
	public boolean isNew() {
		return isNew;
	}

	/**
	 * Sets if this reinforcement needs to be saved to the database or not. Will
	 * automatically update the dirty flag of the cache holding this reinforcement
	 * as well
	 * 
	 * @param dirty
	 */
	public void setDirty(boolean dirty) {
		this.isDirty = dirty;
		if (!dirty) {
			// we saved to the database, so we are no longer new now
			isNew = false;
		} else {
			owningCache.setDirty(true);
		}
	}

	public void setGroup(Group group) {
		if (group == null) {
			throw new IllegalArgumentException("Group can not be set to null for a reinforcement");
		}
		this.groupId = group.getGroupId();
		setDirty(true);
	}

	/**
	 * Sets the health of a reinforcement.
	 * 
	 * @param health new health value
	 */
	public void setHealth(double health) {
		this.health = health;
		setDirty(true);
		if (health <= 0) {
			Citadel.getInstance().getReinforcementManager().removeReinforcement(this);
		}
	}

	/**
	 * Sets which cache this reinforcement belongs to. Cache is expected to set this
	 * when beginning to track the reinforcement
	 */
	void setOwningCache(ChunkCache cache) {
		this.owningCache = cache;
	}

	public void setType(ReinforcementType type) {
		this.type = type;
		setDirty(true);
	}

	/**
	 * Switches the insecure flag of the reinforcement
	 */
	public void toggleInsecure() {
		insecure = !insecure;
		setDirty(true);
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
