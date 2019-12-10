/**
 * Created by Aleksey on 11.07.2017.
 */

package isaac.bastion.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import isaac.bastion.Bastion;
import isaac.bastion.BastionGroup;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

public class BastionGroupStorage {
	private static class Operation {
		public int groupId;
		public int allowedGroupId;
		public boolean isAdded;
	}

	private static class BastionGroupAndAllowed {
		public BastionGroup bastionGroup;
		public Integer allowedGroupId;
	}

	private static final int saveDelay = 60 * 20; //once per 1 minute

	private ManagedDatasource db;
	private Logger log;

	private List<Operation> changed;
	private Queue<Operation> localChanged = new ArrayDeque<Operation>();

	private Map<Integer, BastionGroup> groups;
	private int taskId;

	private static final String selectAllGroups = "select * from bastion_groups order by bastion_group_id;";
	private static final String selectGroup = "select * from bastion_groups where bastion_group_id = ? and allowed_group_id = ?;";
	private static final String addGroup = "insert into bastion_groups (bastion_group_id, allowed_group_id) values (?, ?);";
	private static final String deleteGroup = "delete from bastion_groups where bastion_group_id = ? and allowed_group_id = ?;";

	public BastionGroupStorage(ManagedDatasource db, Logger log) {
		this.changed = new ArrayList<>();
		this.groups = new HashMap<>();
		this.db = db;
		this.log = log;

		this.taskId = new BukkitRunnable(){
			public void run(){
				updateChanged();
			}
		}.runTaskTimerAsynchronously(Bastion.getPlugin(), saveDelay, saveDelay).getTaskId();
	}

	/**
	 * Updates all remaining bastions and cancels the update task
	 */
	public void close() {
		Bukkit.getScheduler().cancelTask(this.taskId);
		updateChanged();
	}

	/**
	 * Data manipulations methods
	 */

	/**
	 * Adds bastion group
	 * @param group Bastion's group
	 * @param allowedGroup Allowed group
	 * @return True is group was added, False - if it already exist
	 */
	public boolean addAllowedGroup(Group group, Group allowedGroup) {
		List<BastionGroup> bastionGroups = getBastionGroups(group);

		if(findBastionGroupAndAllowed(bastionGroups, allowedGroup) != null) return false;

		BastionGroup bastionGroup;

		if(bastionGroups.size() > 0) {
			bastionGroup = bastionGroups.get(0);
		} else {
			this.groups.put(group.getGroupId(), bastionGroup = new BastionGroup(group.getGroupId()));
		}

		bastionGroup.addAllowedGroup(allowedGroup.getGroupId());
		addChanged(bastionGroup.getGroupId(), allowedGroup.getGroupId(), true);

		return true;
	}

	/**
	 * Deletes a bastion group
	 * @param group The bastion's group
	 * @param allowedGroup The allowed group name to delete
	 * @return True is group was deleted, False - if group doesn't exist
	 */
	public boolean deleteAllowedGroup(Group group, Group allowedGroup) {
		boolean isDeleted = false;
		BastionGroupAndAllowed bastionGroupAndAllowed;

		while((bastionGroupAndAllowed = getBastionGroupByAllowed(group, allowedGroup)) != null) {
			if (!bastionGroupAndAllowed.bastionGroup.removeAllowedGroup(bastionGroupAndAllowed.allowedGroupId)) {
				this.groups.remove(bastionGroupAndAllowed.bastionGroup.getGroupId());
			}

			addChanged(bastionGroupAndAllowed.bastionGroup.getGroupId(), bastionGroupAndAllowed.allowedGroupId, false);

			isDeleted = true;
		}

		return isDeleted;
	}

	public boolean isAllowedGroup(Group group, Group allowedGroup) {
		return getBastionGroupByAllowed(group, allowedGroup) != null;
	}

	public List<BastionGroup> getBastionGroups(Group group) {
		List<BastionGroup> bastionGroups = new ArrayList<BastionGroup>();

		for(int groupId : group.getGroupIds()) {
			BastionGroup bastionGroup = this.groups.get(groupId);

			if(bastionGroup != null) {
				bastionGroups.add(bastionGroup);
			}
		}

		return bastionGroups;
	}


	/**
	 * Loads all bastion groups from the database
	 */
	public void loadGroups() {
		try (
			Connection conn = this.db.getConnection();
			PreparedStatement ps = conn.prepareStatement(selectAllGroups)
		) {
			int validCount = 0;
			int invalidCount = 0;
			ResultSet result = ps.executeQuery();

			while(result.next()) {
				int groupId = result.getInt("bastion_group_id");
				int allowedGroupId = result.getInt("allowed_group_id");
				Group group = GroupManager.getGroup(groupId);
				Group allowedGroup = GroupManager.getGroup(allowedGroupId);

				if(group == null || allowedGroup == null) {
					addChanged(groupId, allowedGroupId, false);
					invalidCount++;
				} else {
					BastionGroup bastionGroup = this.groups.get(groupId);

					if(bastionGroup == null) {
						this.groups.put(groupId, bastionGroup = new BastionGroup(groupId));
					}

					bastionGroup.addAllowedGroup(allowedGroupId);

					validCount++;
				}
			}

			log.info("Loaded " + this.groups.size() + " bastion groups with " + validCount + " allowed groups.");

			if(invalidCount > 0) {
				log.info(" Marked to delete " + invalidCount + " records for non-existent groups.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.log(Level.SEVERE, ChatColor.RED + "===== Error loading bastion groups from database, shutting down =====", e);
			Bukkit.getServer().getPluginManager().disablePlugin(Bastion.getPlugin());
		}
	}

	/**
	 * Scheduled task methods
	 */

	private void updateChanged() {
		try {
			synchronized (this.changed) {
				if (this.changed.size() != 0) {
					this.localChanged.addAll(this.changed);
					this.changed.clear();
				}
			}

			this.log.info("'Update bastion groups' task begin, found " + this.localChanged.size() + " operations to proceed");

			int count = 0;

			try {
				Operation operation;

				while ((operation = this.localChanged.poll()) != null) {
					update(operation);
					count++;
				}
			} finally {
				this.log.info("'Update bastion groups' task ended, " + count + " operations proceeded");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void update(Operation operation) {
		try (Connection conn = db.getConnection()) {
			if(operation.isAdded) {
				try (PreparedStatement ps = conn.prepareStatement(selectGroup)) {
					ps.setInt(1, operation.groupId);
					ps.setInt(2, operation.allowedGroupId);

					try (ResultSet result = ps.executeQuery()) {
						boolean hasData = result.next();

						if (hasData) return;
					} catch (SQLException e2) {
						e2.printStackTrace();
						return;
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					return;
				}
			}

			String sql = operation.isAdded ? addGroup : deleteGroup;

			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, operation.groupId);
				ps.setInt(2, operation.allowedGroupId);
				ps.executeUpdate();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 * Helper methods
	 */

	private void addChanged(int groupId, int allowedGroupId, boolean isAdded) {
		Operation operation = new Operation();
		operation.groupId = groupId;
		operation.allowedGroupId = allowedGroupId;
		operation.isAdded = isAdded;

		synchronized (this.changed) {
			this.changed.add(operation);
		}
	}

	private BastionGroupAndAllowed getBastionGroupByAllowed(Group group, Group allowedGroup) {
		List<BastionGroup> bastionGroups = getBastionGroups(group);
		return bastionGroups.size() > 0 ? findBastionGroupAndAllowed(bastionGroups, allowedGroup) : null;
	}

	private BastionGroupAndAllowed findBastionGroupAndAllowed(List<BastionGroup> bastionGroups, Group allowedGroup) {
		List<Integer> allowedGroupIds = allowedGroup.getGroupIds();

		for(BastionGroup bastionGroup : bastionGroups) {
			for(int allowedGroupId : allowedGroupIds) {
				if(bastionGroup.isAllowedGroup(allowedGroupId)) {
					BastionGroupAndAllowed result = new BastionGroupAndAllowed();
					result.bastionGroup = bastionGroup;
					result.allowedGroupId = allowedGroupId;

					return result;
				}
			}
		}

		return null;
	}
}
