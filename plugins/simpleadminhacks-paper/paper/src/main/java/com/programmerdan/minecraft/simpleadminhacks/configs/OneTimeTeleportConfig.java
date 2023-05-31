package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;

public class OneTimeTeleportConfig extends SimpleHackConfig {

	//private List<String> itemBlacklistString;
	private List<Material> materialBlacklist;
	private List<Material> unsafeMaterials;
	private long timelimitOnUsage;

	public OneTimeTeleportConfig(SimpleAdminHacks plugin,
								 ConfigurationSection base) {super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		List<String> itemBlacklistString = config.getStringList("material_blacklist");
		if (itemBlacklistString.isEmpty()) {
			plugin().getLogger().warning("material_blacklist was empty? is this an error?");
		}
		this.materialBlacklist = new ArrayList<>();
		for (String s : itemBlacklistString) {
			Material material = Material.matchMaterial(s);
			if (material == null) {
				plugin().getLogger().warning("Material " + s + " in item black list for OTT couldn't be matched, skipping but is this a typo?");
				continue;
			}
			materialBlacklist.add(material);
		}


		this.unsafeMaterials = new ArrayList<>();
		List<String> unsafeMaterialsString = config.getStringList("unsafe_materials");
		for (String s : unsafeMaterialsString) {
			Material material = Material.matchMaterial(s);
			if (material == null) {
				plugin().getLogger().warning("Material " + s + " in unsafe materials list for OTT couldn't be matched, skipping but is this a typo?");
				continue;
			}
			materialBlacklist.add(material);
		}
		this.timelimitOnUsage = ConfigHelper.parseTime(config.getString("ott_timeout", "2d"));
	}

	public List<Material> getMaterialBlacklist() {
		return Collections.unmodifiableList(materialBlacklist);
	}

	public List<Material> getUnsafeMaterials() {
		return Collections.unmodifiableList(unsafeMaterials);
	}

	public long getTimelimitOnUsageInMillis() {
		return timelimitOnUsage;
	}
}
