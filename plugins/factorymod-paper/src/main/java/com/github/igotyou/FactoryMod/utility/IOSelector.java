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
	private final DirectionMask fuel;

	public IOSelector(byte inMask, byte outMask, byte fuel) {
		this.inputs = new DirectionMask(inMask);
		this.outputs = new DirectionMask(outMask);
		this.fuel = new DirectionMask(fuel);
	}

	public IOSelector() {
		this((byte) 0, (byte) 0, (byte) 0);
	}

	public IOState getState(DirectionMask.Direction direction) {
		return IOState.fromIO(inputs.isSet(direction), outputs.isSet(direction), fuel.isSet(direction));
	}

	public void setState(DirectionMask.Direction direction, IOState state) {
		inputs.set(direction, state.isIn());
		outputs.set(direction, state.isOut());
		fuel.set(direction, state.isFuel());
	}

	public int getInputCount() {
		return Integer.bitCount(inputs.getMask());
	}

	public int getOutputCount() {
		return Integer.bitCount(outputs.getMask());
	}

	public int getFuelCount() {
		return Integer.bitCount(fuel.getMask());
	}

	public int getTotalIOFCount() {
		return getInputCount() + getOutputCount() + getFuelCount();
	}

	public boolean toggleInput(DirectionMask.Direction direction) {
		boolean newState = !inputs.isSet(direction);
		inputs.set(direction, newState);
		return newState;
	}

	public boolean isInput(DirectionMask.Direction direction) {
		return inputs.isSet(direction);
	}

	public boolean toggleOutput(DirectionMask.Direction direction) {
		boolean newState = !outputs.isSet(direction);
		outputs.set(direction, newState);
		return newState;
	}

	public boolean isOutput(DirectionMask.Direction direction) {
		return outputs.isSet(direction);
	}

	public boolean toggleFuel(DirectionMask.Direction direction) {
		boolean newState = !fuel.isSet(direction);
		fuel.set(direction, newState);
		return newState;
	}

	public boolean isFuel(DirectionMask.Direction direction) {
		return fuel.isSet(direction);
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

	public boolean hasFuel() {
		return fuel.getMask() != 0;
	}

	public List<BlockFace> getFuel(BlockFace front) {
		DirectionMask.Direction[] values = DirectionMask.Direction.values();
		List<BlockFace> bfList = new ArrayList<>(values.length);
		for (DirectionMask.Direction dir : values) {
			if (fuel.isSet(dir)) {
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
		fuel.set(direction, cur.isFuel());
		return cur;
	}

	/**
	 * @return a compact serialized form of the fuel/in/out bitmasks (high 8 bits unused, 8 bits fuel, 8 bits input, low
	 * 8 bits output)
	 */
	public int toIntMask() {
		return inputs.getMask() << 8 | outputs.getMask();
	}

	/**
	 * @param fioCombinedMask compact serialized form of the input, output, and fuel bitmasks (high 8 bits unused, 8
	 *                        bits fuel, 8 bits input, low 8 bits output)
	 * @return IOSelector with input, output, and fuel masks set
	 */
	public static IOSelector fromIntMask(int fioCombinedMask) {
		return new IOSelector(
				(byte) (fioCombinedMask >>> 8 & 0xFF),
				(byte) (fioCombinedMask & 0xFF),
				(byte) (fioCombinedMask >>> 16 & 0xFF));
	}

	public enum IOState {
		IGNORED(new ItemStack(Material.GRAY_WOOL), 0x808080),
		INPUT(new ItemStack(Material.BLUE_WOOL), 0x4040FF),
		OUTPUT(new ItemStack(Material.RED_WOOL), 0xFF4040),
		BOTH(new ItemStack(Material.PURPLE_WOOL), 0xFF40FF),
		FUEL(new ItemStack(Material.LIGHT_GRAY_WOOL), 0xA0A0A0),
		INPUT_FUEL(new ItemStack(Material.CYAN_WOOL), 0x8060FF),
		OUTPUT_FUEL(new ItemStack(Material.PINK_WOOL), 0xFF6080),
		BOTH_FUEL(new ItemStack(Material.MAGENTA_WOOL), 0xFF60FF);

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

		public boolean isFuel() {
			return (ordinal() & 4) != 0;
		}

		public IOState next() {
			IOState[] values = values();
			return values[(ordinal() + values.length + 1) % values.length];
		}

		public IOState last() {
			IOState[] values = values();
			return values[(ordinal() + values.length + values.length - 1) % values.length];
		}

		public static IOState fromIO(boolean in, boolean out, boolean fuel) {
			return IOState.values()[(in ? 1 : 0) | (out ? 2 : 0) | (fuel ? 4 : 0)];
		}
	}

}
