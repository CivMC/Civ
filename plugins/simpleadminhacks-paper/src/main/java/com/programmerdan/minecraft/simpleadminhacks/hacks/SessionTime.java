package com.programmerdan.minecraft.simpleadminhacks.hacks;

/**
 * Simple data storage and processing class for NewfriendAssist and elsewhere to track playtime.
 * 
 * @author ProgrammerDan <programmerdan@gmail.com>
 */
public class SessionTime {
	private long playtimeTotal;
	private long lastJoin;

	public SessionTime(long joinTime) {
		playtimeTotal = 0l;
		lastJoin = joinTime;
	}

	public long endSession(long leaveTime) {
		if (lastJoin > 0l) {
			playtimeTotal = leaveTime - lastJoin;
			lastJoin = 0;
		}

		return playtimeTotal;
	}

	public void startSession(long startTime) {
		if (lastJoin > 0l) {
			endSession(startTime);
		}
		lastJoin = startTime;
	}

	public long totalTime() {
		long toReturn = playtimeTotal;
		if (lastJoin > 0l && System.currentTimeMillis() > lastJoin) {
			toReturn += System.currentTimeMillis() - lastJoin;
		}
		return toReturn;
	}
}

