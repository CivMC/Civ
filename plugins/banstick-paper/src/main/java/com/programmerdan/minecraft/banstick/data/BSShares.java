package com.programmerdan.minecraft.banstick.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
		
		if (unpardonedList != null && unpardonedList.size() > 0) {
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
	
	public boolean doesShareWith(Long playerId) {
		if (shareList == null) { fill(); }
		return overlaps.contains(playerId);
	}
	
	public List<BSShare> getSharesWith(BSPlayer player) {
		if (shareList == null) { fill(); }
		List<BSShare> returns = new ArrayList<BSShare>();
		if (shareList == null || shareList.isEmpty()) return returns;
		for (Long id : shareList) {
			BSShare share = BSShare.byId(id);
			if (share != null && (player.getId() == share.getFirstPlayer().getId() || player.getId() == share.getSecondPlayer().getId())) {
				returns.add(share);
			}
		}
		return returns;
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
			if (forPlayer.getId() == session.getPlayer().getId() || session.getId() == overlap.getId()) continue;
			
			if (!overlaps.contains(session.getPlayer().getId())) {// we know. We only record first overlap.
				BSShare newOverlap = BSShare.create(overlap, session);
				if (newOverlap != null) {
					addNew(newOverlap, session.getPlayer());
					session.getPlayer().addShare(newOverlap, forPlayer);
				} else {
					BanStick.getPlugin().debug("Failed while generating share/overlap?");
				}
			}
		}
	}
	
	public void addNew(BSShare share, BSPlayer player) {
		if (shareList == null) { fill(); }
		
		this.shareList.add(share.getId());
		this.overlaps.add(player.getId());
		this.unpardonedList.add(share.getId());
		// be sure it gets promoted to the opposing record
		
		BanStick.getPlugin().info("Found new overlap between {0} and {1}", forPlayer.getName(), player.getName());

	}
	
	public int shareOrdinality() {
		if (shareList == null) { fill(); }
		return shareList.size();
	}
	
	public int unpardonedOrdinality() {
		if (shareList == null) { fill(); }
		return unpardonedList.size();
	}
	
	public void markPardoned(BSShare share) {
		if (shareList == null) { fill(); }
		// Check if a share of this Player
		// Then check if the Share is actually marked pardoned
		// Then shift out of unpardoned list if pardoned.
		// Otherwise, correct.
		if (shareList.contains(share.getId())) {
			if (share.isPardoned()) {
				unpardonedList.remove(share.getId());
			} else {
				markUnpardoned(share);
			}
		}
	}
	
	public void markUnpardoned(BSShare share) {
		if (shareList == null) { fill(); }
		// Check if a share of this Player
		// Then check if Share is actually unpardoned
		// Then shift into the unpardoned list is unpardoned.
		// Otherwise, correct.
		if (shareList.contains(share.getId())) {
			if (!share.isPardoned()) {
				unpardonedList.add(share.getId());
			} else {
				markPardoned(share);
			}
		}
	}

	public BSShare getLatest() {
		if (shareList == null) { fill(); }
		if (shareList.size() > 0) {
			return BSShare.byId(shareList.get(shareList.size() - 1));
		}
		return null;
	}
}
