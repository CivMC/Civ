package com.programmerdan.minecraft.banstick.data;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * TODO: Make accessors
 *
 * <p>Currently a depositor log for changes to player ban status. 
 * Not all bans have internal tracking so this external log ensures 
 * that all bans made or removed are tracked.
 * This ONLY works with player bans.
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class BSLog extends BukkitRunnable {

	private static ConcurrentLinkedQueue<LogEntry> toSave = new ConcurrentLinkedQueue<>();

	private int maxBatch = 100;
	private long delay = 100L;
	private long period = 1200L;
	
	public BSLog(FileConfiguration config) {
		config(config.getConfigurationSection("log"));
	}
	
	private void config(ConfigurationSection config) {
		if (config == null) {
			return;
		}
		maxBatch = config.getInt("maxBatch", maxBatch);
		delay = config.getLong("delay", delay);
		period = config.getLong("period", period);
	}
	
	public long getDelay() {
		return delay;
	}
	
	public long getPeriod() {
		return period;
	}
	
	@Override
	public void run() {
		// drain the queue into the DB in batches
		if (toSave.isEmpty()) {
			return;
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement saveEm = connection.prepareStatement(
						"INSERT INTO bs_ban_log (pid, bid, action) VALUES (?, ?, ?);");) {
			int curBatch = 0;
			while (curBatch < this.maxBatch && !toSave.isEmpty()) {
				LogEntry nextCheck = toSave.poll();
				if (nextCheck == null) {
					break; // we're somehow empty already
				}
				saveEm.setLong(1, nextCheck.player);
				saveEm.setLong(2, nextCheck.ban);
				saveEm.setString(3, nextCheck.action.toString());
				saveEm.addBatch();
				curBatch ++;
			}
			int[] batchRun = saveEm.executeBatch();
			if (batchRun.length != curBatch) {
				BanStick.getPlugin().severe("Some elements of the log batch didn't save? "
						+ curBatch + " vs " + batchRun.length);
			} else {
				BanStick.getPlugin().debug("Log batch: {0} saves", batchRun.length);
			}

		} catch (Exception e) {
			BanStick.getPlugin().severe("Warning, lost elements, some log entries failed to save!", e);
		}
	}

	/**
	 * Register a new log entry to eventually be committed.
	 * 
	 * @param action the nature of the log -- BAN/UNBAN/CHANGE
	 * @param bsPlayer the BSPlayer whose ban is being updated
	 * @param bid the Ban (BSBan)
	 */
	public static void register(Action action, BSPlayer bsPlayer, BSBan...bid) {
		switch (action) {
			case BAN:
			case UNBAN:
				if (bid.length > 0) {
					toSave.offer(new LogEntry(action, bsPlayer, bid[0]));
				}
				break;
			case CHANGE:
				if (bid.length > 0) {
					toSave.offer(new LogEntry(Action.UNBAN, bsPlayer, bid[0]));
				}
				if (bid.length > 1) {
					toSave.offer(new LogEntry(Action.BAN, bsPlayer, bid[1]));
				}
				break;
			default:
				break;
		}
	}
	
	/**
	 * Attempts to shut down this log, but first attempts to force a clear of all 
	 * logged entries.
	 */
	public void disable() {
		while (!toSave.isEmpty()) {
			run();
		}
	}

	/**
	 * Internal enum for type of ban.
	 */
	enum Action {
		BAN,
		UNBAN,
		CHANGE
	}
	
	/**
	 * Internal LogEntry container. On creation just strips down to IDs.
	 */
	static class LogEntry {
		Action action;
		long ban;
		long player;
		Date time;
		
		LogEntry(Action action, BSPlayer player, BSBan ban) {
			this.action = action;
			this.ban = ban.getId();
			this.player = player.getId();
			this.time = new Date();
		}
	}
}
