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

import org.bukkit.ChatColor;

public class BSShare {

	private static Map<Long, BSShare> allShareID = new HashMap<Long, BSShare>();
	private static ConcurrentLinkedQueue<WeakReference<BSShare>> dirtyShares = new ConcurrentLinkedQueue<WeakReference<BSShare>>();
	private boolean dirty;
	
	private long sid;
	private Long deferFirstPlayer;
	private BSPlayer firstPlayer;
	private Long deferSecondPlayer;
	private BSPlayer secondPlayer;
	
	private Long deferFirstSession;
	private BSSession firstSession;
	private Long deferSecondSession;
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
		if (isPardoned()) {
			firstPlayer.pardonShare(this);
			secondPlayer.pardonShare(this);
		} else {
			firstPlayer.unpardonShare(this);
			secondPlayer.unpardonShare(this);
		}
	}
	
	public boolean isPardoned() {
		return this.pardonTime != null && this.pardonTime.compareTo(new Date()) <= 0;
	}
	
	public BSPlayer getFirstPlayer() {
		if (firstPlayer == null && deferFirstPlayer != null) {
			firstPlayer = BSPlayer.byId(deferFirstPlayer);
		}
		return firstPlayer;
	}
	
	public BSPlayer getSecondPlayer() {
		if (secondPlayer == null && deferSecondPlayer != null) {
			secondPlayer = BSPlayer.byId(deferSecondPlayer);
		}
		return secondPlayer;
	}

	public BSSession getFirstSession() {
		if (firstSession == null && deferFirstSession != null) {
			firstSession = BSSession.byId(deferFirstSession);
		}
		return firstSession;
	}
	
	public BSSession getSecondSession() {
		if (secondSession == null && deferSecondSession != null) {
			secondSession = BSSession.byId(deferSecondSession);
		}
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

	public static List<BSShare> byPlayer(BSPlayer player) {
		List<BSShare> shares = new ArrayList<BSShare>();
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getId = connection.prepareStatement("SELECT * FROM bs_share WHERE first_pid = ? OR second_pid = ?");) {
			getId.setLong(1, player.getId());
			getId.setLong(2, player.getId());
			try (ResultSet rs = getId.executeQuery();) {
				while(rs.next()) {
					if (allShareID.containsKey(rs.getLong(1))) {
						shares.add(allShareID.get(rs.getLong(1)));
						continue;
					}
					BSShare nS = internalGetShare(rs);
					allShareID.put(rs.getLong(1), nS);
					shares.add(nS);
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Retrieval of Shares by Player failed: " + player.toString(), se);
		}
		return shares;
	}
	
	public static List<BSShare> bySession(BSSession session) {
		List<BSShare> shares = new ArrayList<BSShare>();
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getId = connection.prepareStatement("SELECT * FROM bs_share WHERE first_sid = ? OR second_sid = ?");) {
			getId.setLong(1, session.getId());
			getId.setLong(2, session.getId());
			try (ResultSet rs = getId.executeQuery();) {
				while(rs.next()) {
					if (allShareID.containsKey(rs.getLong(1))) {
						shares.add(allShareID.get(rs.getLong(1)));
						continue;
					}
					BSShare nS = internalGetShare(rs);
					allShareID.put(rs.getLong(1), nS);
					shares.add(nS);
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Retrieval of Shares by Session failed: " + session.toString(), se);
		}
		return shares;
	}
	
	private static BSShare internalGetShare(ResultSet rs) throws SQLException {
		BSShare nS = new BSShare();
		nS.sid = rs.getLong(1);
		nS.dirty = false;
		nS.createTime = rs.getTimestamp(2);
		nS.deferFirstPlayer = rs.getLong(3);
		//nS.firstPlayer = BSPlayer.byId(rs.getLong(3));
		nS.deferSecondPlayer = rs.getLong(4);
		//nS.secondPlayer = BSPlayer.byId(rs.getLong(4));
		nS.deferFirstSession = rs.getLong(5);
		//nS.firstSession = BSSession.byId(rs.getLong(5));
		nS.deferSecondSession = rs.getLong(6);
		//nS.secondSession = BSSession.byId(rs.getLong(6));
		if (rs.getBoolean(7)) {
			try {
				nS.pardonTime = rs.getTimestamp(8);
			} catch (SQLException se) {
				nS.pardonTime = null; // data is inconsistent.
				nS.dirty = true;
				dirtyShares.offer(new WeakReference<BSShare>(nS));
			}
		} else {
			nS.pardonTime = null;
		}
		return nS;
	}
	
	public static void saveDirty() {
		int batchSize = 0;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement save = connection.prepareStatement("UPDATE bs_share SET pardon = ?, pardon_time = ? WHERE sid = ?");) {
			while (!dirtyShares.isEmpty()) {
				WeakReference<BSShare> rshare = dirtyShares.poll();
				BSShare share = rshare.get();
				if (share != null && share.dirty) {
					share.dirty = false;
					share.saveToStatement(save);
					save.addBatch();
					batchSize ++;
				}
				if (batchSize > 0 && batchSize % 100 == 0) {
					int[] batchRun = save.executeBatch();
					if (batchRun.length != batchSize) {
						BanStick.getPlugin().severe("Some elements of the dirty batch didn't save? " + batchSize + " vs " + batchRun.length);
					} else {
						BanStick.getPlugin().debug("Share batch: {0} saves", batchRun.length);
					}
					batchSize = 0;
				}
			}
			if (batchSize > 0 && batchSize % 100 > 0) {
				int[] batchRun = save.executeBatch();
				if (batchRun.length != batchSize) {
					BanStick.getPlugin().severe("Some elements of the dirty batch didn't save? " + batchSize + " vs " + batchRun.length);
				} else {
					BanStick.getPlugin().debug("Share batch: {0} saves", batchRun.length);
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Save of BSShare dirty batch failed!: ", se);
		}
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
		this.deferFirstPlayer = null;
		this.firstPlayer = null;
		this.deferSecondPlayer = null;
		this.secondPlayer = null;
		this.deferFirstSession = null;
		this.firstSession = null;
		this.deferSecondPlayer = null;
		this.secondSession = null;
	}

	public static long preload(long offset, int limit) {
		long maxId = -1;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement loadShares = connection.prepareStatement("SELECT * FROM bs_share WHERE sid > ? ORDER BY sid LIMIT ?");) {
			loadShares.setLong(1, offset);
			loadShares.setInt(2, limit);
			try (ResultSet rs = loadShares.executeQuery()) {
				while (rs.next()) {
					if (rs.getLong(1) > maxId) maxId = rs.getLong(1);
					if (allShareID.containsKey(rs.getLong(1))) {
						continue;
					}
					
					BSShare share = internalGetShare(rs);
					allShareID.put(share.sid, share);
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed during Share preload, offset " + offset + " limit " + limit, se);
		}
		return maxId;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (this.isPardoned()) {
			sb.append(ChatColor.GREEN).append("[Pardoned] ");
		}
		sb.append(ChatColor.DARK_PURPLE).append("Share by ")
			.append(ChatColor.WHITE).append(getFirstPlayer().getName()).append(ChatColor.DARK_PURPLE).append(" and ")
			.append(ChatColor.WHITE).append(getSecondPlayer().getName()).append(ChatColor.DARK_PURPLE).append(" via ")
			.append(ChatColor.WHITE).append(getFirstSession().toString()).append(ChatColor.DARK_PURPLE).append(" with ")
			.append(ChatColor.WHITE).append(getSecondSession().toString());	
		return sb.toString();
	}
	
	public String toFullString(boolean showIPs) {
		if (showIPs) {
			return toString();
		} else {
			StringBuffer sb = new StringBuffer();
			if (this.isPardoned()) {
				sb.append(ChatColor.GREEN).append("[Pardoned] ");
			}
			sb.append(ChatColor.DARK_PURPLE).append("Share by ")
				.append(ChatColor.WHITE).append(getFirstPlayer().getName()).append(ChatColor.DARK_PURPLE).append(" and ")
				.append(ChatColor.WHITE).append(getSecondPlayer().getName()).append(ChatColor.DARK_PURPLE).append(" via ")
				.append(ChatColor.WHITE).append(getFirstSession().toFullString(showIPs)).append(ChatColor.DARK_PURPLE).append(" with ")
				.append(ChatColor.WHITE).append(getSecondSession().toFullString(showIPs));
			return sb.toString();
		}
	}

	public static BSShare create(BSSession overlap, BSSession session) {
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement newShare = connection.prepareStatement(
						"INSERT INTO bs_share(create_time, first_pid, second_pid, first_sid, second_sid, pardon, pardon_time) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);) {
			BSShare share = new BSShare();
			share.createTime =  new Timestamp(Calendar.getInstance().getTimeInMillis());
			share.deferFirstPlayer = overlap.getPlayer().getId();
			share.firstPlayer = overlap.getPlayer();
			share.deferSecondPlayer = session.getPlayer().getId();
			share.secondPlayer = session.getPlayer();
			share.deferFirstSession = overlap.getId();
			share.firstSession = overlap;
			share.deferSecondSession = session.getId();
			share.secondSession = session;
			share.pardonTime = null;
			
			newShare.setTimestamp(1, share.createTime);
			newShare.setLong(2,  share.firstPlayer.getId());
			newShare.setLong(3,  share.secondPlayer.getId());
			newShare.setLong(4,  share.firstSession.getId());
			newShare.setLong(5,  share.secondSession.getId());
			newShare.setBoolean(6, false);
			newShare.setNull(7, Types.TIMESTAMP);
			
			int ins = newShare.executeUpdate();
			if (ins < 1) {
				BanStick.getPlugin().warning("Insert reported no share inserted? " + share.getId());
			}
		
			try (ResultSet rs = newShare.getGeneratedKeys()) {
				if (rs.next()) {
					long sid = rs.getLong(1);
					share.sid = sid;
					share.dirty = false;
					allShareID.put(sid, share);
					return share;
				} else {
					BanStick.getPlugin().severe("Failed to get ID from inserted share!? " + overlap.getId() + " - " + session.getId());
					return null;
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to insert new share for sessions!", se);
		}
		return null;
	}
}
