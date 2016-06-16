/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.types;


public class GearState {
	private int id;
	private BlockCoord coord;
	private boolean powered;
	private boolean removed;
	private GearLink link;
	private long lastSwitchTime;
	
	public GearState(BlockCoord coord) {
		this.coord = coord;
	}
	
	public int getId() { return this.id; }
	public void setId(int value) { this.id = value; }

	public BlockCoord getCoord() { return this.coord; }
	
	public boolean isRemoved() { return this.removed; }
	public void setRemoved() { this.removed = true; }
	
	public boolean isPowered() { return this.powered; }
	public void setPowered(boolean value) { this.powered = value; }
	
	public GearLink getLink() { return this.link != null && !this.link.isBroken() ? this.link: null; }
	public GearLink getBrokenLink() { return this.link != null && this.link.isBroken() ? this.link: null; }
	public void setLink(GearLink link) { this.link = link; }
	
	public long getLastSwitchTime() { return this.lastSwitchTime; }
	public void setLastSwitchTime() { this.lastSwitchTime = System.currentTimeMillis(); }

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof GearState)) {
			return false;
		}

		GearState object = (GearState) other;
		
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
