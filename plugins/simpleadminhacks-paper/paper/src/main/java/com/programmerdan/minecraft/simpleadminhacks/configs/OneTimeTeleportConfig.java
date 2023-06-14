package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

public final class OneTimeTeleportConfig extends SimpleHackConfig {

	private final CivLogger LOGGER = CivLogger.getLogger(OneTimeTeleportConfig.class);

	private List<Material> materialBlacklist;
	private List<Material> unsafeMaterials;
	private long timeLimitOnUsage;

	public OneTimeTeleportConfig(
			final @NotNull SimpleAdminHacks plugin,
			final @NotNull ConfigurationSection config
	) {
		super(plugin, config);
	}

	@Override
	protected void wireup(final ConfigurationSection config) {
		// Parse blacklisted item materials
		this.materialBlacklist = config.getStringList("material_blacklist")
				.stream()
				.map((raw) -> {
					final Material material = MaterialUtils.getMaterial(raw);
					if (!ItemUtils.isValidItemMaterial(material)) {
						LOGGER.warning("Blacklisted material [" + raw + "] is not a valid item material!");
						return null;
					}
					return material;
				})
				.filter(Objects::nonNull)
				.toList();
		// Parse unsafe blocks
		this.unsafeMaterials = config.getStringList("unsafe_materials")
				.stream()
				.map((raw) -> {
					final Material material = MaterialUtils.getMaterial(raw);
					if (material == null || !material.isBlock()) {
						LOGGER.warning("Unsafe material [" + raw + "] is not a valid block material!");
						return null;
					}
					return material;
				})
				.filter(Objects::nonNull)
				.toList();
		// Parse maximum time limit to use OTT
		this.timeLimitOnUsage = ConfigHelper.parseTime(config.getString("ott_timeout", "2d"));
	}

	public @NotNull List<Material> getMaterialBlacklist() {
		return Collections.unmodifiableList(Objects.requireNonNullElseGet(this.materialBlacklist, List::of));
	}

	public @NotNull List<Material> getUnsafeMaterials() {
		return Collections.unmodifiableList(Objects.requireNonNullElseGet(this.unsafeMaterials, List::of));
	}

	public long getTimeLimitOnUsageInMillis() {
		return this.timeLimitOnUsage;
	}
}
