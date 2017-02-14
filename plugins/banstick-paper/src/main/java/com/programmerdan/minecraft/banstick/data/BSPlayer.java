package com.programmerdan.minecraft.banstick.data;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.entity.Player;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

/**
 * Represents player DAO logic.
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class BSPlayer {
	
	private static Map<Long, BSPlayer> allPlayersID = new HashMap<Long, BSPlayer>();
	private static Map<UUID, BSPlayer> allPlayersUUID = new HashMap<UUID, BSPlayer>();
	private static ConcurrentLinkedQueue<WeakReference<BSPlayer>> dirtyPlayers = new ConcurrentLinkedQueue<WeakReference<BSPlayer>>();
	private BSPlayer() {}
	
	private long pid;
	private String name;
	private UUID uuid;
	private Timestamp firstAdd;
	private BSBan bid;
	private Timestamp vpnPardonTime;
	private Timestamp sharedPardonTime;
	private boolean dirty;
	
	private transient BSSessions allSessions;
	private transient BSIPs allIPs;
	private transient BSShares allShares;
	
	public long getId() {
		return pid;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		this.dirty = true;
		dirtyPlayers.offer(new WeakReference<BSPlayer>(this));
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	public Date getFirstAdd() {
		return firstAdd;
	}
	
	public Date getVpnPardonTime() {
		return vpnPardonTime;
	}
	
	public void setVpnPardonTime(Date pardon) {
		this.vpnPardonTime = pardon != null ? new Timestamp(pardon.getTime()) : null;
		this.dirty = true;
		dirtyPlayers.offer(new WeakReference<BSPlayer>(this));
	}
	
	public Date getSharedPardonTime() {
		return sharedPardonTime;
	}
	
	public void setSharedPardonTime(Date pardon) {
		this.sharedPardonTime = pardon != null ? new Timestamp(pardon.getTime()) : null;
		this.dirty = true;
		dirtyPlayers.offer(new WeakReference<BSPlayer>(this));
	}
	
	public BSBan getBan() {
		return this.bid;
	}

	public void setBan(BSBan bid) {
		this.bid = bid;
		this.dirty = true;
		dirtyPlayers.offer(new WeakReference<BSPlayer>(this));
	}
	
	/**
	 * This is the heart of the tracking. If a session is already active, ends it; Starts a session for this player, associates that session with the current IP data,
	 * checks for new shares and any other stuff like that.
	 * 
	 * @param player
	 * @param sessionStart
	 */
	public void startSession(final Player player, Date sessionStart) {
		BSSession latest = this.allSessions.getLatest();
		if (latest != null && !latest.isEnded()) {
			latest.setLeaveTime(sessionStart);
		}
		BSIP ip = BanStickDatabaseHandler.getInstance().getOrCreateIP(player.getAddress().getAddress());
		this.allIPs.setLatest(ip);
		latest = this.allSessions.startNew(ip, sessionStart);
		this.allShares.check(latest);
	}
	
	/**
	 * Ends the latest session for this player. Player-centric exposed focus for BSSessions's endLatest
	 * 
	 * @param sessionEnd
	 */
	public void endSession(Date sessionEnd) {
		this.allSessions.endLatest(sessionEnd);
	}
	
	/**
	 * Gets the latest session for this player.
	 * 
	 * @return
	 */
	public BSSession getLatestSession() {
		return this.allSessions.getLatest();
	}
	
	/**
	 * This leverages a fun queue of WeakReferences, where if a player is forcibly flush()'d we don't care, or if a player is in the queue more then once
	 * we don't care, b/c we only save a dirty player once; and since we all store references and no copies, everything is nice and synchronized.
	 * 
	 */
	public static void saveDirty() {
		int batchSize = 0;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement savePlayer = connection.prepareStatement("UPDATE bs_player SET vpn_pardon_time = ?, shared_pardon_time = ?, bid = ?, name = ? WHERE pid = ?");) {
			while (!dirtyPlayers.isEmpty()) {
				WeakReference<BSPlayer> rplayer = dirtyPlayers.poll();
				BSPlayer player = rplayer.get();
				if (player != null && player.dirty) {
					player.saveToStatement(savePlayer);
					savePlayer.addBatch();
					batchSize ++;
				}
				if (batchSize % 100 == 0) {
					int[] batchRun = savePlayer.executeBatch();
					if (batchRun.length != batchSize) {
						BanStick.getPlugin().severe("Some elements of the dirt batch didn't save? " + batchSize + " vs " + batchRun.length);
					}
					batchSize = 0;
				}
			}
			if (batchSize % 100 > 0) {
				int[] batchRun = savePlayer.executeBatch();
				if (batchRun.length != batchSize) {
					BanStick.getPlugin().severe("Some elements of the dirt batch didn't save? " + batchSize + " vs " + batchRun.length);
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Save of BSPlayer dirty batch failed!: ", se);
		}
	}
	
	/**
	 * Saves the BSPlayer; only for internal use. Outside code must use Flush();
	 */
	private void save() {
		if (!dirty) return;
		this.dirty = false; // don't let anyone else in!
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement savePlayer = connection.prepareStatement("UPDATE bs_player SET vpn_pardon_time = ?, shared_pardon_time = ?, bid = ?, name = ? WHERE pid = ?");) {
			saveToStatement(savePlayer);
			int effects = savePlayer.executeUpdate();
			if (effects == 0) {
				BanStick.getPlugin().severe("Failed to save BSPlayer or no update? " + this.pid);
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Save of BSPlayer failed!: ", se);
		}
	}
	
	private void saveToStatement(PreparedStatement savePlayer) throws SQLException {
		if (this.vpnPardonTime == null) {
			savePlayer.setNull(1, Types.TIMESTAMP);
		} else {
			savePlayer.setTimestamp(1, this.vpnPardonTime);
		}
		if (this.sharedPardonTime == null) {
			savePlayer.setNull(2, Types.TIMESTAMP);
		} else {
			savePlayer.setTimestamp(2, this.sharedPardonTime);
		}
		if (this.bid == null) {
			savePlayer.setNull(3, Types.BIGINT);
		} else {
			savePlayer.setLong(3,  this.bid.getId());
		}
		if (this.name == null) {
			savePlayer.setNull(4,  Types.VARCHAR);
		} else {
			savePlayer.setString(4, this.name);
		}
		savePlayer.setLong(5, this.pid);
	}
	
	/**
	 * Cleanly saves this player if necessary, and removes it from the references lists.
	 */
	public void flush() {
		if (dirty) {
			save();
		}
		BSSessions.release(this.allSessions);
		BSIPs.release(this.allIPs);
		BSShares.release(this.allShares);
		allPlayersUUID.remove(this.uuid);
		allPlayersID.remove(this.pid);
	}
	
	/**
	 * 
	 */
	public static BSPlayer create(final Player player) {
		if (allPlayersUUID.containsKey(player.getUniqueId())) {
			return allPlayersUUID.get(player);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection()) {
			BSPlayer newPlayer = new BSPlayer();
			newPlayer.dirty = false;
			newPlayer.name = player.getDisplayName();
			newPlayer.uuid = player.getUniqueId();
			newPlayer.firstAdd = new Timestamp(Calendar.getInstance().getTimeInMillis());
			
			try (PreparedStatement insertPlayer = connection.prepareStatement("INSERT INTO bs_player(name, uuid, first_add) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
				insertPlayer.setString(1, newPlayer.name);
				insertPlayer.setString(2, newPlayer.uuid.toString());
				insertPlayer.setTimestamp(3, newPlayer.firstAdd);
				insertPlayer.execute();
				try (ResultSet rs = insertPlayer.getGeneratedKeys()) {
					if (rs.next()) { 
						newPlayer.pid = rs.getLong(1);
					} else {
						BanStick.getPlugin().severe("No PID returned on player insert?!");
						return null; // no pid? error.
					}
				}
			}
			
			newPlayer.allSessions = BSSessions.onlyFor(newPlayer);
			newPlayer.allIPs = BSIPs.onlyFor(newPlayer);
			newPlayer.allShares = BSShares.onlyFor(newPlayer);
			
			allPlayersID.put(newPlayer.pid, newPlayer);
			allPlayersUUID.put(newPlayer.uuid, newPlayer);
			return newPlayer;
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to create a new player record: ", se);
			return null;
		}
	}
	
	/**
	 * Get player by UUID, pull from DB if not cached already.
	 */
	public static BSPlayer byUUID(final UUID uuid) {
		if (allPlayersUUID.containsKey(uuid)) {
			return allPlayersUUID.get(uuid);
		}

		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getPlayer = connection.prepareStatement("SELECT * FROM bs_player WHERE uuid = ?");) {
			getPlayer.setString(1, uuid.toString());
			try (ResultSet rs = getPlayer.executeQuery();) {
				if (rs.next()) {
					// found
					long pid = rs.getLong(1);
					BSPlayer player = null;
					if (allPlayersID.containsKey(pid)) {
						player = allPlayersID.get(pid);
					} else {
						player = new BSPlayer();
						player.pid = pid;
						player.allSessions = BSSessions.onlyFor(player);
						player.allIPs = BSIPs.onlyFor(player);
						player.allShares = BSShares.onlyFor(player);
					}
					player.dirty = false;
					player.name = rs.getString(2);
					player.uuid = UUID.fromString(rs.getString(3));
					player.firstAdd = rs.getTimestamp(4);
					long bid = rs.getLong(5);
					player.bid = rs.wasNull() ? null : BSBan.byId(bid);
					player.vpnPardonTime = rs.getTimestamp(6);
					player.sharedPardonTime = rs.getTimestamp(7);
					allPlayersID.put(pid, player);
					allPlayersUUID.put(player.uuid, player);
					return player;
				} else {
					// not found
					return null; // TODO: exception
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to execute query to get player: " + uuid.toString(), se);
		}
		return null; //TODO: exception
	}
	
	/**
	 * Assume rs is from bs_player, and that next() has been called once.
	 * @throws SQLException 
	 */
	public static BSPlayer byId(final long pid) throws SQLException {
		if (allPlayersID.containsKey(pid)) {
			return allPlayersID.get(pid);
		}

		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getPlayer = connection.prepareStatement("SELECT * FROM bs_player WHERE pid = ?");) {
			getPlayer.setLong(1, pid);
			try (ResultSet rs = getPlayer.executeQuery();) {
				if (rs.next()) {
					// found
					BSPlayer player = null;
					player = new BSPlayer();
					player.dirty = false;
					player.pid = pid;
					player.allSessions = BSSessions.onlyFor(player);
					player.allIPs = BSIPs.onlyFor(player);
					player.allShares = BSShares.onlyFor(player);
					player.name = rs.getString(2);
					player.uuid = UUID.fromString(rs.getString(3));
					player.firstAdd = rs.getTimestamp(4);
					long bid = rs.getLong(5);
					player.bid = rs.wasNull() ? null : BSBan.byId(bid);
					player.vpnPardonTime = rs.getTimestamp(6);
					player.sharedPardonTime = rs.getTimestamp(7);
					allPlayersID.put(pid, player);
					allPlayersUUID.put(player.uuid, player);
					return player;
				} else {
					// not found
					return null; // TODO: exception
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to execute query to get player: " + pid, se);
		}
		return null; // TODO: exception
	}

	public static BSPlayer create(UUID playerId) {
		if (allPlayersUUID.containsKey(playerId)) {
			return allPlayersUUID.get(playerId);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection()) {
			BSPlayer newPlayer = new BSPlayer();
			newPlayer.dirty = false;
			newPlayer.name = null;
			newPlayer.uuid = playerId;
			newPlayer.firstAdd = new Timestamp(Calendar.getInstance().getTimeInMillis());
			
			try (PreparedStatement insertPlayer = connection.prepareStatement("INSERT INTO bs_player(name, uuid, first_add) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
				insertPlayer.setNull(1, Types.VARCHAR);
				insertPlayer.setString(2, newPlayer.uuid.toString());
				insertPlayer.setTimestamp(3, newPlayer.firstAdd);
				insertPlayer.execute();
				try (ResultSet rs = insertPlayer.getGeneratedKeys()) {
					if (rs.next()) { 
						newPlayer.pid = rs.getLong(1);
					} else {
						BanStick.getPlugin().severe("No PID returned on player insert?!");
						return null; // no pid? error.
					}
				}
			}
			
			newPlayer.allSessions = BSSessions.onlyFor(newPlayer);
			newPlayer.allIPs = BSIPs.onlyFor(newPlayer);
			newPlayer.allShares = BSShares.onlyFor(newPlayer);
			
			allPlayersID.put(newPlayer.pid, newPlayer);
			allPlayersUUID.put(newPlayer.uuid, newPlayer);
			return newPlayer;
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to create a new player record: ", se);
			return null;
		}
	}
	
	public static long preload(long offset, int limit) {
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement loadPlayers = connection.prepareStatement("SELECT * FROM bs_player ORDER BY bid OFFSET ? LIMIT ?");) {
			loadPlayers.setLong(1, offset);
			loadPlayers.setInt(2, limit);
			try (ResultSet rs = loadPlayers.executeQuery()) {
				long maxId = -1;
				while (rs.next()) {
					BSPlayer player = new BSPlayer();
					player.dirty = false;
					player.pid = rs.getLong(1);
					player.allSessions = BSSessions.onlyFor(player);
					player.allIPs = BSIPs.onlyFor(player);
					player.allShares = BSShares.onlyFor(player);
					player.name = rs.getString(2);
					player.uuid = UUID.fromString(rs.getString(3));
					player.firstAdd = rs.getTimestamp(4);
					long bid = rs.getLong(5);
					player.bid = rs.wasNull() ? null : BSBan.byId(bid);
					player.vpnPardonTime = rs.getTimestamp(6);
					player.sharedPardonTime = rs.getTimestamp(7);
					if (!allPlayersID.containsKey(player.pid)) {
						allPlayersID.put(player.pid, player);
					}
					if (!allPlayersUUID.containsKey(player.pid)) {
						allPlayersUUID.put(player.uuid, player);
					}
					
					if (player.pid > maxId) maxId = player.pid;
				}
				return maxId;
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed during Player preload, offset " + offset + " limit " + limit, se);
		}
		return -1;
	}
}
