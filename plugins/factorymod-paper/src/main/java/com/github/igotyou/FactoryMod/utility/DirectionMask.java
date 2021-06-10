package com.github.igotyou.FactoryMod.utility;

import org.bukkit.block.BlockFace;

import java.util.function.Function;

/**
 * @author caucow
 */
public class DirectionMask {

	private byte mask;

	public DirectionMask(Direction... dirs) {
		for (Direction dir : dirs) {
			set(dir, true);
		}
	}

	public DirectionMask(byte mask) {
		this.mask = mask;
	}

	public void set(Direction dir, boolean set) {
		if (set) {
			mask |= dir.dmask;
		} else {
			mask &= ~dir.dmask;
		}
	}

	public boolean isSet(Direction dir) {
		return (mask & dir.dmask) != 0;
	}

	public byte getMask() {
		return mask;
	}

	public enum Direction {
		TOP((byte) 0b0000_0001, bf -> BlockFace.UP), // 1
		BOTTOM((byte) 0b0000_0010, bf -> BlockFace.DOWN), // 2
		LEFT((byte) 0b0000_0100, bf -> {
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
		RIGHT((byte) 0b0000_1000, bf -> {
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
		FRONT((byte) 0b0001_0000, bf -> bf), // 16
		BACK((byte) 0b0010_0000, BlockFace::getOppositeFace); // 32

		public final byte dmask;
		private final Function<BlockFace, BlockFace> facingModifier;

		private Direction(byte dmask, Function<BlockFace, BlockFace> facingModifier) {
			this.dmask = dmask;
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

}
