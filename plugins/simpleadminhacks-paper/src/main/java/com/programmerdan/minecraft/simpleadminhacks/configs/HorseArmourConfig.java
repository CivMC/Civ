package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class HorseArmourConfig extends SimpleHackConfig {
	@ApiStatus.Internal
	public HorseArmourConfig(
		final @NotNull SimpleAdminHacks plugin,
		final @NotNull ConfigurationSection base
	) {
		super(plugin, base);
	}

	@Override
	protected void wireup(
		final @NotNull ConfigurationSection config
	) { }

	public boolean shouldRegisterCraftingRecipes() {
		return getBase().getBoolean("registerRecipes", true);
	}

	public boolean implementCustomDurability() {
		return getBase().getBoolean("customDurability", true);
	}

	public boolean implementCustomUnbreaking() {
		return getBase().getBoolean("customUnbreaking", true);
	}
}
