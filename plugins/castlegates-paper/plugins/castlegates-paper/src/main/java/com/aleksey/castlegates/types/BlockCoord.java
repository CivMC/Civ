/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.types;

import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BlockCoord {
	private UUID world;
	private int x;
	private int y;
	private int z;

	public BlockCoord(Block block) {
		this.world = block.getWorld().getUID();
		this.x = block.getX();
		this.y = block.getY();
		this.z = block.getZ();
	}

	public BlockCoord(UUID world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public UUID getWorldUID() { return this.world; }

	public int getX() { return this.x; }

	public int getY() { return this.y; }

	public int getZ() { return this.z; }

	public void increment(BlockFace face) {
		this.x += face.getModX();
		this.y += face.getModY();
		this.z += face.getModZ();
	}

	public BlockCoord getForward() { return new BlockCoord(this.world, this.x, this.y, this.z - 1); }
	public BlockCoord getBackward() { return new BlockCoord(this.world, this.x, this.y, this.z + 1); }
	public BlockCoord getRight() { return new BlockCoord(this.world, this.x - 1, this.y, this.z); }
	public BlockCoord getLeft() { return new BlockCoord(this.world, this.x + 1, this.y, this.z); }
	public BlockCoord getTop() { return new BlockCoord(this.world, this.x, this.y + 1, this.z); }
	public BlockCoord getBottom() { return new BlockCoord(this.world, this.x, this.y - 1, this.z); }

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof BlockCoord)) {
			return false;
		}

		BlockCoord object = (BlockCoord)other;

		return this.world.equals(object.world)
				&& this.x == object.x
				&& this.y == object.y
				&& this.z == object.z
				;
	}

	@Override
	public int hashCode() {
		return this.world.hashCode() ^ this.x ^ this.y ^ this.z;
	}

	@Override
	public String toString() {
		return "World UUID = " + this.world + ", [" + this.x + " " + this.y + " " + this.z + "]";
	}
}
