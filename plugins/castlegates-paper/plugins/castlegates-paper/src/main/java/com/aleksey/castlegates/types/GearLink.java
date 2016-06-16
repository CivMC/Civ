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
		if(gear1 != null && gear2 != null) {
			BlockCoord loc1 = gear1.getCoord();
			BlockCoord loc2 = gear2.getCoord();
			
			if(loc1.getX() < loc2.getX() || loc1.getY() < loc2.getY() || loc1.getZ() < loc2.getZ()) {
				this.gear1 = gear1;
				this.gear2 = gear2;
			} else {
				this.gear1 = gear2;
				this.gear2 = gear1;
			}
		} else {
			if(gear1 == null) {
				this.gear1 = gear2;
			} else {
				this.gear1 = gear1;
			}
		}
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
	
	public boolean canBeRestored(GearState addedGear) {
		BlockCoord loc1, loc2;
		
		if(this.gear1 == null) {
			loc1 = addedGear.getCoord();
			loc2 = this.gear2.getCoord();
		} else {
			loc1 = this.gear1.getCoord();
			loc2 = addedGear.getCoord();
		}
		
		return loc1.getX() <= loc2.getX() && loc1.getY() <= loc2.getY() && loc1.getZ() <= loc2.getZ();
	}
	
	public boolean setRestored(GearState addedGear) {
		if(!canBeRestored(addedGear)) return false;
		
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

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof GearLink)) {
			return false;
		}

		GearLink object = (GearLink) other;
		
		return this.gear1.equals(object.gear1);
	}
	
	@Override
	public int hashCode() {
		return this.gear1.hashCode();
	}
	
	@Override
	public String toString() {
		return this.gear1.toString();
	}
}