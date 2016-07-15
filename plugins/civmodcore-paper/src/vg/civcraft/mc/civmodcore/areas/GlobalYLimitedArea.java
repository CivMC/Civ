package vg.civcraft.mc.civmodcore.areas;

import java.util.Collection;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

public class GlobalYLimitedArea extends AbstractYLimitedArea {

    private World world;

    public GlobalYLimitedArea(double lowerYBound, double upperYBound,
	    Collection<Biome> allowedBiomes, World world) {
	super(lowerYBound, upperYBound, allowedBiomes);
	this.world = world;
    }

    /**
     * @return Null, because this area includes an infinite amount of chunks
     */
    @Override
    public Collection<Chunk> getChunks() {
	return null;
    }

    @Override
    public Location getCenter() {
	return new Location(world, 0, 0, 0);
    }

    @Override
    public World getWorld() {
	return world;
    }

    @Override
    public boolean isInArea(Location loc) {
	return super.isInArea(loc)
		&& loc.getWorld().getUID().equals(world.getUID());
    }

}
