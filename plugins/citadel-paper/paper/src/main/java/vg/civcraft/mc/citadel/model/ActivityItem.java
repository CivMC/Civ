package vg.civcraft.mc.citadel.model;

import java.time.Instant;

public record ActivityItem (
		short world,
		int group,
		int x,
		int z,
		Instant activity,
		int resolution)
{
}
