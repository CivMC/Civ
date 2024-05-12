package com.aleksey.castlegates.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.World;

public class TimerBatch {
	private World _world;
	private Gearblock _gearblock;
	private HashSet<Gearblock> _allGearblocks;
	private long _runTimeMillis;
	private PowerResult.Status _processStatus;
	private TimerOperation _timerOperation;
	private TimerMode _timerMode;
	private boolean _isInvalid;
	private List<TimerLink> _links = new ArrayList<>();

	public World getWorld() {
		return _world;
	}

	public Gearblock getGearblock() {
		return _gearblock;
	}

	public TimerMode getTimerMode() { return _timerMode; }

	public HashSet<Gearblock> getAllGearblocks() { return _allGearblocks; }
	public void clearTimerBatchForAllGearblocks() {
		for(Gearblock gearblock : _allGearblocks) {
			gearblock.setTimerBatch(null);
		}
	}

	public List<TimerLink> getLinks() {
		return _links;
	}

	public long getRunTimeMillis() {
		return _runTimeMillis;
	}

	public boolean resetRunTime() {
		if(_gearblock.getTimer() == null) return false;

		_runTimeMillis = System.currentTimeMillis() + _gearblock.getTimer() * 1000;

		return true;
	}

	public void setProcessStatus(PowerResult.Status processStatus) {
		_processStatus = processStatus;
	}

	public PowerResult.Status getProcessStatus() {
		return _processStatus;
	}

	public boolean isInvalid() { return _isInvalid; }
	public void invalidate() { _isInvalid = true; }

	public TimerBatch(World world, Gearblock gearblock, HashSet<Gearblock> allGearblocks) {
		_world = world;
		_gearblock = gearblock;
		_timerOperation = gearblock.getTimerOperation();
		_timerMode = gearblock.getTimerMode();
		_allGearblocks = allGearblocks;

		for(Gearblock current : _allGearblocks) {
			if(current.getTimerBatch() != null) {
				current.getTimerBatch().invalidate();
			}

			current.setTimerBatch(this);
		}

		resetRunTime();
	}

	public void addLink(GearblockLink link) {
		boolean mustDraw = _timerOperation == TimerOperation.DRAW
				|| _timerOperation == TimerOperation.REVERT && !link.isDrawn();

		_links.add(new TimerLink(link, mustDraw));
	}

	public TimerBatch clone(Gearblock newGearblock) {
		TimerBatch clone = new TimerBatch(_world, newGearblock, _allGearblocks);

		clone._world = _world;
		clone._gearblock = newGearblock;
		clone._allGearblocks = _allGearblocks;
		clone._runTimeMillis = _runTimeMillis;
		clone._processStatus = _processStatus;
		clone._timerOperation = _timerOperation;
		clone._timerMode = _timerMode;
		clone._isInvalid = false;
		clone._links = _links;

		return clone;
	}
}
