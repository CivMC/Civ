package com.github.igotyou.FactoryMod.utility;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author caucow
 */
public class IOSelector {

	private final EnumSet<Direction> inputs;
	private final EnumSet<Direction> outputs;
	private final EnumSet<Direction> fuel;

	public IOSelector(Collection<Direction> inMask, Collection<Direction> outMask, Collection<Direction> fuel) {
		this();
		this.inputs.addAll(inMask);
		this.outputs.addAll(outMask);
		this.fuel.addAll(fuel);
	}

	public IOSelector() {
		this.inputs = EnumSet.noneOf(Direction.class);
		this.outputs = EnumSet.noneOf(Direction.class);
		this.fuel = EnumSet.noneOf(Direction.class);
	}

	public IOState getState(Direction direction) {
		return IOState.fromIO(inputs.contains(direction), outputs.contains(direction), fuel.contains(direction));
	}

	public void setState(Direction direction, IOState state) {
		if (state.isIn()) {
			inputs.add(direction);
		} else {
			inputs.remove(direction);
		}
		if (state.isOut()) {
			outputs.add(direction);
		} else {
			outputs.remove(direction);
		}
		if (state.isFuel()) {
			fuel.add(direction);
		} else {
			fuel.remove(direction);
		}
	}

	public int getInputCount() {
		return inputs.size();
	}

	public int getOutputCount() {
		return outputs.size();
	}

	public int getFuelCount() {
		return fuel.size();
	}

	public int getTotalIOFCount() {
		return getInputCount() + getOutputCount() + getFuelCount();
	}

	public boolean toggleInput(Direction direction) {
		boolean added = inputs.add(direction);
		if (!added) {
			inputs.remove(direction);
		}
		return added;
	}

	public boolean isInput(Direction direction) {
		return inputs.contains(direction);
	}

	public boolean toggleOutput(Direction direction) {
		boolean added = outputs.add(direction);
		if (!added) {
			outputs.remove(direction);
		}
		return added;
	}

	public boolean isOutput(Direction direction) {
		return outputs.contains(direction);
	}

	public boolean toggleFuel(Direction direction) {
		boolean added = fuel.add(direction);
		if (!added) {
			fuel.remove(direction);
		}
		return added;
	}

	public boolean isFuel(Direction direction) {
		return fuel.contains(direction);
	}

	public boolean hasInputs() {
		return !inputs.isEmpty();
	}

	public List<BlockFace> getInputs(BlockFace front) {
		Direction[] values = Direction.values();
		List<BlockFace> bfList = new ArrayList<>(values.length);
		for (Direction dir : values) {
			if (inputs.contains(dir)) {
				bfList.add(dir.getBlockFacing(front));
			}
		}
		return bfList;
	}

	public boolean hasOutputs() {
		return !outputs.isEmpty();
	}

	public List<BlockFace> getOutputs(BlockFace front) {
		Direction[] values = Direction.values();
		List<BlockFace> bfList = new ArrayList<>(values.length);
		for (Direction dir : values) {
			if (outputs.contains(dir)) {
				bfList.add(dir.getBlockFacing(front));
			}
		}
		return bfList;
	}

	public boolean hasFuel() {
		return !fuel.isEmpty();
	}

	public List<BlockFace> getFuel(BlockFace front) {
		Direction[] values = Direction.values();
		List<BlockFace> bfList = new ArrayList<>(values.length);
		for (Direction dir : values) {
			if (fuel.contains(dir)) {
				bfList.add(dir.getBlockFacing(front));
			}
		}
		return bfList;
	}

	public IOState cycleDirection(Direction direction, boolean backwards) {
		IOState cur = getState(direction);
		if (backwards) {
			cur = cur.last();
		} else {
			cur = cur.next();
		}
		setState(direction, cur);
		return cur;
	}

	public ConfigurationSection toConfigSection() {
		ConfigurationSection sec = new YamlConfiguration();
		sec.set("in", inputs.stream().map(Enum::name).collect(Collectors.toList()));
		sec.set("out", outputs.stream().map(Enum::name).collect(Collectors.toList()));
		sec.set("fuel", fuel.stream().map(Enum::name).collect(Collectors.toList()));
		return sec;
	}

	public static IOSelector fromConfigSection(ConfigurationSection conf) {
		List<Direction> inList = conf.getStringList("in").stream().map(Direction::valueOf).collect(Collectors.toList());
		List<Direction> outList = conf.getStringList("out").stream().map(Direction::valueOf).collect(Collectors.toList());
		List<Direction> fuelList = conf.getStringList("fuel").stream().map(Direction::valueOf).collect(Collectors.toList());
		return new IOSelector(inList, outList, fuelList);
	}

	public enum IOState {
		IGNORED("Ignored", new ItemStack(Material.GRAY_WOOL), 0x808080),
		INPUT("Input", new ItemStack(Material.BLUE_WOOL), 0x4040FF),
		OUTPUT("Output", new ItemStack(Material.RED_WOOL), 0xFF4040),
		BOTH("Input+Output", new ItemStack(Material.PURPLE_WOOL), 0xFF40FF),
		FUEL("Fuel", new ItemStack(Material.LIGHT_GRAY_WOOL), 0xA0A0A0),
		INPUT_FUEL("Input+Fuel", new ItemStack(Material.CYAN_WOOL), 0x8060FF),
		OUTPUT_FUEL("Output+Fuel", new ItemStack(Material.PINK_WOOL), 0xFF6080),
		BOTH_FUEL("Input+Output+Fuel", new ItemStack(Material.MAGENTA_WOOL), 0xFF60FF);

		public final String displayName;
		private final ItemStack uiVisual;
		public final int color;

		IOState(String displayName, ItemStack uiVisual, int color) {
			this.displayName = displayName;
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
