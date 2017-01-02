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
	
	public static BSBan byId(long bid) {
		if (allBanID.containsKey(bid)) {
			return allBanID.get(bid);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getId = connection.prepareStatement("SELECT * FROM bs_ban WHERE bid = ?");) {
			getId.setLong(1, bid);
			try (ResultSet rs = getId.executeQuery();) {
				if (rs.next()) {
					BSBan nS = new BSBan();
					nS.bid = bid;
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
					nS.banEnd = rs.getTimestamp(8);
					nS.dirty = false;
					return nS;
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
}
