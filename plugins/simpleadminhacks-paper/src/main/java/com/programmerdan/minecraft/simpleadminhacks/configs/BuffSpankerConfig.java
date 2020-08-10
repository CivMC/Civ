package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.google.common.base.Strings;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

public class BuffSpankerConfig extends SimpleHackConfig {

	private static final Set<PotionEffectType> NAUGHTY_LIST = new HashSet<>();

	public BuffSpankerConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		NAUGHTY_LIST.clear();
		plugin().info("Parsing naughty buffs ;)");
		for (String naughty : config.getStringList("naughty")) {
			if (Strings.isNullOrEmpty(naughty)) {
				plugin().warning("\tNaughty buff entry was null or empty D:");
				continue;
			}
			PotionEffectType found = PotionEffectType.getByName(naughty);
			if (found == null) {
				plugin().warning("\tNaughty buff entry could not be matched D: [" + naughty + "]");
				continue;
			}
			if (NAUGHTY_LIST.contains(found)) {
				plugin().warning("\tNaughty buff entry is duplicated: [" + naughty + "]");
				continue;
			}
			NAUGHTY_LIST.add(found);
			plugin().info("\tNaughty buff recognised: " + found.getName());
		}
		plugin().info("Parsed " + NAUGHTY_LIST.size() + " buffs.");
	}

	public Set<PotionEffectType> getNaughtyList() {
		return Collections.unmodifiableSet(NAUGHTY_LIST);
	}

	public boolean isNaughtyBuff(PotionEffectType type) {
		return type != null && NAUGHTY_LIST.contains(type);
	}

}

