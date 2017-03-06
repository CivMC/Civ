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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

public class BSSession {

	private static Map<Long, BSSession> allSessionID = new HashMap<Long, BSSession>();
	private static ConcurrentLinkedQueue<WeakReference<BSSession>> dirtySessions = new ConcurrentLinkedQueue<WeakReference<BSSession>>();
	private boolean dirty;
	
	private long sid;
	private Long deferPid;
	private BSPlayer pid;
	
	private Timestamp joinTime;
	private Timestamp leaveTime;

	private Long deferIid;
	private BSIP iid;
	
	private BSSession() {}

	public Date getJoinTime() {
		return joinTime;
	}
	
	public Date getLeaveTime() {
		return leaveTime;
	}
	
	public void setLeaveTime(Date leaveTime) {
		setLeaveTime(new Timestamp(leaveTime.getTime()));
	}
	
	public void setLeaveTime(Timestamp leaveTime) {
		this.leaveTime = leaveTime;
		this.dirty = true;
		dirtySessions.offer(new WeakReference<BSSession>(this));
	}
	
	public boolean isEnded() {
		return this.leaveTime != null;
	}
	
	public BSPlayer getPlayer() {
		if (pid == null && deferPid != null) {
			pid = BSPlayer.byId(deferPid);
		}
		return pid;
	}
	
	public BSIP getIP() {
		if (iid == null && deferIid != null) {
			iid = BSIP.byId(deferIid);
		}
		return iid;
	}
	
	public long getId() {
		return sid;
	}
	
	/**
	 * This leverages a fun queue of WeakReferences, where if a player is forcibly flush()'d we don't care, or if a player is in the queue more then once
	 * we don't care, b/c we only save a dirty player once; and since we all store references and no copies, everything is nice and synchronized.
	 * 
	 */
	public static void saveDirty() {
		int batchSize = 0;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement save = connection.prepareStatement("UPDATE bs_session SET leave_time = ? WHERE sid = ?");) {
			while (!dirtySessions.isEmpty()) {
				WeakReference<BSSession> rsession = dirtySessions.poll();
				BSSession session = rsession.get();
				if (session != null && session.dirty) {
					session.dirty = false;
					session.saveToStatement(save);
					save.addBatch();
					batchSize ++;
				}
				if (batchSize > 0 && batchSize % 100 == 0) {
					int[] batchRun = save.executeBatch();
					if (batchRun.length != batchSize) {
						BanStick.getPlugin().severe("Some elements of the dirty batch didn't save? " + batchSize + " vs " + batchRun.length);
					} else {
						BanStick.getPlugin().debug("Session batch: {0} saves", batchRun.length);
					}
					batchSize = 0;
				}
			}
			if (batchSize > 0 && batchSize % 100 > 0) {
				int[] batchRun = save.executeBatch();
				if (batchRun.length != batchSize) {
					BanStick.getPlugin().severe("Some elements of the dirty batch didn't save? " + batchSize + " vs " + batchRun.length);
				} else {
					BanStick.getPlugin().debug("Session batch: {0} saves", batchRun.length);
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Save of BSSession dirty batch failed!: ", se);
		}
	}
	
	/**
	 * Saves the BSSession; only for internal use. Outside code must use Flush();
	 */
	private void save() {
		if (!dirty) return;
		this.dirty = false; // don't let anyone else in!
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement save = connection.prepareStatement("UPDATE bs_session SET leave_time = ? WHERE sid = ?");) {
			saveToStatement(save);
			int effects = save.executeUpdate();
			if (effects == 0) {
				BanStick.getPlugin().severe("Failed to save BSSession or no update? " + this.sid);
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Save of BSSession failed!: ", se);
		}
	}
	
	private void saveToStatement(PreparedStatement save) throws SQLException {
		if (this.leaveTime == null) {
			save.setNull(1, Types.TIMESTAMP);
		} else {
			save.setTimestamp(1, this.leaveTime);
		}
		save.setLong(2, this.sid);
	}
	
	/**
	 * Cleanly saves this player if necessary, and removes it from the references lists.
	 */
	public void flush() {
		if (dirty) {
			save();
		}
		allSessionID.remove(this.sid);
		this.pid = null;
		this.deferPid = null;
		this.iid = null;
		this.deferIid = null;
	}
	
	public static BSSession byId(long sid) {
		if (allSessionID.containsKey(sid)) {
			return allSessionID.get(sid);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getId = connection.prepareStatement("SELECT * FROM bs_session WHERE sid = ?");) {
			getId.setLong(1, sid);
			try (ResultSet rs = getId.executeQuery();) {
				if (rs.next()) {
					BSSession nS = internalGetSession(rs);
					allSessionID.put(sid, nS);
					return nS;
				} else {
					BanStick.getPlugin().warning("Failed to retrieve Session by id: " + sid + " - not found");
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Retrieval of session by ID failed: " + sid, se);
		}
		return null;
	}

	private static BSSession internalGetSession(ResultSet rs) throws SQLException {
		BSSession nS = new BSSession();
		nS.sid = rs.getLong(1);
		nS.deferPid = rs.getLong(2);
		//nS.pid = BSPlayer.byId(rs.getLong(2));
		nS.joinTime = rs.getTimestamp(3);
		try {
			nS.leaveTime = rs.getTimestamp(4);
		} catch (SQLException se) {
			nS.leaveTime = null;
		}
		nS.deferIid = rs.getLong(5);
		//nS.iid = BSIP.byId(rs.getLong(5));
		nS.dirty = false;
		return nS;
	}
	
	public static List<BSSession> byIP(BSIP iid) {
		ArrayList<BSSession> sessions = new ArrayList<BSSession>();
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getIds = connection.prepareStatement("SELECT * FROM bs_session WHERE iid = ?");) {
			getIds.setLong(1, iid.getId());
			try (ResultSet rs = getIds.executeQuery();) {
				while (rs.next()) {
					long sid = rs.getLong(1);
					if (allSessionID.containsKey(sid)) {
						sessions.add(allSessionID.get(sid));
						continue;
					}
					
					BSSession nS = internalGetSession(rs);
					sessions.add(nS);
					allSessionID.put(nS.sid, nS);
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Retrieval of sessions by IP failed: " + iid.toString(), se);
		}
		return sessions;
	}

	public static BSSession create(BSPlayer pid, Date sessionStart, BSIP iid) {
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement newSession = connection.prepareStatement(
						"INSERT INTO bs_session(pid, join_time, iid) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);) {
			BSSession session = new BSSession();
			session.pid = pid;
			session.deferPid = pid.getId();
			session.joinTime = new Timestamp(sessionStart.getTime());
			session.iid = iid;
			session.deferIid = iid.getId();
			
			newSession.setLong(1, pid.getId());
			newSession.setTimestamp(2, session.joinTime);
			newSession.setLong(3,  iid.getId());
			int ins = newSession.executeUpdate();
			if (ins < 1) {
				BanStick.getPlugin().warning("Insert reported no session inserted?" + pid.getName());
			}
		
			try (ResultSet rs = newSession.getGeneratedKeys()) {
				if (rs.next()) {
					long sid = rs.getLong(1);
					session.sid = sid;
					session.dirty = false;
					allSessionID.put(sid, session);
					return session;
				} else {
					BanStick.getPlugin().severe("Failed to get ID from inserted session!? " + pid.getName());
					return null;
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to insert new session for " + pid.getName(), se);
		}
		return null;
	}
	
	/**
	 * Preloads a block of session data.
	 * 
 	 * @param offset (not included) ID to retrieve after
	 * @param limit how many to retrieve
	 * @return last ID encountered or -1 if none.
	 */
	public static long preload(long offset, int limit) {
		long maxId = -1;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement loadSessions = connection.prepareStatement("SELECT * FROM bs_session WHERE sid > ? ORDER BY sid LIMIT ?");) {
			loadSessions.setLong(1, offset);
			loadSessions.setInt(2, limit);
			try (ResultSet rs = loadSessions.executeQuery()) {
				while (rs.next()) {
					BSSession session = new BSSession();
					session.dirty = false;
					session.sid = rs.getLong(1);
					session.deferPid = rs.getLong(2);
					//session.pid = BSPlayer.byId(rs.getLong(2));
					session.joinTime = rs.getTimestamp(3);
					try {
						session.leaveTime = rs.getTimestamp(4);
					} catch (SQLException se) {
						session.leaveTime = null;
					}
					session.deferIid = rs.getLong(5);
					//session.iid = BSIP.byId(rs.getLong(5));

					if (!allSessionID.containsKey(session.sid)) {
						allSessionID.put(session.sid, session);
					}
					
					if (session.sid > maxId) maxId = session.sid;
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed during Session preload, offset " + offset + " limit " + limit, se);
		}
		return maxId;
	}
	
	@Override
	public String toString() {
		return toFullString(true);
	}
	
	/**
	 * Shows IP
	 * 
	 * @param showIP determines if to show IP or not
	 * @return the display string
	 */
	public String toFullString(boolean showIP) {
		StringBuffer sb = new StringBuffer();
		sb.append(getPlayer().getName()).append(" [");
		if (showIP) {
			sb.append(getIP().toString());
		} else {
			sb.append(getIP().getId());
		}
		sb.append("]: ");
		sb.append(getJoinTime().toString());
		if (isEnded()) {
			sb.append(" - ").append(getLeaveTime());
		}
		return sb.toString();
	}
}
