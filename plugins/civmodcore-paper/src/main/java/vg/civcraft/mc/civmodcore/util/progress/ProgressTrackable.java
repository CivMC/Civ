package vg.civcraft.mc.civmodcore.util.progress;

public interface ProgressTrackable extends Comparable<ProgressTrackable> {

	public long getNextUpdate();

	void updateInternalProgressTime(long update);

	void updateState();

}
