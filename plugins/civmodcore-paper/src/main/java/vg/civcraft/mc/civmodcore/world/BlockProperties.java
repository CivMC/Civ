package vg.civcraft.mc.civmodcore.world;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

public final class BlockProperties {

	private static final CivLogger LOGGER = CivLogger.getLogger(BlockProperties.class);
	private static final Map<String, Property<?>> BLOCK_STATES = new HashMap<>();

	static {
		for (final Field field : FieldUtils.getAllFields(net.minecraft.world.level.block.state.properties.BlockStateProperties.class)) {
			if (!Property.class.isAssignableFrom(field.getType())) {
				continue;
			}
			try {
				final Property<?> state = (Property<?>) FieldUtils.readField(field, (Object) null, true);
				BLOCK_STATES.put(state.getName(), state);
			}
			catch (final IllegalAccessException exception) {
				exception.printStackTrace();
			}
		}
		LOGGER.info("[BlockProperties] Available block states: " + String.join(",", BLOCK_STATES.keySet()) + ".");
	}

	public static boolean setBlockProperty(final Block block, final String key, final String value) {
		// we need this wrapper method to trick the java generics
		return innerSetBlockProperty(block, key, value);
	}

	private static <V extends Comparable<V>> boolean innerSetBlockProperty(final Block block,
																		   final String key,
																		   final String value) {
		@SuppressWarnings("unchecked")
		final Property<V> state = (Property<V>) BLOCK_STATES.get(key);
		if (state == null) {
			return false;
		}
		final Optional<V> opt = state.getValue(value);
		if (opt.isEmpty()) {
			return false;
		}
		final V valueToSet = opt.get();
		final CraftBlock craftBlock = (CraftBlock) block;
		final CraftWorld craftWorld = (CraftWorld) block.getWorld();
		// Deobf path: net.minecraft.world.level.Level.setBlock()
		craftWorld.getHandle().setBlock(craftBlock.getPosition(), craftBlock.getNMS().setValue(state, valueToSet),
				// This value is named "flag" and appears to be a set of bitwise indicators for block updates
				2); // 2 and 3 being used in NMS code and stuck to that
		return true;
	}

}
