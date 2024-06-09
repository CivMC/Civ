package com.github.igotyou.FactoryMod.compaction;

import com.comphenix.protocol.events.ListenerPriority;
import com.github.igotyou.FactoryMod.FactoryMod;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import uk.protonull.civ.protocollib.NetworkItemMetaTransformer;

public final class CompactedItemNetworkAdapter extends NetworkItemMetaTransformer {
	public CompactedItemNetworkAdapter(
			final @NotNull FactoryMod plugin
	) {
		super(
				plugin,
				ListenerPriority.NORMAL
		);
	}

	@Override
	protected boolean processItem(
			final @NotNull Material material,
			final int amount,
			final @NotNull ItemMeta meta,
			final @NotNull Player recipient
	) {
		if (Compaction.isCompacted(meta)) {
			Compaction.addCompactedLore(meta);
			return true;
		}
		return false;
	}
}
