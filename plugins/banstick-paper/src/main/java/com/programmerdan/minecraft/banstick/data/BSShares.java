package com.programmerdan.minecraft.banstick.data;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * BSShares DAO management object.
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public final class BSShares {
	
	private BSPlayer forPlayer;
	private List<Long> shareList;
	private Set<Long> overlaps;
	private Set<Long> unpardonedList;

	private BSShares() { }
	
	/**
	 * Get BSShares DAO for a particular BSPlayer
	 * 
	 * @param player the Player to look at
	 * @return the BSShares DAO for this player.
	 */
	public static BSShares onlyFor(BSPlayer player) {
		BSShares shares = new BSShares();
		shares.forPlayer = player;
		shares.shareList = null;
		shares.overlaps = null;
		shares.unpardonedList = null;
		return shares;
	}
	
	/**
	 * Release a BSShares DAO.
	 * 
	 * @param shares the BSShares DAO to release.
	 */
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
	
	/**
	 * Get all Shares related to the configured player.
	 * 
	 * @return a List of Shares, which could be empty if none are known.
	 */
	public List<BSShare> getAll() {
		if (shareList == null) {
			fill();
		}
		
		List<BSShare> all = new ArrayList<>();
		
		if (shareList != null && !shareList.isEmpty()) {
			for (Long sid : shareList) {
				all.add(BSShare.byId(sid));
			}
		}
		
		return all;
	}
	
	/**
	 * Get all shares that are not pardoned.
	 * 
	 * @return a List of Shares, which could be empty if all are pardoned.
	 */
	public List<BSShare> getUnpardoned() {
		if (shareList == null) {
			fill();
		}
		
		List<BSShare> unpardoned = new ArrayList<>();
		
		if (unpardonedList != null && !unpardonedList.isEmpty()) {
			for (Long sid : unpardonedList) {
				unpardoned.add(BSShare.byId(sid));
			}
		}
		
		return unpardoned;
	}
	
	/**
	 * Get all players that have shares with the configured player.
	 * 
	 * @return a List of Players, which could be empty if none are shared.
	 */
	public List<BSPlayer> getSharesWith() {
		if (shareList == null) {
			fill();
		}
		
		List<BSPlayer> players = new ArrayList<>();
		if (overlaps != null && !overlaps.isEmpty()) {
			for (Long pid : overlaps) {
				players.add(BSPlayer.byId(pid));
			}
		}
		
		return players;
	}
	
	/**
	 * Get all Shares between this player and the passed player.
	 * 
	 * @param player the player to find shares with
	 * @return a list of shares, might be empty if no shares exist.
	 */
	public List<BSShare> getSharesWith(BSPlayer player) {
		if (shareList == null) {
			fill();
		}
		List<BSShare> returns = new ArrayList<>();
		if (shareList == null || shareList.isEmpty()) {
			return returns;
		}
		for (Long id : shareList) {
			BSShare share = BSShare.byId(id);
			if (share != null && (player.getId() == share.getFirstPlayer().getId()
					|| player.getId() == share.getSecondPlayer().getId())) {
				returns.add(share);
			}
		}
		return returns;
	}

	/**
	 * Check if we have any overlap with a specific player.
	 * 
	 * @param playerId The player to check
	 * @return true if any share overlaps, false otherwise.
	 */
	public boolean doesShareWith(Long playerId) {
		if (shareList == null) { 
			fill();
		}
		return overlaps.contains(playerId);
	}
	
	private synchronized void fill() {
		if (shareList != null) {
			return;
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getIDs = connection.prepareStatement(// Get all ids only, order by create time.
						"SELECT sid, first_pid, second_pid, pardon FROM bs_share WHERE first_pid = ? OR second_pid = ? ORDER BY create_time;");) {
			getIDs.setLong(1, forPlayer.getId());
			getIDs.setLong(2, forPlayer.getId());
			try (ResultSet rs = getIDs.executeQuery()) {
				List<Long> localShareList = new ArrayList<>();
				overlaps = new HashSet<>();
				unpardonedList = new HashSet<>();
				while (rs.next()) {
					localShareList.add(rs.getLong(1));
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
				if (localShareList.isEmpty()) {
					BanStick.getPlugin().info("No Shares for {0}", forPlayer.getName());
				}
				shareList = localShareList;
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to get list of Share ids", se);
		}
	}
	
	/**
	 * Check if there's any overlap between this Shares and a given session.
	 * 
	 * @param overlap The session to check against.
	 */
	public void check(BSSession overlap) {
		if (shareList == null) {
			fill(); 
		}
		
		// looks for all potential overlaps based on IP information
		// Checks for existing overlaps
		// If found, do nothing more.
		// If new, create a new share.
		List<BSSession> allSessions = BSSession.byIP(overlap.getIP());
		for (BSSession session : allSessions) {
			if (forPlayer.getId() == session.getPlayer().getId()
					|| session.getId() == overlap.getId()) {
				continue;
			}
			
			if (!overlaps.contains(session.getPlayer().getId())) { // we know. We only record first overlap.
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
	
	/**
	 * Adds a new share / player to this BSShare.
	 * @param share the Share
	 * @param player the Player
	 */
	public void addNew(BSShare share, BSPlayer player) {
		if (shareList == null) { 
			fill(); 
		}
		
		this.shareList.add(share.getId());
		this.overlaps.add(player.getId());
		this.unpardonedList.add(share.getId());
		// be sure it gets promoted to the opposing record
		
		BanStick.getPlugin().info("Found new overlap between {0} and {1}", forPlayer.getName(), player.getName());

	}
	
	/**
	 * @return Determine share ordinality.
	 */
	public int shareOrdinality() {
		if (shareList == null) {
			fill(); 
		}
		return shareList.size();
	}

	/**
	 * @return Determine the share unpardoned ordinality
	 */
	public int unpardonedOrdinality() {
		if (shareList == null) { 
			fill();
		}
		return unpardonedList.size();
	}
	
	/**
	 * @param share the Share to pardon.
	 */
	public void markPardoned(BSShare share) {
		if (shareList == null) {
			fill();
		}
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
	
	/**
	 * @param share the Share to unpardon
	 */
	public void markUnpardoned(BSShare share) {
		if (shareList == null) {
			fill();
		}
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

	/**
	 * @return the latest Share
	 */
	public BSShare getLatest() {
		if (shareList == null) {
			fill();
		}
		if (!shareList.isEmpty()) {
			return BSShare.byId(shareList.get(shareList.size() - 1));
		}
		return null;
	}
}
