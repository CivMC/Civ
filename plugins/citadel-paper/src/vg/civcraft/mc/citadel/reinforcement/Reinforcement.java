package vg.civcraft.mc.citadel.reinforcement;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class Reinforcement {

	private final long creationTime;
	private final ReinforcementType type;
	private final Location loc;
	private double health;
	protected boolean isDirty;
	protected boolean isNew;
	private final int groupId;
	private boolean insecure;

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

	public Reinforcement(Location loc, ReinforcementType type, Group group) {
		this(loc, type, group.getGroupId(), System.currentTimeMillis(), type.getHealth(), true, true, false);
	}

	/**
	 * Sets the health of a reinforcement.
	 * 
	 * @param health new health value
	 */
	public void setHealth(double health) {
		this.health = health;
		isDirty = true;
		if (health <= 0) {
			Citadel.getInstance().getReinforcementManager().removeReinforcement(this);
		}
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
	 * @return Unix time in ms when the reinforcement was created
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * @return Whether the reinforcement is insecure, meaning it ignores Citadel
	 *         restrictions on hoppers etc.
	 */
	public boolean isInsecure() {
		return insecure;
	}

	/**
	 * @return Whether this reinforcement needs to be saved to the database
	 */
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * Sets if this reinforcement needs to be saved to the database or not.
	 * 
	 * @param dirty
	 */
	public void setDirty(boolean dirty) {
		isDirty = dirty;
		if (!dirty) {
			isNew = false;
		}
	}

	/**
	 * @return True if the reinforcement has not been written to the database since its creation
	 */
	public boolean isNew() {
		return isNew;
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
	 * @return Type of this reinforcement
	 */
	public ReinforcementType getType() {
		return type;
	}

	/**
	 * @return Age of this reinforcement in milli seconds
	 */
	public long getAge() {
		return System.currentTimeMillis() - creationTime;
	}

	public boolean hasPermission(Player p, String permission) {
		Group g = getGroup();
		if (g == null) {
			return false;
		}
		return NameAPI.getGroupManager().hasAccess(g, p.getUniqueId(), PermissionType.getPermission(permission));
	}
}
