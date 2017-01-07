/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.types;


public class Gearblock {
	private int id;
	private BlockCoord coord;
	private boolean powered;
	private boolean removed;
	private GearblockLink link;
	private long lastSwitchTime;
	private Integer timer;
	private TimerOperation timerOperation;
	
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
	
	public long getLastSwitchTime() { return this.lastSwitchTime; }
	public void setLastSwitchTime() { this.lastSwitchTime = System.currentTimeMillis(); }
	
	public Integer getTimer() { return this.timer; }
	public TimerOperation getTimerOperation() { return this.timerOperation; }
	public void setTimer(Integer timer, TimerOperation timerOperation) {
		this.timer = timer;
		this.timerOperation = timerOperation;
	}

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
