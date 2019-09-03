package vg.civcraft.mc.civmodcore.locations.chunkmeta;

public enum CacheState {

	NEW, NORMAL, MODIFIED, DELETED;

	public CacheState progress(CacheState next) {
		if (next == MODIFIED && this == NEW) {
			return NEW;
		}
		if (this == DELETED) {
			return DELETED;
		}
		return next;
	}

}
