package com.aleksey.castlegates.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.World;

public class TimerBatch {
	private World world;
	private Gearblock gearblock;
	private HashSet<Gearblock> allGearblocks;
	private long runTimeMillis;
	private PowerResult.Status processStatus;
	private TimerOperation timerOperation;
	private TimerMode timerMode;
	private boolean isInvalid;
	private List<TimerLink> links = new ArrayList<TimerLink>();

	public World getWorld() {
		return this.world;
	}

	public Gearblock getGearblock() {
		return this.gearblock;
	}

	public TimerMode getTimerMode() { return this.timerMode; }

	public HashSet<Gearblock> getAllGearblocks() { return this.allGearblocks; }
	public void clearTimerBatchForAllGearblocks() {
		for(Gearblock gearblock : this.allGearblocks) {
			gearblock.setTimerBatch(null);
		}
	}

	public List<TimerLink> getLinks() {
		return this.links;
	}

	public long getRunTimeMillis() {
		return this.runTimeMillis;
	}

	public boolean resetRunTime() {
		if(this.gearblock.getTimer() == null) return false;

		this.runTimeMillis = System.currentTimeMillis() + this.gearblock.getTimer() * 1000;

		return true;
	}

	public void setProcessStatus(PowerResult.Status processStatus) {
		this.processStatus = processStatus;
	}

	public PowerResult.Status getProcessStatus() {
		return this.processStatus;
	}

	public boolean isInvalid() { return this.isInvalid; }
	public void invalidate() { this.isInvalid = true; }

	public TimerBatch(World world, Gearblock gearblock, HashSet<Gearblock> allGearblocks) {
		this.world = world;
		this.gearblock = gearblock;
		this.timerOperation = gearblock.getTimerOperation();
		this.timerMode = gearblock.getTimerMode();
		this.allGearblocks = allGearblocks;

		for(Gearblock current : this.allGearblocks) {
			if(current.getTimerBatch() != null) {
				current.getTimerBatch().invalidate();
			}

			current.setTimerBatch(this);
		}

		resetRunTime();
	}

	public void addLink(GearblockLink link) {
		boolean mustDraw = this.timerOperation == TimerOperation.DRAW
				|| this.timerOperation == TimerOperation.REVERT && !link.isDrawn();

		this.links.add(new TimerLink(link, mustDraw));
	}

	public TimerBatch clone(Gearblock newGearblock) {
		TimerBatch clone = new TimerBatch(this.world, newGearblock, this.allGearblocks);

		clone.world = this.world;
		clone.gearblock = newGearblock;
		clone.allGearblocks = this.allGearblocks;
		clone.runTimeMillis = this.runTimeMillis;
		clone.processStatus = this.processStatus;
		clone.timerOperation = this.timerOperation;
		clone.timerMode = this.timerMode;
		clone.isInvalid = false;
		clone.links = this.links;

		return clone;
	}
}
