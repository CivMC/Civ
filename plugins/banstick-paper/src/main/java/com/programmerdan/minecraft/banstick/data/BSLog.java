package com.programmerdan.minecraft.banstick.data;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.scheduler.BukkitRunnable;

import com.programmerdan.minecraft.banstick.data.BSLog.Action;

import inet.ipaddr.IPAddress;

/**
 * TODO: Make accessors
 *
 * Currently a depositor log for changes to player ban status. Not all bans have internal tracking so this external log ensures that all bans made or removed are tracked.
 * 
 * @author ProgrammerDan
 *
 */
public class BSLog extends BukkitRunnable{

	private int maxBatch = 100;
	
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
		try (// do connection) {
			int cBatch = 0;
			while (cBatch < this.maxBatch && !toSave.isEmpty()) {
				LogEntry nextCheck = toSave.poll();
				if (nextCheck == null) break; // we're somehow empty already

			}
		} catch (Exception e) {
			//ok
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
}
