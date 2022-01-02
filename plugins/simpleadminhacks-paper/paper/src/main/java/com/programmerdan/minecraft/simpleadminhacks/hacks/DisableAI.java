package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.DisableAIConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_18_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public final class DisableAI extends SimpleHack<DisableAIConfig> implements Listener {

	private static final String HAS_BEEN_DISABLED_KEY = "SAH_AI_DISABLED";

	public DisableAI(final SimpleAdminHacks plugin, final DisableAIConfig config) {
		super(plugin, config);
	}

	public static DisableAIConfig generate(final SimpleAdminHacks plugin, final ConfigurationSection config) {
		return new DisableAIConfig(plugin, config);
	}

	@Override
	public void onEnable() {
		plugin().registerListener(this);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		config().reset();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void disableModAI(final EntityAddToWorldEvent event) {
		final var entity = event.getEntity();
		if (!(entity instanceof LivingEntity)) {
			return;
		}
		final var livingEntity = (LivingEntity) entity;
		final var nbt = new CompoundTag(((CraftPersistentDataContainer) entity.getPersistentDataContainer()).getRaw()){};
		if (!config().isLimitingEntityAI(entity.getType(), entity.getEntitySpawnReason())) {
			// If the entity was disabled before, re-enable it
			if (nbt.contains(HAS_BEEN_DISABLED_KEY)) {
				livingEntity.setAI(nbt.getBoolean(HAS_BEEN_DISABLED_KEY));
				nbt.remove(HAS_BEEN_DISABLED_KEY);
			}
			return;
		}
		// If the entity has already been disabled, leave alone
		if (nbt.contains(HAS_BEEN_DISABLED_KEY)) {
			return;
		}
		nbt.putBoolean(HAS_BEEN_DISABLED_KEY, livingEntity.hasAI());
		livingEntity.setAI(false);
	}

}
