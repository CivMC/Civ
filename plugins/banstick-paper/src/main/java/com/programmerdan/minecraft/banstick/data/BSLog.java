package com.programmerdan.minecraft.banstick.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

/**
 * TODO: Make accessors
 *
 * Currently a depositor log for changes to player ban status. Not all bans have internal tracking so this external log ensures that all bans made or removed are tracked.
 * 
 * @author ProgrammerDan
 *
 */
public class BSLog extends BukkitRunnable{

	public BSLog(FileConfiguration config) {
		super();
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
	
	private int maxBatch = 100;
	private long delay = 100l;
	private long period = 1200l;
	
	public long getDelay() {
		return delay;
	}
	
	public long getPeriod() {
		return period;
	}
	
	static enum Action {
		BAN,
		UNBAN,
		CHANGE
	}
	
	static class LogEntry {
		Action action;
		long ban;
		long player;
		Date time;
		
		public LogEntry(Action action, BSPlayer player, BSBan ban) {
			this.action = action;
			this.ban = ban.getId();
			this.player = player.getId();
			this.time = new Date();
		}
	}
	
	private static ConcurrentLinkedQueue<LogEntry> toSave = new ConcurrentLinkedQueue<LogEntry>();

	@Override
	public void run() {
		// drain the queue into the DB in batches
		if (toSave.isEmpty()) return;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement saveEm = connection.prepareStatement("INSERT INTO bs_ban_log (pid, bid, action) VALUES (?, ?, ?);");) {
			int cBatch = 0;
			while (cBatch < this.maxBatch && !toSave.isEmpty()) {
				LogEntry nextCheck = toSave.poll();
				if (nextCheck == null) break; // we're somehow empty already
				saveEm.setLong(1, nextCheck.player);
				saveEm.setLong(2, nextCheck.ban);
				saveEm.setString(3, nextCheck.action.toString());
				saveEm.addBatch();
				cBatch ++;
			}
			int[] batchRun = saveEm.executeBatch();
			if (batchRun.length != cBatch) {
				BanStick.getPlugin().severe("Some elements of the log batch didn't save? " + cBatch + " vs " + batchRun.length);
			} else {
				BanStick.getPlugin().debug("Log batch: {0} saves", batchRun.length);
			}

		} catch (Exception e) {
			BanStick.getPlugin().severe("Warning, lost elements, some log entries failed to save!", e);
		}
	}

	public static void register(Action action, BSPlayer bsPlayer, BSBan...bid) {
		switch(action) {
		case BAN:
		case UNBAN:
			if (bid.length > 0) toSave.offer(new LogEntry(action, bsPlayer, bid[0]));
			break;
		case CHANGE:
			if (bid.length > 0) toSave.offer(new LogEntry(Action.UNBAN, bsPlayer, bid[0]));
			if (bid.length > 1) toSave.offer(new LogEntry(Action.BAN, bsPlayer, bid[1]));
			break;
		}
	}
	
	public void disable() {
		while (!toSave.isEmpty()) {
			run();
		}
	}
}
