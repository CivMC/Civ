/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.types;

import java.util.List;

public class GearLink {
	private int id;
	private GearState gear1;
	private GearState gear2;
	private boolean removed;
	private List<BlockState> blocks;
	
	public GearLink(GearState gear1, GearState gear2) {
		this.gear1 = gear1;
		this.gear2 = gear2;
	}
	
	public int getId() { return this.id; }
	public void setId(int value) { this.id = value; }
	
	public GearState getGear1() { return this.gear1; }
	
	public GearState getGear2() { return this.gear2; }

	public boolean isBroken() { return this.gear1 == null || this.gear2 == null; }
	
	public void setBroken(GearState removedGear) {
		if(this.gear1 == removedGear) {
			this.gear1 = null;
		} else {
			this.gear2 = null;
		}
	}
	
	public boolean setRestored(GearState addedGear) {
		if(this.gear1 == null) {
			gear1 = addedGear;
		} else {
			gear2 = addedGear;
		}
		
		return true;
	}
	
	public boolean isRemoved() { return this.removed; }
	public void setRemoved() { this.removed = true; }

	public boolean isDrawn() { return this.blocks != null; }
	public List<BlockState> getBlocks() { return this.blocks; }
	public void setBlocks(List<BlockState> blocks) { this.blocks = blocks; }
}