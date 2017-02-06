/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.types;

import java.util.List;

public class GearblockLink {
	private int id;
	private Gearblock gearblock1;
	private Gearblock gearblock2;
	private boolean removed;
	private List<BlockState> blocks;

	public GearblockLink(Gearblock gearblock1, Gearblock gearblock2) {
		this.gearblock1 = gearblock1;
		this.gearblock2 = gearblock2;
	}

	public int getId() { return this.id; }
	public void setId(int value) { this.id = value; }

	public Gearblock getGearblock1() { return this.gearblock1; }

	public Gearblock getGearblock2() { return this.gearblock2; }

	public boolean isBroken() { return this.gearblock1 == null || this.gearblock2 == null; }

	public void setBroken(Gearblock removedGear) {
		if(this.gearblock1 == removedGear) {
			this.gearblock1 = null;
		} else {
			this.gearblock2 = null;
		}
	}

	public boolean setRestored(Gearblock addedGearblock) {
		if(this.gearblock1 == null) {
			gearblock1 = addedGearblock;
		} else {
			gearblock2 = addedGearblock;
		}

		return true;
	}

	public boolean isRemoved() { return this.removed; }
	public void setRemoved() { this.removed = true; }

	public boolean isDrawn() { return this.blocks != null; }
	public List<BlockState> getBlocks() { return this.blocks; }
	public void setBlocks(List<BlockState> blocks) { this.blocks = blocks; }
}
