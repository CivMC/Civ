package com.programmerdan.minecraft.banstick.data;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

public class BSBan {

	private static Map<Long, BSBan> allBanID = new HashMap<Long, BSBan>();
	private static ConcurrentLinkedQueue<WeakReference<BSBan>> dirtyBans = new ConcurrentLinkedQueue<WeakReference<BSBan>>();
	private boolean dirty;
	
	private BSBan() {}
	
	/*
	 * 					" bid BIGINT AUTOINCREMENT PRIMARY KEY," +
					" ban_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" ip_ban REFERENCES bs_ip(iid)," +
					" vpn_ban REFERENCES bs_vpn(vid)," +
					" share_ban REFERENCES bs_share(sid)," +
					" admin_ban BOOLEAN," +
					" message TEXT," +
					" ban_end TIMESTAMP," +
	 */
	
	private long bid; 
	private Timestamp banTime;
	private BSIP ipBan;
	private BSVPN vpnBan;
	private BSShare shareBan;
	private boolean isAdminBan;
	private String message; //mutable
	private Timestamp banEnd; //mutable
	
	public long getId() {
		return this.bid;
	}

	public Date getBanTime() {
		return banTime;
	}
	
	public BSIP getIPBan() {
		return ipBan;
	}
	
	public BSVPN getVPNBan() {
		return vpnBan;
	}
	
	public BSShare getShareBan() {
		return shareBan;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
		this.dirty = true;
		dirtyBans.offer(new WeakReference<BSBan>(this));
	}
	
	public Date getBanEndTime() {
		return banEnd;
	}
	
	public void setBanEndTime(Date banEnd) {
		setBanEndTime(new Timestamp(banEnd.getTime()));
	}
	
	public void setBanEndTime(Timestamp banEnd) {
		this.banEnd = banEnd;
		this.dirty = true;
		dirtyBans.offer(new WeakReference<BSBan>(this));
	}
	
	public boolean isAdminBan() {
		return isAdminBan;
	}
	
	public void setAdminBan(boolean isAdminBan) {
		this.isAdminBan = isAdminBan;
	}
	
	/**
	 * This leverages a fun queue of WeakReferences, where if a player is forcibly flush()'d we don't care, or if a player is in the queue more then once
	 * we don't care, b/c we only save a dirty player once; and since we all store references and no copies, everything is nice and synchronized.
	 * 
	 */
	public static void saveDirty() {
		int batchSize = 0;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement save = connection.prepareStatement("UPDATE bs_ban SET admin_ban = ?, message = ?, ban_end = ? WHERE bid = ?");) {
			while (!dirtyBans.isEmpty()) {
				WeakReference<BSBan> rban = dirtyBans.poll();
				BSBan ban = rban.get();
				if (ban != null && ban.dirty) {
					ban.saveToStatement(save);
					save.addBatch();
					batchSize ++;
				}
				if (batchSize % 100 == 0) {
					int[] batchRun = save.executeBatch();
					if (batchRun.length != batchSize) {
						BanStick.getPlugin().severe("Some elements of the dirty batch didn't save? " + batchSize + " vs " + batchRun.length);
					} else {
						BanStick.getPlugin().debug("Ban batch: {0} saves", batchRun.length);
					}
					batchSize = 0;
				}
			}
			if (batchSize % 100 > 0) {
				int[] batchRun = save.executeBatch();
				if (batchRun.length != batchSize) {
					BanStick.getPlugin().severe("Some elements of the dirty batch didn't save? " + batchSize + " vs " + batchRun.length);
				} else {
					BanStick.getPlugin().debug("Ban batch: {0} saves", batchRun.length);
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Save of BSBan dirty batch failed!: ", se);
		}
	}
	
	/**
	 * Saves the BSBan; only for internal use. Outside code must use Flush();
	 */
	private void save() {
		if (!dirty) return;
		this.dirty = false; // don't let anyone else in!
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement save = connection.prepareStatement("UPDATE bs_ban SET admin_ban = ?, message = ?, ban_end = ? WHERE bid = ?");) {
			saveToStatement(save);
			int effects = save.executeUpdate();
			if (effects == 0) {
				BanStick.getPlugin().severe("Failed to save BSBan or no update? " + this.bid);
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Save of BSBan failed!: ", se);
		}
	}
	
	private void saveToStatement(PreparedStatement save) throws SQLException {
		save.setBoolean(1, this.isAdminBan);
		save.setString(2, this.message);
		if (this.banEnd == null) {
			save.setNull(3, Types.TIMESTAMP);
		} else {
			save.setTimestamp(3, this.banEnd);
		}
		save.setLong(4, this.bid);
	}
	
	/**
	 * Cleanly saves this player if necessary, and removes it from the references lists.
	 */
	public void flush() {
		if (dirty) {
			save();
		}
		allBanID.remove(this.bid);
		this.ipBan = null;
		this.vpnBan = null;
		this.shareBan = null;
	}
	
	public static BSBan byId(long bid) {
		if (allBanID.containsKey(bid)) {
			return allBanID.get(bid);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getId = connection.prepareStatement("SELECT * FROM bs_ban WHERE bid = ?");) {
			getId.setLong(1, bid);
			try (ResultSet rs = getId.executeQuery();) {
				if (rs.next()) {
					BSBan ban = extractBan(rs);
					allBanID.put(bid,  ban);
					return ban;
				} else {
					BanStick.getPlugin().warning("Failed to retrieve Ban by id: " + bid + " - not found");
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Retrieval of ban by ID failed: " + bid, se);
		}
		return null;
	}

	public static BSBan create(String message, Date banEnd, boolean adminBan) {
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection()) {
			BSBan newBan = new BSBan();
			newBan.dirty = false;
			newBan.banTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
			newBan.banEnd = banEnd != null ? new Timestamp(banEnd.getTime()) : null;
			newBan.message = message;
			newBan.isAdminBan = adminBan;
			
			try (PreparedStatement insertBan = connection.prepareStatement("INSERT INTO bs_ban(ban_time, message, ban_end, admin_ban) VALUES (?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
				insertBan.setTimestamp(1, newBan.banTime);
				if (newBan.message != null) {
					insertBan.setString(2, newBan.message);
				} else {
					insertBan.setNull(2, Types.VARCHAR);
				}
				if (newBan.banEnd != null) {
					insertBan.setTimestamp(3, newBan.banEnd);
				} else {
					insertBan.setNull(3, Types.TIMESTAMP);
				}
				insertBan.setBoolean(4, adminBan);
				insertBan.execute();
				try (ResultSet rs = insertBan.getGeneratedKeys()) {
					if (rs.next()) { 
						newBan.bid = rs.getLong(1);
					} else {
						BanStick.getPlugin().severe("No BID returned on ban insert?!");
						return null; // no bid? error.
					}
				}
			}
			
			allBanID.put(newBan.bid, newBan);
			return newBan;
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to create a new ban record: ", se);
		}
		return null;
	}
	
	public static List<BSBan> byIP(BSIP exactIP, boolean includeExpired) {
		List<BSBan> results = new ArrayList<BSBan>();
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement findBans = connection.prepareStatement(
						includeExpired?"SELECT * FROM bs_ban WHERE ip_ban = ? ORDER BY ban_time":
							"SELECT * FROM bs_ban WHERE ip_ban = ? AND (ban_end IS NULL OR ban_end >= CURRENT_TIMESTAMP) ORDER BY ban_time");) {
			findBans.setLong(1, exactIP.getId());
			try (ResultSet rs = findBans.executeQuery()) {
				while(rs.next()) {
					BSBan ban = extractBan(rs);
					if (allBanID.containsKey(ban.bid)) {
						results.add(allBanID.get(ban.bid));
					} else {
						results.add(ban);
						allBanID.put(ban.bid, ban);
					}
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to lookup bans by IP: ", se);
		}
		return results;
	}

	public static BSBan create(BSIP exactIP, String message, Date banEnd, boolean adminBan) {
		// TODO: Check if this IP is already actively banned!
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection()) {
			BSBan newBan = new BSBan();
			newBan.dirty = false;
			newBan.ipBan = exactIP;
			newBan.banTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
			newBan.banEnd = banEnd != null ? new Timestamp(banEnd.getTime()) : null;
			newBan.message = message;
			newBan.isAdminBan = adminBan;
			
			try (PreparedStatement insertBan = connection.prepareStatement("INSERT INTO bs_ban(ban_time, message, ban_end, admin_ban, ip_ban) VALUES (?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
				insertBan.setTimestamp(1, newBan.banTime);
				if (newBan.message != null) {
					insertBan.setString(2, newBan.message);
				} else {
					insertBan.setNull(2, Types.VARCHAR);
				}
				if (newBan.banEnd != null) {
					insertBan.setTimestamp(3, newBan.banEnd);
				} else {
					insertBan.setNull(3, Types.TIMESTAMP);
				}
				insertBan.setBoolean(4, adminBan);
				insertBan.setLong(5,  newBan.ipBan.getId());
				insertBan.execute();
				try (ResultSet rs = insertBan.getGeneratedKeys()) {
					if (rs.next()) { 
						newBan.bid = rs.getLong(1);
					} else {
						BanStick.getPlugin().severe("No BID returned on ban insert?!");
						return null; // no bid? error.
					}
				}
			}
			
			allBanID.put(newBan.bid, newBan);
			return newBan;
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to create a new ban record: ", se);
		}
		return null;
	}
	
	private static BSBan extractBan(ResultSet rs) throws SQLException {
		BSBan nS = new BSBan();
		nS.bid = rs.getLong(1);
		// TODO: refactor to avoid recursive lookups.
		nS.banTime = rs.getTimestamp(2);
		long iid = rs.getLong(3);
		if (!rs.wasNull()) {
			nS.ipBan = BSIP.byId(iid);
		}
		long vid = rs.getLong(4);
		if (!rs.wasNull()) {
			nS.vpnBan = BSVPN.byId(vid);
		}
		long sid = rs.getLong(5);
		if (!rs.wasNull()) {
			nS.shareBan = BSShare.byId(sid);
		}
		nS.isAdminBan = rs.getBoolean(6);
		nS.message = rs.getString(7);
		try {
			nS.banEnd = rs.getTimestamp(8);
		} catch (SQLException se) {
			nS.banEnd = null;
		}
		nS.dirty = false;
		return nS;
	}
	
	public static long preload(long offset, int limit, boolean includeExpired) {
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement loadBans = connection.prepareStatement(
						includeExpired?"SELECT * FROM bs_ban ORDER BY bid OFFSET ? LIMIT ?":
							"SELECT * FROM bs_ban WHERE ban_end IS NULL OR ban_end >= CURRENT_TIMESTAMP ORDER BY bid LIMIT ? OFFSET ?");) {
			loadBans.setLong(2, offset);
			loadBans.setInt(1, limit);
			try (ResultSet rs = loadBans.executeQuery()) {
				long maxId = -1;
				while (rs.next()) {
					BSBan ban = extractBan(rs);
					if (!allBanID.containsKey(ban.bid)) {
						allBanID.put(ban.bid, ban);
					}
					if (ban.bid > maxId) maxId = ban.bid;
				}
				return maxId;
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed during Ban preload, offset " + offset + " limit " + limit, se);
		}
		return -1;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (ipBan != null) {
			sb.append("IP Ban: ").append(ipBan.toString());
		} else if (vpnBan != null) {
			sb.append("VPN Ban: ").append(vpnBan.toString());
		} else if (shareBan != null) {
			sb.append("Share Ban: ").append(shareBan.toString());
		} else {
			sb.append("Player Ban");
		}
		
		if (isAdminBan()) {
			sb.append(" (administrative)");
		}
		
		if (getBanEndTime() != null) {
			if ((new Date()).after(getBanEndTime())) { // passed
				sb.append(" - Expired");
			} else {
				sb.append(" - Until ").append(getBanEndTime());
			}
		} else {
			sb.append(" - Forever");
		}
		
		sb.append(" with message \"").append(message).append("\"");
		return sb.toString();
	}
}
