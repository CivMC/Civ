package com.github.civcraft.donum.database;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.rowset.serial.SerialBlob;

import org.bukkit.Bukkit;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.civcraft.donum.Donum;
import com.github.civcraft.donum.misc.ItemMapBlobHandling;

public class DonumDAO {

	private Database db;

	public DonumDAO(String username, String host, int port, String password, String dbname) {
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
			db.execute("create table if not exists deliveryInventories (uuid varchar(36), inventory blob not null, lastUpdate datetime not null default now(), primary key (uuid));");
			db.execute("create table if not exists logoutInventories (uuid varchar(36), inventory blob not null, hash int, lastUpdate datetime not null default now(),primary key(uuid));");
		}

	}

	/**
	 * @return Highest version stored for this plugin in the db versioning table
	 */
	public int getVersion() {
		try (PreparedStatement ps = db
				.prepareStatement("select max(db_version) as db_version from db_version where plugin_name='PrisonPearl';")) {
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
		try (PreparedStatement ps = db
				.prepareStatement("insert into db_version (db_version, plugin_name) values(?,'Donum');")) {
			ps.setInt(1, version);
			ps.execute();
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to insert version " + version + " into tracking: " + e);
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
		try (PreparedStatement ps = db.prepareStatement("select inventory from deliveryInventories where uuid=?;")) {
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				return new ItemMap();
			}
			Blob blob = rs.getBlob(1);
			int blobLength = (int) blob.length();
			byte[] blobAsBytes = blob.getBytes(1, blobLength);
			blob.free();
			return ItemMapBlobHandling.turnBlobIntoItemMap(blobAsBytes);
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to get delivery inventory for player " + uuid + " ; " + e);
			return new ItemMap();
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
		if (im.getTotalItemAmount() == 0) {
			// no items in the inventory, we can just remove its entry completly
			// from the db
			try (PreparedStatement ps = db.prepareStatement("delete from deliveryInventories where uuid=?;")) {
				Donum.getInstance().debug(
						"Delivery inventory for " + uuid.toString() + " is empty, removing any records from the db");
				ps.setString(1, uuid.toString());
				ps.execute();
			} catch (SQLException e) {
				Donum.getInstance().warning(
						"Error deleting record of delivery inventory for player " + uuid + " ; " + e);
			}
			return;
		}
		try (PreparedStatement ps = db
				.prepareStatement("insert into deliveryInventories (uuid,inventory) values(?,?) on duplicate key update inventory=values(inventory);")) {
			Donum.getInstance().debug("Inserting inventory " + im.toString() + " for " + uuid.toString() + " into db");
			ps.setString(1, uuid.toString());
			ps.setBlob(2, new SerialBlob(ItemMapBlobHandling.turnItemMapIntoBlob(im)));
			ps.execute();
		} catch (SQLException e) {
			Donum.getInstance().warning("Error updating record of delivery inventory for player " + uuid + " ; " + e);
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
		try (PreparedStatement ps = db.prepareStatement("select inventory, hash from logoutInventories where uuid=?;")) {
			ps.setString(1, uuid.toString());
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
		try (PreparedStatement ps = db
				.prepareStatement("insert into logoutInventories (uuid,inventory,hash) values(?,?,?) on duplicate key update inventory=values(inventory), hash = values(hash);")) {
			ps.setString(1, uuid.toString());
			ps.setBlob(2, new SerialBlob(ItemMapBlobHandling.turnItemMapIntoBlob(items)));
			ps.setInt(3, items.hashCode());
			ps.execute();
		} catch (SQLException e) {
			Donum.getInstance().warning("Failed to insert logout inventor for player " + uuid + " ; " + e);
		}
	}
}
