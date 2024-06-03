package com.github.igotyou.FactoryMod.compaction;

import java.util.Map;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

/**
 * Bit of a janky workaround to mark {@link ItemMap} items as compacted, since ItemMap lives in CivModCore.
 */
public final class CompactedConfigItemModifier extends ItemMap.ConfigItemModifier {
	@Override
	public void modifyItem(
			final @NotNull ItemMeta meta
	) {
		Compaction.markAsCompacted(meta);
	}

	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of();
	}

	public static @NotNull CompactedConfigItemModifier deserialize(
			final @NotNull Map<String, Object> data
	) {
		return new CompactedConfigItemModifier();
	}
}
