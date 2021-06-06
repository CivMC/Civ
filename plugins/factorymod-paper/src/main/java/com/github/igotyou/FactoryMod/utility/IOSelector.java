package com.github.igotyou.FactoryMod.utility;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caucow
 */
public class IOSelector {

	private final DirectionMask inputs;
	private final DirectionMask outputs;

	public IOSelector(byte inMask, byte outMask) {
		this.inputs = new DirectionMask(inMask);
		this.outputs = new DirectionMask(outMask);
	}

	public IOSelector() {
		this((byte) 0, (byte) 0);
	}

	public IOState getState(DirectionMask.Direction direction) {
		return IOState.fromIO(inputs.isSet(direction), outputs.isSet(direction));
	}

	public boolean hasInputs() {
		return inputs.getMask() != 0;
	}

	public List<BlockFace> getInputs(BlockFace front) {
		DirectionMask.Direction[] values = DirectionMask.Direction.values();
		List<BlockFace> bfList = new ArrayList<>(values.length);
		for (DirectionMask.Direction dir : values) {
			if (inputs.isSet(dir)) {
				bfList.add(dir.getBlockFacing(front));
			}
		}
		return bfList;
	}

	public boolean hasOutputs() {
		return outputs.getMask() != 0;
	}

	public List<BlockFace> getOutputs(BlockFace front) {
		DirectionMask.Direction[] values = DirectionMask.Direction.values();
		List<BlockFace> bfList = new ArrayList<>(values.length);
		for (DirectionMask.Direction dir : values) {
			if (outputs.isSet(dir)) {
				bfList.add(dir.getBlockFacing(front));
			}
		}
		return bfList;
	}

	public IOState cycleDirection(DirectionMask.Direction direction, boolean backwards) {
		IOState cur = getState(direction);
		if (backwards) {
			cur = cur.last();
		} else {
			cur = cur.next();
		}
		inputs.set(direction, cur.isIn());
		outputs.set(direction, cur.isOut());
		return cur;
	}

	/**
	 * @return a compact serialized form of the input and output bitmasks (high 8 bits => in; low 8 bits => out)
	 */
	public short toShortMask() {
		return (short) ((short) inputs.getMask() << 8 | (short) outputs.getMask());
	}

	/**
	 * @param IShOrO compact serialized form of the input and output bitmasks (high 8 bits => in; low 8 bits => out)
	 * @return IOSelector with input and output masks set
	 */
	public static IOSelector fromShortMask(short IShOrO) {
		return new IOSelector((byte) (IShOrO >>> 8 & 0xFF), (byte) (IShOrO & 0xFF));
	}

	public enum IOState {
		IGNORED(new ItemStack(Material.WHITE_WOOL), 0x808080),
		INPUT(new ItemStack(Material.BLUE_WOOL), 0x4040FF),
		OUTPUT(new ItemStack(Material.RED_WOOL), 0xFF4040),
		BOTH(new ItemStack(Material.PURPLE_WOOL), 0xFF40FF);

		private final ItemStack uiVisual;
		public final int color;

		private IOState(ItemStack uiVisual, int color) {
			this.uiVisual = uiVisual;
			this.color = color;
		}

		public ItemStack getUIVisual() {
			return uiVisual.clone();
		}

		public boolean isIn() {
			return (ordinal() & 1) != 0;
		}

		public boolean isOut() {
			return (ordinal() & 2) != 0;
		}

		public IOState next() {
			IOState[] values = values();
			return values[(ordinal() + values.length + 1) % values.length];
		}

		public IOState last() {
			IOState[] values = values();
			return values[(ordinal() + values.length + values.length - 1) % values.length];
		}

		public static IOState fromIO(boolean in, boolean out) {
			return IOState.values()[(in ? 1 : 0) | (out ? 2 : 0)];
		}
	}

}
