package com.programmerdan.minecraft.banstick.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

public class BSShares {
	
	private BSShares() {}
	
	private BSPlayer forPlayer;
	
	private List<Long> shareList;
	
	private Set<Long> overlaps;
	
	private Set<Long> unpardonedList;

	public static BSShares onlyFor(BSPlayer player) {
		BSShares shares = new BSShares();
		shares.forPlayer = player;
		shares.shareList = null;
		shares.overlaps = null;
		shares.unpardonedList = null;
		return shares;
	}
	
	public static void release(BSShares shares) {
		shares.forPlayer = null;
		if (shares.shareList != null) {
			shares.shareList.clear();
		}
		if (shares.overlaps != null) {
			shares.overlaps.clear();
		}
		if (shares.unpardonedList != null) {
			shares.unpardonedList.clear();
		}
		shares.shareList = null;
	}
	
	public List<BSShare> getAll() {
		if (shareList == null) {
			fill();
		}
		
		List<BSShare> all = new ArrayList<BSShare>();
		
		if (shareList != null && shareList.size() > 0) {
			for (Long sid : shareList) {
				all.add(BSShare.byId(sid));
			}
		}
		
		return all;
	}
	
	public List<BSShare> getUnpardoned() {
		if (shareList == null) { fill(); }
		
		List<BSShare> unpardoned = new ArrayList<BSShare>();
		
		if (unpardoned != null && unpardoned.size() > 0) {
			for (Long sid : unpardonedList) {
				unpardoned.add(BSShare.byId(sid));
			}
		}
		
		return unpardoned;
	}
	
	public List<BSPlayer> getSharesWith() {
		if (shareList == null) { fill(); }
		
		List<BSPlayer> players = new ArrayList<BSPlayer>();
		
		if (overlaps != null && overlaps.size() > 0) {
			for (Long pid : overlaps) {
				players.add(BSPlayer.byId(pid));
			}
		}
		
		return players;
	}
	
	private void fill() {
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getIDs = connection.prepareStatement( // Get all ids only, order by create time.
					"SELECT sid, first_pid, second_pid, pardon FROM bs_share WHERE first_pid = ? OR second_pid = ? ORDER BY create_time;");) {
			getIDs.setLong(1, forPlayer.getId());
			getIDs.setLong(2, forPlayer.getId());
			try (ResultSet rs = getIDs.executeQuery()) {
				shareList = new ArrayList<Long>();
				overlaps = new HashSet<Long>();
				unpardonedList = new HashSet<Long>();
				while (rs.next()) {
					shareList.add(rs.getLong(1));
					long fpid = rs.getLong(2);
					long spid = rs.getLong(3);
					boolean pardon = rs.getBoolean(4);
					if (fpid == forPlayer.getId()) {
						overlaps.add(spid);
					} else {
						overlaps.add(fpid);
					}
					if (!pardon) {
						unpardonedList.add(rs.getLong(1));
					}
				}
				if (shareList.size() == 0) {
					BanStick.getPlugin().info("No Shares for {0}", forPlayer.getName());
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to get list of Share ids", se);
		}
	}
	
	public void check(BSSession overlap) {
		if (shareList == null) { fill(); }
		
		// looks for all potential overlaps based on IP information
		// Checks for existing overlaps
		// If found, do nothing more.
		// If new, create a new share.
		List<BSSession> allSessions = BSSession.byIP(overlap.getIP());
		for (BSSession session : allSessions) {
			if (!overlaps.contains(session.getPlayer().getId())) {// we know. We only record first overlap.
				BSShare newOverlap = BSShare.create(overlap, session);
				if (newOverlap != null) {
					this.shareList.add(newOverlap.getId());
					this.overlaps.add(session.getPlayer().getId());
					this.unpardonedList.add(newOverlap.getId());
					BanStick.getPlugin().info("Found new overlap between {0} and {1}", forPlayer.getName(), session.getPlayer().getName());
				} else {
					BanStick.getPlugin().debug("Failed while generating share/overlap?");
				}
			}
		}
	}
	
	public int shareOrdinality() {
		if (shareList == null) { fill(); }
		return shareList.size();
	}
	
	public int unpardonedOrdinality() {
		if (shareList == null) { fill(); }
		return unpardonedList.size();
	}
}
