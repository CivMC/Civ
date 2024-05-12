/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.types;

import java.util.List;

public class GearblockLink {
	private int _id;
	private Gearblock _gearblock1;
	private Gearblock _gearblock2;
	private boolean _removed;
	private List<BlockState> _blocks;

	public GearblockLink(Gearblock gearblock1, Gearblock gearblock2) {
		_gearblock1 = gearblock1;
		_gearblock2 = gearblock2;
	}

	public int getId() { return _id; }
	public void setId(int value) { _id = value; }

	public Gearblock getGearblock1() { return _gearblock1; }

	public Gearblock getGearblock2() { return _gearblock2; }

	public boolean isBroken() { return _gearblock1 == null || _gearblock2 == null; }

	public void setBroken(Gearblock removedGear) {
		if(_gearblock1 == removedGear) {
			_gearblock1 = null;
		} else {
			_gearblock2 = null;
		}
	}

	public void setRestored(Gearblock addedGearblock) {
		if(_gearblock1 == null) {
			_gearblock1 = addedGearblock;
		} else {
			_gearblock2 = addedGearblock;
		}
	}

	public boolean isRemoved() { return _removed; }
	public void setRemoved() { _removed = true; }

	public boolean isDrawn() { return _blocks != null; }
	public List<BlockState> getBlocks() { return _blocks; }
	public void setBlocks(List<BlockState> blocks) { _blocks = blocks; }
}
