package vg.civcraft.mc.civmodcore.utilities.progress;

public interface ProgressTrackable extends Comparable<ProgressTrackable> {

	long getNextUpdate();

	void updateInternalProgressTime(long update);

	void updateState();

}
