package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.DogFactsConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;

public final class DogFacts extends SimpleHack<DogFactsConfig> {
	private final BooleanSetting disableAnnouncementsSetting;

	private BukkitTask task;
	private int counter;

	public DogFacts(
			final @NotNull SimpleAdminHacks plugin,
			final @NotNull DogFactsConfig config
	) {
		super(plugin, config);
		PlayerSettingAPI.registerSetting(this.disableAnnouncementsSetting = new BooleanSetting(plugin,
				// Default Value
				false,
				// Display Name
				"Disable Announcements",
				// Slug
				"disableAnnouncements",
				// Description
				"Disable Announcements?"
		), plugin.getSettingManager().getMainMenu());
		this.counter = 0;
	}

	public static @NotNull DogFactsConfig generate(
			final @NotNull SimpleAdminHacks plugin,
			final @NotNull ConfigurationSection config
	) {
		return new DogFactsConfig(plugin, config);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		final List<Component> announcements = List.copyOf(config().getAnnouncements());
		if (!announcements.isEmpty()) {
			final long intervalInTicks = config().getIntervalInTicks();
			this.task = Bukkit.getScheduler().runTaskTimer(plugin(), () -> {
				if (this.counter >= announcements.size()) {
					this.counter = 0;
				}
				final Component announcement = announcements.get(this.counter);
				plugin().info("Broadcasting DogFact #" + PlainTextComponentSerializer.plainText().serialize(announcement));
				for (final Player recipient : Bukkit.getOnlinePlayers()) {
					if (!this.disableAnnouncementsSetting.getValue(recipient.getUniqueId())) {
						recipient.sendMessage(announcement);
					}
				}
				this.counter++;
			}, (int) (Math.random() * intervalInTicks), intervalInTicks);
		}
	}

	@Override
	public void onDisable() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
		super.onDisable();
	}
}
