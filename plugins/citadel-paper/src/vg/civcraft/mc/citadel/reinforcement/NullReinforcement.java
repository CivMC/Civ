package vg.civcraft.mc.citadel.reinforcement;

import org.bukkit.Location;

/**
 * Just a place holder for null reinforcements for the cache.  No one Should be using this.
 */
public class NullReinforcement extends Reinforcement{

	public NullReinforcement(Location loc) {
		super(loc, null, 0, 0, 0);
	}
}
