/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.types;

import java.util.List;


public class Gearblock {
	private int id;
	private BlockCoord coord;
	private boolean powered;
	private boolean removed;
	private GearblockLink link;
	private Integer timer;
	private TimerOperation timerOperation;
	private TimerMode timerMode;
	private long lastSwitchTime;
	private Gearblock lockGearblock;
	private List<Gearblock> lockedGearblocks;

	public Gearblock(BlockCoord coord) {
		this.coord = coord;
	}

	public int getId() { return this.id; }
	public void setId(int value) { this.id = value; }

	public BlockCoord getCoord() { return this.coord; }

	public boolean isRemoved() { return this.removed; }
	public void setRemoved() { this.removed = true; }

	public boolean isPowered() { return this.powered; }
	public void setPowered(boolean value) { this.powered = value; }

	public GearblockLink getLink() { return this.link != null && !this.link.isBroken() ? this.link: null; }
	public GearblockLink getBrokenLink() { return this.link != null && this.link.isBroken() ? this.link: null; }
	public void setLink(GearblockLink link) { this.link = link; }

	public Integer getTimer() { return this.timer; }
	public TimerOperation getTimerOperation() { return this.timerOperation; }
	public TimerMode getTimerMode() { return this.timerMode; }
	public void setTimer(Integer timer, TimerOperation timerOperation, TimerMode timerMode) {
		this.timer = timer;
		this.timerOperation = timerOperation;
		this.timerMode = timerMode;
	}

	public long getLastSwitchTime() { return this.lastSwitchTime; }
	public void setLastSwitchTime() { this.lastSwitchTime = System.currentTimeMillis(); }

	public void setLockGearblock(Gearblock lockGearblock) { this.lockGearblock = lockGearblock; }
	public Gearblock getLockGearblock() { return this.lockGearblock; }

	public void setLockedGearblocks(List<Gearblock> lockedGearblocks) { this.lockedGearblocks = lockedGearblocks; }
	public List<Gearblock> getLockedGearblocks() { return this.lockedGearblocks; }

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Gearblock)) {
			return false;
		}

		Gearblock object = (Gearblock) other;

		return this.coord.equals(object.coord);
	}

	@Override
	public int hashCode() {
		return this.coord.hashCode();
	}

	@Override
	public String toString() {
		return this.coord.toString();
	}
}
