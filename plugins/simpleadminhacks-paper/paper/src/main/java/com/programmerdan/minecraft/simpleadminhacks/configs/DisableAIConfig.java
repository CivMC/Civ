package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

public final class DisableAIConfig extends SimpleHackConfig {

	private final CivLogger logger;
	private final EnumMap<EntityType, List<CreatureSpawnEvent.SpawnReason>> mobAiLimitations;

	public DisableAIConfig(final SimpleAdminHacks plugin, final ConfigurationSection base) {
		super(plugin, base, false);
		this.logger = CivLogger.getLogger(getClass());
		this.mobAiLimitations = new EnumMap<>(EntityType.class);
		wireup(base);
	}

	@Override
	protected void wireup(final ConfigurationSection config) {
		for (final EntityType entityType : EntityType.values()) {
			if (!config.contains(entityType.name())) {
				continue;
			}
			if (!entityType.isAlive()) {
				this.logger.warning("EntityType [" + entityType.name() + "] is not alive.");
				continue;
			}
			final List<String> rawSpecifiedReasons = ConfigHelper.getStringList(config, entityType.name());
			rawSpecifiedReasons.removeIf(StringUtils::isBlank);
			if (CollectionUtils.isEmpty(rawSpecifiedReasons)) {
				this.logger.warning("EntityType [" + entityType.name() + "] did not specify any spawn reasons; " +
						"skipping. You should fix your config.");
				continue;
			}
			final var allSpawnReasons = CreatureSpawnEvent.SpawnReason.values();
			final var limitationReasons = this.mobAiLimitations.computeIfAbsent(entityType,
					(ignored) -> new ArrayList<>(allSpawnReasons.length));
			if (rawSpecifiedReasons.size() == 1 && "ALL".equalsIgnoreCase(rawSpecifiedReasons.get(0))) {
				limitationReasons.addAll(List.of(allSpawnReasons));
				continue;
			}
			for (final String rawSpecifiedReason : rawSpecifiedReasons) {
				final var matchedSpawnReason = EnumUtils.getEnumIgnoreCase(
						CreatureSpawnEvent.SpawnReason.class, rawSpecifiedReason);
				if (matchedSpawnReason == null) {
					this.logger.warning("EntityType [" + entityType.name() + "] specified an " +
							"invalid spawn reason: " + rawSpecifiedReason);
					continue;
				}
				if (limitationReasons.contains(matchedSpawnReason)) {
					this.logger.warning("EntityType [" + entityType.name() + "] already specifies " +
							"reason: " + rawSpecifiedReason);
					continue;
				}
				limitationReasons.add(matchedSpawnReason);
			}
		}
	}

	public void reset() {
		this.mobAiLimitations.forEach((type, reasons) -> reasons.clear());
		this.mobAiLimitations.clear();
	}

	public boolean isLimitingEntityAI(final EntityType entityType, final CreatureSpawnEvent.SpawnReason spawnReason) {
		if (entityType == null || spawnReason == null) {
			return false;
		}
		final var limitationReasons = this.mobAiLimitations.get(entityType);
		if (CollectionUtils.isEmpty(limitationReasons)) {
			return false;
		}
		return limitationReasons.contains(spawnReason);
	}

}
