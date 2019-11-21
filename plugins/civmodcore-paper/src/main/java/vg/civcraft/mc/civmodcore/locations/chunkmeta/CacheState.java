package vg.civcraft.mc.civmodcore.locations.chunkmeta;

public enum CacheState {

	NEW, NORMAL, MODIFIED, DELETED;

	public CacheState progress(CacheState next) {
		if (this == DELETED) {
			return this;
		}
		if (this == NEW) {
			if (next == MODIFIED) {
				return NEW;
			}
			if (next == DELETED) {
				//if the data was new and deleted before being persisted, we don't
				//need to do anything
				return NORMAL;
			}
		}
		return next;
	}

}
