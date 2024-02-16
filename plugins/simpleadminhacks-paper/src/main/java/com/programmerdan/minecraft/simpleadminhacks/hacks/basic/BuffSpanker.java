package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import static org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import static org.bukkit.event.entity.EntityPotionEffectEvent.Cause;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.DataParser;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class BuffSpanker extends BasicHack {

	@AutoLoad(id = "naughty", processor = DataParser.POTION_EFFECT_TYPE)
	private List<PotionEffectType> naughtyEffects;

	public BuffSpanker(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		if (this.naughtyEffects == null) {
			this.naughtyEffects = new ArrayList<>();
		}
	}

	@Override
	public void onDisable() {
		this.naughtyEffects.clear();
		super.onDisable();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void whenPotionEffectsChange(final EntityPotionEffectEvent event) {
		final PotionEffect effect = event.getNewEffect();
		if (effect == null || event.getAction() != Action.ADDED || event.getCause() == Cause.PLUGIN) {
			return;
		}
		if (this.naughtyEffects.contains(effect.getType())) {
			plugin().debug("Prevented [" + effect.getType().getName() + "] on: " + event.getEntity().getName());
			event.setCancelled(true);
			//return;
		}
	}

}
