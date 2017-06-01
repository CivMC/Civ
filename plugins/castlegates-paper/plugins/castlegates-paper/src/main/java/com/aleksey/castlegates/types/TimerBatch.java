package com.aleksey.castlegates.types;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;

public class TimerBatch {
	private World world;
	private Gearblock gearblock;
	private long runTimeMillis;
	private PowerResult.Status processStatus;
	private TimerOperation timerOperation;
	private List<TimerLink> links = new ArrayList<TimerLink>();

	public World getWorld() {
		return this.world;
	}

	public Gearblock getGearblock() {
		return this.gearblock;
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

	public TimerBatch(World world, Gearblock gearblock) {
		this.world = world;
		this.gearblock = gearblock;
		this.timerOperation = gearblock.getTimerOperation();

		resetRunTime();
	}

	public void addLink(GearblockLink link) {
		boolean mustDraw = this.timerOperation == TimerOperation.DRAW
				|| this.timerOperation == TimerOperation.REVERT && !link.isDrawn();

		this.links.add(new TimerLink(link, mustDraw));
	}
}
