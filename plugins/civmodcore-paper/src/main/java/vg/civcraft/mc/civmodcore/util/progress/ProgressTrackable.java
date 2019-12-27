package vg.civcraft.mc.civmodcore.util.progress;

public interface ProgressTrackable extends Comparable<ProgressTrackable> {

	long getNextUpdate();

	void updateInternalProgressTime(long update);

	void updateState();

}
