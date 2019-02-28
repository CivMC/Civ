package com.programmerdan.minecraft.banstick.data;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.entity.Player;

/**
 * Represents player DAO logic.
 *
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class BSPlayer {

	private static Map<Long, BSPlayer> allPlayersID = new HashMap<>();
	private static Map<UUID, BSPlayer> allPlayersUUID = new HashMap<>();
	private static ConcurrentLinkedQueue<WeakReference<BSPlayer>> dirtyPlayers = new ConcurrentLinkedQueue<>();
	private BSPlayer() {}

	private long pid;
	private String name;
	private UUID uuid;
	private Timestamp firstAdd;
	private Long deferBid;
	private BSBan bid;
	private Timestamp ipPardonTime;
	private Timestamp proxyPardonTime;
	private Timestamp sharedPardonTime;
	private boolean dirty;

	private transient BSSessions allSessions;
	private transient BSIPs allIPs;
	private transient BSShares allShares;
	private transient BSExclusions allExclusions;

	public long getId() {
		return pid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.dirty = true;
		dirtyPlayers.offer(new WeakReference<>(this));
	}

	public UUID getUUID() {
		return uuid;
	}

	public Date getFirstAdd() {
		return firstAdd;
	}

	public Date getIPPardonTime() {
		return ipPardonTime;
	}

	public void setIPPardonTime(Date pardon) {
		this.ipPardonTime = pardon != null ? new Timestamp(pardon.getTime()) : null;
		this.dirty = true;
		dirtyPlayers.offer(new WeakReference<>(this));
	}

	public Date getProxyPardonTime() {
		return proxyPardonTime;
	}

	public void setProxyPardonTime(Date pardon) {
		this.proxyPardonTime = pardon != null ? new Timestamp(pardon.getTime()) : null;
		this.dirty = true;
		dirtyPlayers.offer(new WeakReference<>(this));
	}

	public Date getSharedPardonTime() {
		return sharedPardonTime;
	}

	public void setSharedPardonTime(Date pardon) {
		this.sharedPardonTime = pardon != null ? new Timestamp(pardon.getTime()) : null;
		this.dirty = true;
		dirtyPlayers.offer(new WeakReference<>(this));
	}

	public BSBan getBan() {
		if (this.bid == null && this.deferBid != null) {
			this.bid = BSBan.byId(this.deferBid);
		}
		return this.bid;
	}

	public void setBan(BSBan bid) {
		if (bid == null && this.bid != null) {
			BSLog.register(BSLog.Action.UNBAN, this, this.bid);
		} else if (bid != null && this.bid == null) {
			BSLog.register(BSLog.Action.BAN, this, bid);
		} else if (bid != null && this.bid != null && bid.getId() != this.bid.getId()) {
			BSLog.register(BSLog.Action.CHANGE, this, this.bid, bid);
		}
		this.bid = bid;
		this.deferBid = (bid == null ? null : bid.getId());
		this.dirty = true;
		dirtyPlayers.offer(new WeakReference<>(this));

	}

	/**
	 * This is the heart of the tracking. If a session is already active, ends it; Starts a session for this player, associates that session with the current IP data,
	 * checks for new shares and any other stuff like that.
	 *
	 * @param player The player whose session to start
	 * @param sessionStart The Date that the session began
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
	 * If session is active, end it. Starts a new session, when IP is known.
	 * Checks for new shares.
	 *
	 * @param ip The exact IP to use
	 * @param sessionStart the session start time
	 */
	public void startSession(BSIP ip, Date sessionStart) {
		BSSession latest = this.allSessions.getLatest();
		if (latest != null && !latest.isEnded()) {
			latest.setLeaveTime(sessionStart);
		}
		this.allIPs.setLatest(ip);
		latest = this.allSessions.startNew(ip, sessionStart);
		this.allShares.check(latest);
	}

	/**
	 * Ends the latest session for this player. Player-centric exposed focus for BSSessions's endLatest
	 *
	 * @param sessionEnd Date to end this player's current session.
	 */
	public void endSession(Date sessionEnd) {
		this.allSessions.endLatest(sessionEnd);
	}

	/**
	 * Gets the latest session for this player.
	 *
	 * @return The latest BSSession object
	 */
	public BSSession getLatestSession() {
		return this.allSessions.getLatest();
	}

	/**
	 * Gets all sessions for this player
	 *
	 * @return A list of all BSSessions for this player
	 */
	public List<BSSession> getAllSessions() {
		return this.allSessions.getAll();
	}

	/**
	 * This leverages a fun queue of WeakReferences, where if a player is forcibly flush()'d we don't care, or if a player is in the queue more then once
	 * we don't care, b/c we only save a dirty player once; and since we all store references and no copies, everything is nice and synchronized.
	 *
	 */
	public static void saveDirty() {
		int batchSize = 0;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement savePlayer = connection.prepareStatement("UPDATE bs_player SET ip_pardon_time = ?, proxy_pardon_time = ?, shared_pardon_time = ?, bid = ?, name = ? WHERE pid = ?");) {
			while (!dirtyPlayers.isEmpty()) {
				WeakReference<BSPlayer> rplayer = dirtyPlayers.poll();
				BSPlayer player = rplayer.get();
				if (player != null && player.dirty) {
					player.dirty = false;
					player.saveToStatement(savePlayer);
					savePlayer.addBatch();
					batchSize ++;
				}
				if (batchSize > 0 && batchSize % 100 == 0) {
					int[] batchRun = savePlayer.executeBatch();
					if (batchRun.length != batchSize) {
						BanStick.getPlugin().severe("Some elements of the dirt batch didn't save? " + batchSize + " vs " + batchRun.length);
					} else {
						BanStick.getPlugin().debug("Player batch: {0} saves", batchRun.length);
					}
					batchSize = 0;
				}
			}
			if (batchSize > 0 && batchSize % 100 > 0) {
				int[] batchRun = savePlayer.executeBatch();
				if (batchRun.length != batchSize) {
					BanStick.getPlugin().severe("Some elements of the dirt batch didn't save? " + batchSize + " vs " + batchRun.length);
				} else {
					BanStick.getPlugin().debug("Player batch: {0} saves", batchRun.length);
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
				PreparedStatement savePlayer = connection.prepareStatement("UPDATE bs_player SET ip_pardon_time = ?, proxy_pardon_time = ?, shared_pardon_time = ?, bid = ?, name = ? WHERE pid = ?");) {
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
		if (this.ipPardonTime == null) {
			savePlayer.setNull(1,  Types.TIMESTAMP);
		} else {
			savePlayer.setTimestamp(1,  this.ipPardonTime);
		}
		if (this.proxyPardonTime == null) {
			savePlayer.setNull(2, Types.TIMESTAMP);
		} else {
			savePlayer.setTimestamp(2, this.proxyPardonTime);
		}
		if (this.sharedPardonTime == null) {
			savePlayer.setNull(3, Types.TIMESTAMP);
		} else {
			savePlayer.setTimestamp(3, this.sharedPardonTime);
		}
		if (this.getBan() == null) {
			savePlayer.setNull(4, Types.BIGINT);
		} else {
			savePlayer.setLong(4,  this.bid.getId());
		}
		if (this.name == null) {
			savePlayer.setNull(5,  Types.VARCHAR);
		} else {
			savePlayer.setString(5, this.name);
		}
		savePlayer.setLong(6, this.pid);
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
		this.bid = null;
		this.deferBid = null;
	}

	/**
	 * Create a new player from the Bukkit object
	 *
	 * @param player The player object to use
	 * @return the BSPlayer created from the player, or null
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
			newPlayer.allExclusions = BSExclusions.onlyFor(newPlayer);

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
	 * @param uuid Gets a BSPlayer record by uuid.
	 * @return BSPlayer matching the uuid or null if not found
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
						return player;
					} else {
						player = new BSPlayer();
						player.pid = pid;
						player.allSessions = BSSessions.onlyFor(player);
						player.allExclusions = BSExclusions.onlyFor(player);
						player.allIPs = BSIPs.onlyFor(player);
						player.allShares = BSShares.onlyFor(player);
					}
					player.dirty = false;
					player.name = rs.getString(2);
					player.uuid = UUID.fromString(rs.getString(3));
					player.firstAdd = rs.getTimestamp(4);
					long bid = rs.getLong(5);
					if (!rs.wasNull()) {
						player.deferBid = bid;
						player.bid = null;
					}
					//player.bid = rs.wasNull() ? null : BSBan.byId(bid);
					try {
						player.ipPardonTime = rs.getTimestamp(6);
					} catch (SQLException te) {
						player.ipPardonTime = null;
					}
					try {
						player.proxyPardonTime = rs.getTimestamp(7);
					} catch (SQLException te) {
						player.proxyPardonTime = null;
					}
					try {
						player.sharedPardonTime = rs.getTimestamp(8);
					} catch (SQLException te) {
						player.sharedPardonTime = null;
					}
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
	 * Direct lookup for a BSPlayer by database ID
	 *
	 * @param pid The Player ID to use in the lookup
	 * @return The Player found, or null if not found
	 */
	public static BSPlayer byId(final long pid) {
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
					player.allExclusions = BSExclusions.onlyFor(player);
					player.allIPs = BSIPs.onlyFor(player);
					player.allShares = BSShares.onlyFor(player);
					player.name = rs.getString(2);
					player.uuid = UUID.fromString(rs.getString(3));
					player.firstAdd = rs.getTimestamp(4);
					long bid = rs.getLong(5);
					if (!rs.wasNull()) {
						player.deferBid = bid;
						player.bid = null;
					}
					//player.bid = rs.wasNull() ? null : BSBan.byId(bid);
					try {
						player.ipPardonTime = rs.getTimestamp(6);
					} catch (SQLException te) {
						player.ipPardonTime = null;
					}
					try {
						player.proxyPardonTime = rs.getTimestamp(7);
					} catch (SQLException te) {
						player.proxyPardonTime = null;
					}
					try {
						player.sharedPardonTime = rs.getTimestamp(8);
					} catch (SQLException te) {
						player.sharedPardonTime = null;
					}
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
		return null;
	}

	public static BSPlayer create(UUID playerId, String name) {
		if (allPlayersUUID.containsKey(playerId)) {
			return allPlayersUUID.get(playerId);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection()) {
			BSPlayer newPlayer = new BSPlayer();
			newPlayer.dirty = false;
			newPlayer.name = name;
			newPlayer.uuid = playerId;
			newPlayer.firstAdd = new Timestamp(Calendar.getInstance().getTimeInMillis());

			try (PreparedStatement insertPlayer = connection.prepareStatement("INSERT INTO bs_player(name, uuid, first_add) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
				if (newPlayer.name == null) {
					insertPlayer.setNull(1, Types.VARCHAR);
				} else {
					insertPlayer.setString(1, newPlayer.name);
				}
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
			newPlayer.allExclusions = BSExclusions.onlyFor(newPlayer);

			allPlayersID.put(newPlayer.pid, newPlayer);
			allPlayersUUID.put(newPlayer.uuid, newPlayer);
			return newPlayer;
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to create a new player record: ", se);
			return null;
		}
	}

	/**
	 * Preloads a segment of player data. Offset indicates lowbound exclusive to begin, with limit constraining size of batch.
	 *
	 * @param offset (not included) low bound on ID of record to load.
	 * @param limit how many to load
	 * @return last ID encountered, or -1 is none/no more
	 */
	public static long preload(long offset, int limit) {
		long maxId = -1;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement loadPlayers = connection.prepareStatement("SELECT * FROM bs_player WHERE pid > ? ORDER BY pid LIMIT ?");) {
			loadPlayers.setLong(1, offset);
			loadPlayers.setInt(2, limit);
			try (ResultSet rs = loadPlayers.executeQuery()) {
				while (rs.next()) {
					long nPid = rs.getLong(1);
					if (nPid > maxId) maxId = nPid;
					if (allPlayersID.containsKey(nPid)) {
						continue; // skip those we know.
					}
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
					if (!rs.wasNull()) {
						player.deferBid = bid;
						player.bid = null;
					}
					//player.bid = rs.wasNull() ? null : BSBan.byId(bid);
					try {
						player.ipPardonTime = rs.getTimestamp(6);
					} catch (SQLException te) {
						player.ipPardonTime = null;
					}
					try {
						player.proxyPardonTime = rs.getTimestamp(7);
					} catch (SQLException te) {
						player.proxyPardonTime = null;
					}
					try {
						player.sharedPardonTime = rs.getTimestamp(8);
					} catch (SQLException te) {
						player.sharedPardonTime = null;
					}

					player.allSessions = BSSessions.onlyFor(player);
					player.allIPs = BSIPs.onlyFor(player);
					player.allShares = BSShares.onlyFor(player);
					player.allExclusions = BSExclusions.onlyFor(player);

					if (!allPlayersID.containsKey(player.pid)) {
						allPlayersID.put(player.pid, player);
					}
					if (!allPlayersUUID.containsKey(player.uuid)) {
						allPlayersUUID.put(player.uuid, player);
					}
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed during Player preload, offset " + offset + " limit " + limit, se);
		}
		return maxId;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.name);
		if (this.getBan() != null) {
			sb.append(" [Banned]");
		}
		if (this.ipPardonTime != null) {
			sb.append(" [IP Pardoned]");
		}
		if (this.proxyPardonTime != null) {
			sb.append(" [Proxy Pardoned]");
		}
		if (this.sharedPardonTime != null) {
			sb.append(" [Share Pardoned]");
		}
		return sb.toString();
	}

	public void pardonShare(BSShare share) {
		this.allShares.markPardoned(share);
	}

	public void unpardonShare(BSShare share) {
		this.allShares.markUnpardoned(share);
	}

	public int getUnpardonedShareCardinality() {
		return this.allShares.unpardonedOrdinality();
	}

	public int getTotalShareCardinality() {
		return this.allShares.shareOrdinality();
	}

	public BSShare getLatestShare() {
		return this.allShares.getLatest();
	}

	public List<BSShare> getUnpardonedShares() {
		return this.allShares.getUnpardoned();
	}

	public List<BSShare> sharesWith(BSPlayer player) {
		if (this.allShares.doesShareWith(player.getId())) {
			return this.allShares.getSharesWith(player);
		} else {
			return null;
		}
	}

	/**
	 * Recursively retrieves all other BSPlayers this one has a transitive IP connection to.
	 * For example if Player A uses IP X, Player B uses IP X &amp; Y and Player C uses IP Y,
	 * the players A and C would not have a share. This function digs recursively through shares
	 * though to find connections like that one
	 *
	 * @param respectExclusions Whether player specific exclusions should be taken into account to ignore specific connections
	 *
	 * @return Set containing all BSPlayer this one has transitive ip association with
	 */
	public Set<BSPlayer> getTransitiveSharedPlayers(boolean respectExclusions) {
	   return getTransitiveSharedPlayersRecursive(new HashSet<BSPlayer>(), respectExclusions);
	}

	private Set <BSPlayer> getTransitiveSharedPlayersRecursive(Set <BSPlayer> alts, boolean respectExclusions) {
	    alts.add(this);
	    for(BSShare share : getAllShares()) {
	        if (share.isPardoned()) {
	            continue;
	        }
	        if (!alts.contains(share.getFirstPlayer()) && !(respectExclusions && allExclusions.hasExclusionWith(share.getFirstPlayer()))) {
	            share.getFirstPlayer().getTransitiveSharedPlayersRecursive(alts, respectExclusions);
	        }
	        if (!alts.contains(share.getSecondPlayer()) && !(respectExclusions && allExclusions.hasExclusionWith(share.getSecondPlayer()))) {
                share.getSecondPlayer().getTransitiveSharedPlayersRecursive(alts, respectExclusions);
            }
	    }
	    return alts;
	}

	public List<BSShare> getAllShares() {
		return this.allShares.getAll();
	}

	public void addShare(BSShare share, BSPlayer player) {
		this.allShares.addNew(share, player);
	}

	public void addExclusion(BSExclusion excl) {
	    this.allExclusions.addNew(excl);
	}

	public void removeExclusion(BSExclusion excl) {
	    this.allExclusions.remove(excl);
	}

	public BSExclusion getExclusionWith(BSPlayer player) {
	    return allExclusions.getExclusionWith(player);
	}

	@Override
    public int hashCode() {
	    return Objects.hash(getId());
	}
}
