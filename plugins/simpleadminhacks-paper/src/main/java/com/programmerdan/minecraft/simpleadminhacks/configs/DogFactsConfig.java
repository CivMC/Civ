package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;

public final class DogFactsConfig extends SimpleHackConfig {
	public DogFactsConfig(
			final @NotNull SimpleAdminHacks plugin,
			final @NotNull ConfigurationSection base
	) {
		super(plugin, base);
	}

	@Override
	protected void wireup(
			final @NotNull ConfigurationSection config
	) {

	}

	public @NotNull List<Component> getAnnouncements() {
		return getBase()
				.getStringList("announcements")
				.stream()
				.map(MiniMessage.miniMessage()::deserialize)
				.toList();
	}

	public long getIntervalInTicks() {
		return ConfigHelper.parseTimeAsTicks(Objects.requireNonNullElse(
				getBase().getString("intervalTime"), "30m"
		));
	}
}
