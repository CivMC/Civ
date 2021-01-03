package vg.civcraft.mc.civmodcore.world;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.v1_16_R3.IBlockState;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;

public final class BlockProperties {

	private static final Map<String, IBlockState<?>> BLOCK_STATES = new HashMap<>();

	static {
		for (final Field field : FieldUtils.getAllFields(net.minecraft.server.v1_16_R3.BlockProperties.class)) {
			if (!IBlockState.class.isAssignableFrom(field.getType())) {
				continue;
			}
			try {
				final IBlockState<?> state = (IBlockState<?>) FieldUtils.readField(field, (Object) null, true);
				BLOCK_STATES.put(state.getName(), state);
			}
			catch (final IllegalAccessException exception) {
				exception.printStackTrace();
			}
		}
		Bukkit.getLogger().warning("[BlockProperties] Available block states: " +
				String.join(",", BLOCK_STATES.keySet()) + ".");
	}

	public static boolean setBlockProperty(final Block block, final String key, final String value) {
		// we need this wrapper method to trick the java generics
		return innerSetBlockProperty(block, key, value);
	}

	private static <V extends Comparable<V>> boolean innerSetBlockProperty(final Block block,
																		   final String key,
																		   final String value) {
		@SuppressWarnings("unchecked")
		final IBlockState<V> state = (IBlockState<V>) BLOCK_STATES.get(key);
		if (state == null) {
			return false;
		}
		final Optional<V> opt = state.b(value);
		if (opt.isEmpty()) {
			return false;
		}
		final V valueToSet = opt.get();
		final CraftBlock craftBlock = (CraftBlock) block;
		final CraftWorld craftWorld = (CraftWorld) block.getWorld();
		// no idea what the last integer parameter does, I found 2 and 3 being used in NMS code and stuck to that
		craftWorld.getHandle().setTypeAndData(craftBlock.getPosition(), craftBlock.getNMS().set(state, valueToSet), 2);
		return true;
	}

}
