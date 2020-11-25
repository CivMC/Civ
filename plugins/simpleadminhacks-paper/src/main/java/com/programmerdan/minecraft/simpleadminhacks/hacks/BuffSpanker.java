package com.programmerdan.minecraft.simpleadminhacks.hacks;

import static org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import static org.bukkit.event.entity.EntityPotionEffectEvent.Cause;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.BuffSpankerConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BuffSpanker extends SimpleHack<BuffSpankerConfig> implements Listener {

	public BuffSpanker(SimpleAdminHacks plugin, BuffSpankerConfig config) {
		super(plugin, config);
	}

	@Override
	public String status() {
		StringBuilder builder = new StringBuilder(BuffSpanker.class.getSimpleName());
		builder.append(" is ").append(isEnabled() ? "enabled" : "disabled").append(".").append("\n");
		Set<PotionEffectType> naughtyList = this.config.getNaughtyList();
		if (naughtyList.isEmpty()) {
			builder.append("No naughty effects.");
		}
		else {
			builder.append(" Naughty effects:\n");
			builder.append(naughtyList.stream()
					.map(effect -> "  • " + effect.getName())
					.collect(Collectors.joining("\n")));
		}
		return builder.toString();
	}

	// ------------------------------------------------------------
	// Listeners
	// ------------------------------------------------------------

	@Override
	public void registerListeners() {
		this.plugin().registerListener(this);
	}

	@Override
	public void unregisterListeners() {
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void whenPotionEffectsChange(EntityPotionEffectEvent event) {
		PotionEffect effect = event.getNewEffect();
		if (effect == null || event.getAction() != Action.ADDED || event.getCause() == Cause.PLUGIN) {
			return;
		}
		if (this.config.isNaughtyBuff(effect.getType())) {
			plugin().debug("Prevented [" + effect.getType().getName() + "] on: " + event.getEntity().getName());
			event.setCancelled(true);
			//return;
		}
	}

	// ------------------------------------------------------------
	// Commands
	// ------------------------------------------------------------

	@Override
	public void registerCommands() { }

	@Override
	public void unregisterCommands() { }

	// ------------------------------------------------------------
	// Setup
	// ------------------------------------------------------------

	@Override
	public void dataBootstrap() { }

	@Override
	public void dataCleanup() { }

	public static BuffSpankerConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BuffSpankerConfig(plugin, config);
	}

}
