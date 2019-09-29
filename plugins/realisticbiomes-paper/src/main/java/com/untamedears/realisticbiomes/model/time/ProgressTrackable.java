package com.untamedears.realisticbiomes.model.time;

public interface ProgressTrackable extends Comparable<ProgressTrackable> {

	public long getNextUpdate();

	void updateInternalProgressTime(long update);

	void updateState();

}
