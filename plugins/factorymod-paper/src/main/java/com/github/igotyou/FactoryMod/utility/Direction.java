package com.github.igotyou.FactoryMod.utility;

import org.bukkit.block.BlockFace;

import java.util.function.Function;

/**
 * @author caucow
 */
public enum Direction {
	TOP(bf -> BlockFace.UP), // 1
	BOTTOM(bf -> BlockFace.DOWN), // 2
	LEFT(bf -> {
		switch (bf) {
			case NORTH:
				return BlockFace.EAST;
			case EAST:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.WEST;
			case WEST:
				return BlockFace.NORTH;
			default:
				return bf;
		}
	}), // 4
	RIGHT(bf -> {
		switch (bf) {
			case NORTH:
				return BlockFace.WEST;
			case EAST:
				return BlockFace.NORTH;
			case SOUTH:
				return BlockFace.EAST;
			case WEST:
				return BlockFace.SOUTH;
			default:
				return bf;
		}
	}), // 8
	FRONT(bf -> bf), // 16
	BACK(BlockFace::getOppositeFace); // 32

	private final Function<BlockFace, BlockFace> facingModifier;

	private Direction(Function<BlockFace, BlockFace> facingModifier) {
		this.facingModifier = facingModifier;
	}

	/**
	 * @param front direction a block (such as a furnace) is facing.
	 * @return BlockFace in this direction, relative to the player's perspective when the player and block are
	 * facing each other.
	 */
	public BlockFace getBlockFacing(BlockFace front) {
		return facingModifier.apply(front);
	}

	public static Direction getDirection(BlockFace front, BlockFace axis) {
		for (Direction dir : Direction.values()) {
			// if dir's transformation of front == axis
			if (dir.getBlockFacing(front) == axis) {
				return dir;
			}
		}
		throw new IllegalArgumentException("Direction can only be gotten from an axis-aligned BlockFace.");
	}

	private static BlockFace getBlockFaceFromDirection(int dirx, int dirz) {
		switch (dirz) {
			case 1:
				return BlockFace.SOUTH;
			case -1:
				return BlockFace.NORTH;
			case 0: {
				switch (dirx) {
					case 1:
						return BlockFace.EAST;
					case -1:
						return BlockFace.WEST;
				}
			}
		}
		throw new IllegalArgumentException("Not a horizontal ordinal: (" + dirx + "," + dirz + ")");
	}
}
