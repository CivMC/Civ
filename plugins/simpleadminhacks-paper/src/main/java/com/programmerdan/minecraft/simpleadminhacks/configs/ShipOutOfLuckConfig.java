package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.google.common.base.Strings;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;

public class ShipOutOfLuckConfig extends SimpleHackConfig {

	private static final Set<Material> BOAT_BREAKERS = new HashSet<>();

	public ShipOutOfLuckConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		BOAT_BREAKERS.clear();
		plugin().info("Parsing scuttle materials:");
		for (String scuttle : config.getStringList("boatBreakers")) {
			if (Strings.isNullOrEmpty(scuttle)) {
				plugin().warning("\tScuttle entry was null or empty D:");
				continue;
			}
			Material found = MaterialAPI.getMaterial(scuttle);
			if (found == null) {
				plugin().warning("\tScuttle entry could not be matched D: [" + scuttle + "]");
				continue;
			}
			if (BOAT_BREAKERS.contains(found)) {
				plugin().warning("\tScuttle entry is duplicated: [" + scuttle + "]");
				continue;
			}
			BOAT_BREAKERS.add(found);
			plugin().info("\tScuttle recognised: " + found.name());
		}
		plugin().info("Parsed " + BOAT_BREAKERS.size() + " scuttles.");
	}

	public Set<Material> getBoatBreakers() {
		return Collections.unmodifiableSet(BOAT_BREAKERS);
	}

	public boolean isBoatBreaker(Material type) {
		return type != null && BOAT_BREAKERS.contains(type);
	}

}
