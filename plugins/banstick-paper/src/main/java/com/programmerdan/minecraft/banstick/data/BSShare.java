package com.programmerdan.minecraft.banstick.data;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

public class BSShare {

	private static Map<Long, BSShare> allShareID = new HashMap<Long, BSShare>();
	private static ConcurrentLinkedQueue<WeakReference<BSShare>> dirtyShares = new ConcurrentLinkedQueue<WeakReference<BSShare>>();
	private boolean dirty;
	
	private long sid;
	private BSPlayer firstPlayer;
	private BSPlayer secondPlayer;
	
	private BSSession firstSession;
	private BSSession secondSession;
	
	private Timestamp createTime;
	private Timestamp pardonTime;
	
	private BSShare() {}
	
	public long getId() {
		return this.sid;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	
	public Date getPardonTime() {
		return pardonTime;
	}
	
	public void setPardonTime(Date pardonTime) {
		setPardonTime(new Timestamp(pardonTime.getTime()));
	}
	
	public void setPardonTime(Timestamp pardonTime) {
		this.pardonTime = pardonTime;
		this.dirty = true;
		dirtyShares.offer(new WeakReference<BSShare>(this));
	}
	
	public boolean isPardoned() {
		return this.pardonTime != null;
	}
	
	public BSPlayer getFirstPlayer() {
		return firstPlayer;
	}
	
	public BSPlayer getSecondPlayer() {
		return secondPlayer;
	}

	public BSSession getFirstSession() {
		return firstSession;
	}
	
	public BSSession getSecondSession() {
		return secondSession;
	}
	public static BSShare byId(long sid) {
		if (allShareID.containsKey(sid)) {
			return allShareID.get(sid);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getId = connection.prepareStatement("SELECT * FROM bs_share WHERE sid = ?");) {
			getId.setLong(1, sid);
			try (ResultSet rs = getId.executeQuery();) {
				if (rs.next()) {
					BSShare nS = internalGetShare(rs);
					allShareID.put(sid, nS);
					return nS;
				} else {
					BanStick.getPlugin().warning("Failed to retrieve Share by id: " + sid + " - not found");
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Retrieval of Share by ID failed: " + sid, se);
		}

		return null;
	}

	private static BSShare internalGetShare(ResultSet rs) throws SQLException {
		BSShare nS = new BSShare();
		nS.sid = rs.getLong(1);
		// TODO: refactor to avoid recursive lookups.
		nS.createTime = rs.getTimestamp(2);
		nS.firstPlayer = BSPlayer.byId(rs.getLong(3));
		nS.secondPlayer = BSPlayer.byId(rs.getLong(4));
		nS.firstSession = BSSession.byId(rs.getLong(5));
		nS.secondSession = BSSession.byId(rs.getLong(6));
		try {
			nS.pardonTime = rs.getTimestamp(8);
		} catch (SQLException se) {
			nS.pardonTime = null;
		}
		nS.dirty = false;
		return nS;
	}
	
	public static void saveDirty() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Saves the BSShare; only for internal use. Outside code must use Flush();
	 */
	private void save() {
		if (!dirty) return;
		this.dirty = false; // don't let anyone else in!
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement save = connection.prepareStatement("UPDATE bs_share SET pardon = ?, pardon_time = ? WHERE sid = ?");) {
			saveToStatement(save);
			int effects = save.executeUpdate();
			if (effects == 0) {
				BanStick.getPlugin().severe("Failed to save BSShare or no update? " + this.sid);
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Save of BSShare failed!: ", se);
		}
	}
	
	private void saveToStatement(PreparedStatement save) throws SQLException {
		if (this.pardonTime == null) {
			save.setBoolean(1, false);
			save.setNull(2, Types.TIMESTAMP);
		} else {
			save.setBoolean(1, true);
			save.setTimestamp(2, this.pardonTime);
		}
		save.setLong(3, this.sid);
	}
	
	/**
	 * Cleanly saves this player if necessary, and removes it from the references lists.
	 */
	public void flush() {
		if (dirty) {
			save();
		}
		allShareID.remove(this.sid);
		this.firstPlayer = null;
		this.secondPlayer = null;
		this.firstSession = null;
		this.secondSession = null;
	}

	public static long preload(long offset, int limit) {
		// TODO Auto-generated method stub
		return -1;
	}
	
	@Override
	public String toString() {
		return "";
	}
	
	public String toFullString(boolean showIPs) {
		return "";
	}

	public static BSShare create(BSSession overlap, BSSession session) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
