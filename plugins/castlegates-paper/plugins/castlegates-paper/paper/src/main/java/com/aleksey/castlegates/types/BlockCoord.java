/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.types;

import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BlockCoord {
	private final UUID _world;
	private int _x;
	private int _y;
	private int _z;

	public BlockCoord(Block block) {
		_world = block.getWorld().getUID();
		_x = block.getX();
		_y = block.getY();
		_z = block.getZ();
	}

	public BlockCoord(UUID world, int x, int y, int z) {
		_world = world;
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	public BlockCoord clone() {
		return new BlockCoord(_world, _x, _y, _z);
	}

	public UUID getWorldUID() { return _world; }

	public int getX() { return _x; }

	public int getY() { return _y; }

	public int getZ() { return _z; }

	public void increment(BlockFace face) {
		_x += face.getModX();
		_y += face.getModY();
		_z += face.getModZ();
	}

	public BlockCoord getForward() { return new BlockCoord(_world, _x, _y, _z - 1); }
	public BlockCoord getBackward() { return new BlockCoord(_world, _x, _y, _z + 1); }
	public BlockCoord getRight() { return new BlockCoord(_world, _x - 1, _y, _z); }
	public BlockCoord getLeft() { return new BlockCoord(_world, _x + 1, _y, _z); }
	public BlockCoord getTop() { return new BlockCoord(_world, _x, _y + 1, _z); }
	public BlockCoord getBottom() { return new BlockCoord(_world, _x, _y - 1, _z); }

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof BlockCoord object))
			return false;

		return _world.equals(object._world)
				&& _x == object._x
				&& _y == object._y
				&& _z == object._z
				;
	}

	@Override
	public int hashCode() {
		return _world.hashCode() ^ _x ^ _y ^ _z;
	}

	@Override
	public String toString() {
		return "World UUID = " + _world + ", [" + _x + " " + _y + " " + _z + "]";
	}
}
