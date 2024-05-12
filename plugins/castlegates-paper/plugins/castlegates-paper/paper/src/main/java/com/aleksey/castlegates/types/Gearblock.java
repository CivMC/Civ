/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.types;

import java.util.List;


public class Gearblock {
	private int _id;
	private final BlockCoord _coord;
	private boolean _powered;
	private boolean _removed;
	private GearblockLink _link;
	private Integer _timer;
	private TimerOperation _timerOperation;
	private TimerMode _timerMode;
	private TimerBatch _timerBatch;
	private long _lastSwitchTime;
	private Gearblock _lockGearblock;
	private List<Gearblock> _lockedGearblocks;

	public Gearblock(BlockCoord coord) {
		_coord = coord;
	}

	public int getId() { return _id; }
	public void setId(int value) { _id = value; }

	public BlockCoord getCoord() { return _coord; }

	public boolean isRemoved() { return _removed; }
	public void setRemoved() { _removed = true; }

	public boolean isPowered() { return _powered; }
	public void setPowered(boolean value) { _powered = value; }

	public GearblockLink getLink() { return _link != null && !_link.isBroken() ? _link : null; }
	public GearblockLink getBrokenLink() { return _link != null && _link.isBroken() ? _link : null; }
	public void setLink(GearblockLink link) { _link = link; }

	public Integer getTimer() { return _timer; }
	public TimerOperation getTimerOperation() { return _timerOperation; }
	public TimerMode getTimerMode() { return _timerMode; }
	public void setTimer(Integer timer, TimerOperation timerOperation, TimerMode timerMode) {
		_timer = timer;
		_timerOperation = timerOperation;
		_timerMode = timerMode;
	}

	public TimerBatch getTimerBatch() { return _timerBatch; }
	public void setTimerBatch(TimerBatch timerBatch) { _timerBatch = timerBatch; }

	public long getLastSwitchTime() { return _lastSwitchTime; }
	public void setLastSwitchTime() { _lastSwitchTime = System.currentTimeMillis(); }

	public void setLockGearblock(Gearblock lockGearblock) { _lockGearblock = lockGearblock; }
	public Gearblock getLockGearblock() { return _lockGearblock; }

	public void setLockedGearblocks(List<Gearblock> lockedGearblocks) { _lockedGearblocks = lockedGearblocks; }
	public List<Gearblock> getLockedGearblocks() { return _lockedGearblocks; }

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Gearblock object))
			return false;

		return _coord.equals(object._coord);
	}

	@Override
	public int hashCode() {
		return _coord.hashCode();
	}

	@Override
	public String toString() {
		return _coord.toString();
	}
}
