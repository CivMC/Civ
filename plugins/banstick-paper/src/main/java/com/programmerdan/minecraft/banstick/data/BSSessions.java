package com.programmerdan.minecraft.banstick.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

public class BSSessions {

	private BSSessions() {}
	
	private BSPlayer forPlayer;
	
	private List<Long> sessionList;
	
	public static BSSessions onlyFor(BSPlayer newPlayer) {
		BSSessions sessions = new BSSessions();
		sessions.forPlayer = newPlayer;
		sessions.sessionList = null;
		return sessions;
	}
	
	public static void release(BSSessions session) {
		if (session != null) {
			session.sessionList.clear();
			session.sessionList = null;
			session.forPlayer = null;
		}
	}
	
	public BSSession getLatest() {
		if (sessionList == null) {
			fill();
		}
		if (sessionList.size() == 0) return null;
		return BSSession.byId(sessionList.get(sessionList.size() - 1));
	}
	
	public List<BSSession> getAll() {
		if (sessionList == null) {
			fill();
		}
		
		List<BSSession> all = new ArrayList<BSSession>();
		
		if (sessionList != null && sessionList.size() > 0) {
			for (Long sid : sessionList) {
				all.add(BSSession.byId(sid));
			}
		}
		
		return all;
	}
	
	private void fill() {
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getIDs = connection.prepareStatement( // Get all ids only, order by join time.
					"SELECT sid FROM bs_session WHERE pid = ? ORDER BY join_time;");) {
				// TODO: replace statement w/ view.
			getIDs.setLong(1, forPlayer.getId());
			try (ResultSet rs = getIDs.executeQuery()) {
				sessionList = new ArrayList<Long>();
				while (rs.next()) {
					sessionList.add(rs.getLong(1));
				}
				if (sessionList.size() == 0) {
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

	public void endLatest(Date sessionEnd) {
		BSSession session = getLatest();
		if (session != null) {
			session.setLeaveTime(sessionEnd);
		} else {
			BanStick.getPlugin().warning("Call to end a session, but no active session: " + forPlayer.getId());
		}
	}
}
