package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import java.util.Hashtable;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;

public class UnblockingHackConfig extends SimpleHackConfig {

	private Map<Material, Material> ninePartsBlocks;
	private Map<Material, Material> fourPartsBlocks;
	private Map<Material, Material> nineItemsBlocks;
	private Map<Material, Material> fourItemsBlocks;

	public UnblockingHackConfig(final SimpleAdminHacks plugin, final ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(final ConfigurationSection config) {
		parseUnblockConfigs(this.ninePartsBlocks = new Hashtable<>(), config, "9block");
		parseUnblockConfigs(this.fourPartsBlocks = new Hashtable<>(), config, "4block");
		parseUnblockConfigs(this.nineItemsBlocks = new Hashtable<>(), config, "9items");
		parseUnblockConfigs(this.fourItemsBlocks = new Hashtable<>(), config, "4items");
	}

	private void parseUnblockConfigs(final Map<Material, Material> destination,
									 final ConfigurationSection origin,
									 final String type) {
		if (origin == null) {
			return;
		}
		final var config = origin.getConfigurationSection(type);
		if (config == null) {
			return;
		}
		config.getValues(false).forEach((key, value) -> {
			final var fromMaterial = MaterialUtils.getMaterial(key);
			if (!ItemUtils.isValidItemMaterial(fromMaterial)) {
				plugin().warning("[UnblockingHackConfig] Could not find a valid item material from: " + key);
				return;
			}
			final var toMaterial = MaterialUtils.getMaterial("" + value);
			if (!ItemUtils.isValidItemMaterial(toMaterial)) {
				plugin().warning("[UnblockingHackConfig] Could not find a valid item material from: " + value);
				return;
			}
			destination.put(fromMaterial, toMaterial);
			plugin().info("[UnblockingHackConfig] Added " + type + ": "
					+ fromMaterial.name() + " -> " + toMaterial.name());
		});
	}

	public Map<Material, Material> getNinePartsBlocks() {
		return this.ninePartsBlocks;
	}

	public Map<Material, Material> getFourPartsBlocks() {
		return this.fourPartsBlocks;
	}

	public Map<Material, Material> getNineItemsBlocks() {
		return this.nineItemsBlocks;
	}

	public Map<Material, Material> getFourItemsBlocks() {
		return this.fourItemsBlocks;
	}

}
