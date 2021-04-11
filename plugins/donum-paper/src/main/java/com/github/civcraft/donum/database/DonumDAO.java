package com.github.civcraft.donum.database;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.sql.rowset.serial.SerialBlob;

import org.bukkit.Bukkit;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.civcraft.donum.Donum;
import com.github.civcraft.donum.inventories.DeathInventory;
import com.github.civcraft.donum.misc.ItemMapBlobHandling;

public class DonumDAO {

	private Database db;

	public DonumDAO(String host, int port, String dbname, String username, String password) {
		db = new Database(host, port, dbname, username, password, Donum.getInstance().getLogger());
		if (!db.connect()) {
			Donum.getInstance().severe("Could not establish database connection, shutting down");
			Bukkit.getPluginManager().disablePlugin(Donum.getInstance());
			return;
		}
		updateTables();
	}

	/**
	 * Automatically updates all tables if structual changes were made
	 */
	public void updateTables() {
		db.execute("create table if not exists db_version (db_version int(11), plugin_name varchar(40), timestamp datetime default now());");
		int version = getVersion();
		if (version == 0) {
			Donum.getInstance().info("Creating tables");
			db.execute("create table if not exists deliveryInventories (uuid varchar(36), inventory blob not null, "
					+ "lastUpdate datetime not null default now(), primary key (uuid));");
			db.execute("create table if not exists deliveryAdditions (deliveryId int not null auto_increment, uuid varchar(36) not null, "
					+ "inventory blob not null, creationTime datetime not null default now(), index deliveryAdditionsUuidIndex (uuid), "
					+ "primary key(deliveryId));");
			db.execute("create table if not exists deliveryInventoryLocks (uuid varchar(36) not null, creationTime datetime not null default now(), "
					+ "primary key(uuid));");
			db.execute("create table if not exists logoutInventories (uuid varchar(36), inventory blob not null, hash int, "
					+ "creationTime datetime not null default now(), index logOutInventoryCreationTimeIndex (creationTime), primary key(uuid, creationTime));");
			db.execute("create table if not exists loggedInconsistencies (id int not null auto_increment, uuid varchar(36) not null, inventory blob not null, state varchar(20) not null default 'NEW', "
					+ "creationTime datetime not null default now(), lastUpdate datetime not null default now(), index loggedInconsistenciesUuidIndex (uuid),"
					+ "index loggedInconsistenciesState (state), primary key(id));");
			db.execute("create table if not exists deathInventories (id int not null auto_increment, uuid varchar(36), inventory blob not null, returned boolean not null default false, "
					+ "creationTime datetime not null default now(), primary key(id), index deathInventoriesUuidIndex (uuid));");
		}
	}

	/**
	 * @return Highest version stored for this plugin in the db versioning table
	 */
	public int getVersion() {
		ensureConnection();
		try (PreparedStatement ps = db
				.prepareStatement("select max(db_version) as db_version from db_version where plugin_name='Donum';")) {
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to get db version " + e);
		}
		return 0;
	}

	/**
	 * Inserts the given version into the version tracking table for this plugin
	 * with the current time as time stamp
	 * 
	 * @param version
	 *            Version to insert
	 */
	public void trackVersion(int version) {
		ensureConnection();
		try (PreparedStatement ps = db
				.prepareStatement("insert into db_version (db_version, plugin_name) values(?,'Donum');")) {
			ps.setInt(1, version);
			ps.execute();
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to insert version " + version + " into tracking: " + e);
		}
	}

	public synchronized boolean getLock(UUID uuid) {
		ensureConnection();
		Donum.getInstance().debug("Acquiring lock for " + uuid.toString());
		try (PreparedStatement getLock = db.prepareStatement("insert into deliveryInventoryLocks (uuid) values(?);")) {
			getLock.setString(1, uuid.toString());
			getLock.execute();
			return true;
		} catch (SQLException e) {
			Donum.getInstance().debug("Failed to get lock for " + uuid.toString());
			return false;
		}
	}

	public synchronized void freeLock(UUID uuid) {
		ensureConnection();
		Donum.getInstance().debug("Freeing lock for " + uuid.toString());
		try (PreparedStatement freeLock = db.prepareStatement("delete from deliveryInventoryLocks where uuid=?;")) {
			freeLock.setString(1, uuid.toString());
			freeLock.execute();
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to free lock for " + uuid.toString() + ": " + e);
		}
	}

	/**
	 * Gets the delivery inventory stored for the given player. If a players
	 * delivery inventory is empty, no data on it will be in the database, in
	 * this case a blank ItemMap will be returned. The same fallback behavior is
	 * applied if an exception occurs while trying to get the data
	 * 
	 * @param uuid
	 *            UUID of the player of which we want the delivery inventory
	 * @return ItemMap representing the players delivery inventory
	 */
	public ItemMap getDeliveryInventory(UUID uuid) {
		ensureConnection();
		while (!getLock(uuid)) {
			Donum.getInstance().debug("Failed to acquire lock for " + uuid.toString());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		try (PreparedStatement ps = db.prepareStatement("select inventory from deliveryInventories where uuid=?;")) {
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			ItemMap loadedMap;
			if (!rs.next()) {
				loadedMap = new ItemMap();
			} else {
				Blob blob = rs.getBlob(1);
				int blobLength = (int) blob.length();
				byte[] blobAsBytes = blob.getBytes(1, blobLength);
				blob.free();
				loadedMap = ItemMapBlobHandling.turnBlobIntoItemMap(blobAsBytes);
			}
			for (ItemMap toAdd : popStagedAdditions(uuid)) {
				loadedMap.merge(toAdd);
			}
			return loadedMap;
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to get delivery inventory for player " + uuid + " ; " + e);
			return new ItemMap();
		} finally {
			freeLock(uuid);
		}
	}

	/**
	 * Gets all staged additions and removes them from the database
	 * 
	 * @return All staged additions for the given player
	 */
	public Collection<ItemMap> popStagedAdditions(UUID player) {
		Collection<ItemMap> maps = new LinkedList<ItemMap>();
		try (PreparedStatement loadToProcess = db
				.prepareStatement("select deliveryId, inventory from deliveryAdditions where uuid=?;")) {
			loadToProcess.setString(1, player.toString());
			ResultSet toProcess = loadToProcess.executeQuery();
			while (toProcess.next()) {
				int id = toProcess.getInt(1);
				Blob addBlob = toProcess.getBlob(2);
				int blobLengthInner = (int) addBlob.length();
				byte[] blobInnerAsBytes = addBlob.getBytes(1, blobLengthInner);
				addBlob.free();
				ItemMap mapToAdd = ItemMapBlobHandling.turnBlobIntoItemMap(blobInnerAsBytes);
				Donum.getInstance().info("Adding " + mapToAdd + " to delivery inventory for " + player.toString());
				maps.add(mapToAdd);
				try (PreparedStatement cleanPendingAdditions = db
						.prepareStatement("delete from deliveryAdditions where deliveryId=?;")) {
					cleanPendingAdditions.setInt(1, id);
					cleanPendingAdditions.execute();
				}
			}
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to get delivery inventory addition for player " + player + " ; " + e);
			return maps;
		}
		return maps;
	}

	public void stageDeliveryAddition(UUID uuid, ItemMap addition) {
		ensureConnection();
		try (PreparedStatement ps = db.prepareStatement("insert into deliveryAdditions (uuid,inventory) values(?,?);")) {
			ps.setString(1, uuid.toString());
			ps.setBlob(2, new SerialBlob(ItemMapBlobHandling.turnItemMapIntoBlob(addition)));
			ps.execute();
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to stage delivery inventory addition for player " + uuid + " ; " + e);
		}
	}

	/**
	 * Updates the given players delivery inventory to the given ItemMap. Any
	 * existing records will be overwritten
	 * 
	 * @param uuid
	 *            Player for which the delivery inventory should be saved
	 * @param im
	 *            DeliveryInventory to save
	 */
	public void updateDeliveryInventory(UUID uuid, ItemMap im) {
		ensureConnection();
		while (!getLock(uuid)) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		try (PreparedStatement ps = db
				.prepareStatement("insert into deliveryInventories (uuid,inventory) values(?,?) on duplicate key update inventory=values(inventory);")) {
			Donum.getInstance().debug("Inserting inventory " + im.toString() + " for " + uuid.toString() + " into db");
			ps.setString(1, uuid.toString());
			ps.setBlob(2, new SerialBlob(ItemMapBlobHandling.turnItemMapIntoBlob(im)));
			ps.execute();
		} catch (SQLException e) {
			Donum.getInstance().warning("Error updating record of delivery inventory for player " + uuid + " ; " + e);
		} finally {
			freeLock(uuid);
		}
	}

	/**
	 * Checks whether the stored hash of the item map representing the players
	 * inventory when he logged out equals that of his inventory on logging in.
	 * If it does, null will be returned and if it doesn't, the ItemMap of the
	 * players inventory on logout will be returned for further handling. If no
	 * record of the players inventory is found null will also be returned, as
	 * no further handling is possible
	 * 
	 * @param uuid
	 *            UUID of the player to check for
	 * @param hash
	 *            Hashcode of the itemmap representing the players inventory on
	 *            login
	 * @return Null if the hashs equal or no record was found, an ItemMap if
	 *         they dont and there was an issue
	 */
	public ItemMap checkForInventoryInconsistency(UUID uuid, int hash) {
		ensureConnection();
		try (PreparedStatement ps = db
				.prepareStatement("select inventory, hash from logoutInventories where uuid=? and creationTime=(select max(creationTime) from logoutInventories where uuid=?);")) {
			ps.setString(1, uuid.toString());
			ps.setString(2, uuid.toString());
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				Donum.getInstance().debug("Found no logout inventory for " + uuid + ", login hash: " + hash);
				return null;
			}
			Blob blob = rs.getBlob(1);
			int blobLength = (int) blob.length();
			byte[] blobAsBytes = blob.getBytes(1, blobLength);
			blob.free();
			ItemMap im = ItemMapBlobHandling.turnBlobIntoItemMap(blobAsBytes);
			if (im.hashCode() != hash) {
				return im;
			}
			return null;
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to check logout inventory for player " + uuid + " ; " + e);
			return null;
		}
	}

	public void insertInconsistency(UUID uuid, ItemMap diff) {
		ensureConnection();
		try (PreparedStatement ps = db
				.prepareStatement("insert into loggedInconsistencies (uuid,inventory) values(?,?);")) {
			ps.setString(1, uuid.toString());
			ps.setBlob(2, new SerialBlob(ItemMapBlobHandling.turnItemMapIntoBlob(diff)));
			ps.execute();
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to insert diff inconsistency inventory for player " + uuid + " ; " + e);
		}
	}

	/**
	 * Saves the given ItemMap for the given UUID as inventory the player logged
	 * out with
	 * 
	 * @param uuid
	 *            UUID of the player
	 * @param items
	 *            ItemMap representing the players logout inventory
	 */
	public void insertLogoutInventory(UUID uuid, ItemMap items) {
		ensureConnection();
		try (PreparedStatement ps = db
				.prepareStatement("insert into logoutInventories (uuid,inventory,hash) values(?,?,?);")) {
			ps.setString(1, uuid.toString());
			ps.setBlob(2, new SerialBlob(ItemMapBlobHandling.turnItemMapIntoBlob(items)));
			ps.setInt(3, items.hashCode());
			ps.execute();
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to insert logout inventory for player " + uuid + " ; " + e);
		}
	}

	/**
	 * Saves the given ItemMap for the given UUID as death inventory
	 * 
	 * @param uuid
	 *            UUID of the player
	 * @param inventory
	 *            ItemMap representing the players inventory when dying
	 */
	public void insertDeathInventory(UUID uuid, ItemMap inventory) {
		ensureConnection();
		try (PreparedStatement ps = db.prepareStatement("insert into deathInventories (uuid,inventory) values(?,?);")) {
			ps.setString(1, uuid.toString());
			ps.setBlob(2, new SerialBlob(ItemMapBlobHandling.turnItemMapIntoBlob(inventory)));
			ps.execute();
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to insert death inventory for player " + uuid + " ; " + e);
		}
	}

	/**
	 * Loads death inventories saved for the given players, sorted from newest
	 * to oldest
	 * 
	 * @param uuid
	 *            UUID of the player of which we want the death inventories
	 * @param limit
	 *            How many inventories to load
	 * @return Deathinventories of the player
	 */
	public List<DeathInventory> getLastDeathInventories(UUID uuid, int limit) {
		ensureConnection();
		List<DeathInventory> inventories = new LinkedList<DeathInventory>();
		try (PreparedStatement ps = db
				.prepareStatement("select id, inventory, creationTime, returned from deathInventories where uuid=? order by creationTime desc limit ?;")) {
			ps.setString(1, uuid.toString());
			ps.setInt(2, limit);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				Blob blob = rs.getBlob(2);
				Date date = rs.getDate(3);
				boolean returned = rs.getBoolean(4);
				int blobLength = (int) blob.length();
				byte[] blobAsBytes = blob.getBytes(1, blobLength);
				blob.free();
				ItemMap im = ItemMapBlobHandling.turnBlobIntoItemMap(blobAsBytes);
				inventories.add(new DeathInventory(id, uuid, im, returned, date));
			}
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to load death inventories for player " + uuid + " ; " + e);
		}
		return inventories;
	}

	public void updateDeathInventoryReturnStatus(int id, boolean returnStatus) {
		ensureConnection();
		try (PreparedStatement ps = db.prepareStatement("update deathInventories set returned=? where id=?;")) {
			ps.setBoolean(1, returnStatus);
			ps.setInt(2, id);
			ps.execute();
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to update death inventory with id " + id + " ; " + e);
		}
	}

	private void ensureConnection() {
		if (!db.isConnected()) {
			db.connect();
		}
	}
}
