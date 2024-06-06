package com.github.igotyou.FactoryMod.compaction;

import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

/**
 * <p>Bit of a janky workaround to mark {@link ItemMap} items as compacted, since ItemMap lives in CivModCore.</p>
 *
 * <pre><code>
 * glass_bottles:
 *   material: GLASS_BOTTLE
 *   amount: 128
 *   modifiers:
 *     - ==: com.github.igotyou.FactoryMod.compaction.CompactedConfigItemModifier
 * </code></pre>
 *
 * <p>Please note that modifiers are applied in order of appearance. See
 * {@link ConfigHelper#parseItemMapDirectly(ConfigurationSection)} for more details</p>
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
