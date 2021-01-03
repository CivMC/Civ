package com.programmerdan.minecraft.banstick.data;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * BSSessions management DAO for BSSession.
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 */
public final class BSSessions {

	private BSPlayer forPlayer;
	private List<Long> sessionList;
	
	private BSSessions() { }
	
	/**
	 * Get a BSSessions DAO for a specific player.
	 * 
	 * @param newPlayer the player to get a Sessions DAO for.
	 * @return the BSessions.
	 */
	public static BSSessions onlyFor(BSPlayer newPlayer) {
		BSSessions sessions = new BSSessions();
		sessions.forPlayer = newPlayer;
		sessions.sessionList = null;
		return sessions;
	}
	
	/**
	 * @param session When called, releases this BSSessions DAO.
	 */
	public static void release(BSSessions session) {
		if (session != null) {
			session.sessionList.clear();
			session.sessionList = null;
			session.forPlayer = null;
		}
	}
	
	/**
	 * @return the latest session in this BSSessions set. Null if no sessions.
	 */
	public BSSession getLatest() {
		if (sessionList == null) {
			fill();
		}
		if (sessionList.isEmpty()) {
			return null;
		}
		return BSSession.byId(sessionList.get(sessionList.size() - 1));
	}
	
	/**
	 * @return the list of sessions from this BSSession set. Empty List if no sessions.
	 */
	public List<BSSession> getAll() {
		if (sessionList == null) {
			fill();
		}
		
		List<BSSession> all = new ArrayList<>();
		
		if (sessionList != null && !sessionList.isEmpty()) {
			for (Long sid : sessionList) {
				all.add(BSSession.byId(sid));
			}
		}
		
		return all;
	}
	
	private void fill() {
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getIDs = connection.prepareStatement(// Get all ids only, order by join time.
						"SELECT sid FROM bs_session WHERE pid = ? ORDER BY join_time;");) {
			// TODO: replace statement w/ view.
			getIDs.setLong(1, forPlayer.getId());
			try (ResultSet rs = getIDs.executeQuery()) {
				sessionList = new ArrayList<>();
				while (rs.next()) {
					sessionList.add(rs.getLong(1));
				}
				if (sessionList.isEmpty()) {
					BanStick.getPlugin().warning("No Sessions for " + forPlayer.getName());
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to get list of Session ids", se);
		}
	}

	/**
	 * We assume all prior sessions are ended; this method doesn't check.
	 * 
	 * @param ip The IP in use for this session.
	 * @param sessionStart The start time of the session
	 * @return the new Session.
	 */
	public BSSession startNew(BSIP ip, Date sessionStart) {
		BSSession session = BSSession.create(forPlayer, sessionStart, ip);
		if (session != null) {
			sessionList.add(session.getId());
		} // TODO: else throw exception
		return session;
	}

	/**
	 * Helper that safely ends the latest session if not already ended.
	 * 
	 * @param sessionEnd the Date to use for ending the latest session.
	 */
	public void endLatest(Date sessionEnd) {
		BSSession session = getLatest();
		if (session != null) {
			session.setLeaveTime(sessionEnd);
		} else {
			BanStick.getPlugin().warning("Call to end a session, but no active session: " + forPlayer.getId());
		}
	}
}
