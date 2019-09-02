package com.untamedears.JukeAlert.model;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.model.field.FieldManager;
import com.untamedears.JukeAlert.model.field.SingleCuboidRangeManager;
import com.untamedears.JukeAlert.model.log.LoggingDelegate;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class Snitch extends TableBasedDataObject {

	private int snitchId;
	private String name;
	private int typeID;
	private int groupID;
	private LoggingDelegate loggingDelegate;
	private FieldManager fieldManager;
	private boolean shouldToggleLevers;
	private long lastRefresh;

	/**
	 * Constructor for freshly created snitches
	 * 
	 * @param loc                Location of the snitch
	 * @param group              Group the snitch belongs to
	 * @param loggingDelegate    Logging handler
	 * @param shouldToggleLevers Should snitch toggle adjacent levers
	 */
	public Snitch(Location loc, boolean isNew, int groupID, LoggingDelegate loggingDelegate, boolean shouldToggleLevers, int range, int typeID) {
		super(loc, isNew);
		this.groupID = groupID;
		this.loggingDelegate = loggingDelegate;
		this.shouldToggleLevers = shouldToggleLevers;
		this.name = "";
		this.fieldManager = new SingleCuboidRangeManager(loc, range, this);
		this.typeID = typeID;
	}
	
	/**
	 * @return Identifying type id given to this instance by the config it was created from
	 */
	public int getTypeID() {
		return typeID;
	}

	/**
	 * Checks if the player with the given UUID has the given permission for this
	 * snitch
	 * 
	 * @param uuid       UUID of the player
	 * @param permission Permission to check for
	 * @return True if the player has the permission and the snitch/group is valid,
	 *         false otherwise
	 */
	public boolean hasPermission(UUID uuid, PermissionType permission) {
		return NameAPI.getGroupManager().hasAccess(getGroup(), uuid, permission);
	}

	/**
	 * Checks if the player with the given UUID has the given permission for this
	 * snitch
	 * 
	 * @param uuid       Player to check for
	 * @param permission Permission to check for
	 * @return True if the player has the permission and the snitch/group is valid,
	 *         false otherwise
	 */
	public boolean hasPermission(Player player, PermissionType permission) {
		return hasPermission(player.getUniqueId(), permission);
	}

	/**
	 * @return FieldManager which handles the exact area covered by the snitch
	 */
	public FieldManager getFieldManager() {
		return fieldManager;
	}

	/**
	 * @return Group the snitch belongs to
	 */
	public Group getGroup() {
		return GroupManager.getGroup(groupID);
	}

	/**
	 * @return Database internal id of the snitch. -1 if not set yet
	 */
	public int getId() {
		return snitchId;
	}

	/**
	 * @return Custom name of the snitch, empty String by default
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the internal database id of the snitch, should only be used after first
	 * inserting the snitch into the database
	 * 
	 * @param newId Snitch id
	 */
	public void setId(int newId) {
		this.snitchId = newId;
	}

	/**
	 * Changes the custom player given name of the snitch
	 * 
	 * @param name New name
	 */
	public void setName(String name) {
		this.name = name;
		setDirty();
	}

	public void setLoggingDelegate(LoggingDelegate delegate) {
		this.loggingDelegate = delegate;
	}

	public void setShouldToggleLevers(boolean shouldToggleLevers) {
		if (this.shouldToggleLevers != shouldToggleLevers) {
			setDirty();
		}
		this.shouldToggleLevers = shouldToggleLevers;
	}

	public boolean shouldToggleLevers() {
		return shouldToggleLevers;
	}
	
	public LoggingDelegate getLoggingDelegate() {
		return loggingDelegate;
	}
	
	/**
	 * @return UNIX time stamp of when this snitch was last refreshed
	 */
	public long getLastRefresh() {
		return lastRefresh;
	}
	
	/**
	 * Resets the snitchs refresh timer
	 */
	public void refresh() {
		this.lastRefresh = System.currentTimeMillis();
		setDirty();
	}
}
