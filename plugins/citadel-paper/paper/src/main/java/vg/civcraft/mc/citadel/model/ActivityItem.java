package vg.civcraft.mc.citadel.model;

import java.time.Instant;

public class ActivityItem {
	public final short world;
	public final int group;
	public final int x;
	public final int z;
	public final Instant activity;
	public final int resolution;

	public ActivityItem(short world, int group, int x, int z, Instant activity, int resolution) {
		this.world = world;
		this.group = group;
		this.x = x;
		this.z = z;
		this.activity = activity;
		this.resolution = resolution;
	}
}
