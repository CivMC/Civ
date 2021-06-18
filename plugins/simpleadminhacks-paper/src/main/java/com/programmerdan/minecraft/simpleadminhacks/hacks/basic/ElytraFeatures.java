package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleGlideEvent;

public class ElytraFeatures extends BasicHack {

	@AutoLoad
	private boolean disableFlight;

	@AutoLoad
	private boolean disableFireworkBoosting;

	@AutoLoad
	private boolean disableFlightInCombat;

	public ElytraFeatures(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
	}

	private boolean hasBypassPermission(final Entity entity) {
		if (!(entity instanceof Player)) {
			return false;
		}
		return entity.hasPermission("simpleadmin.elytrabypass");
	}

	private boolean isInCombat(final Entity entity) {
		if (!(entity instanceof Player)) {
			return false;
		}
		final var pluginManager = Bukkit.getPluginManager();
		final var foundPlugin = pluginManager.getPlugin("CombatTagPlus");
		if (pluginManager.isPluginEnabled(foundPlugin)) {
			final var combatTagPlugin = (CombatTagPlus) foundPlugin;
			return combatTagPlugin.getTagManager().isTagged(entity.getUniqueId());
		}
		return false;
	}

	@EventHandler(ignoreCancelled = true)
	public void disableElytraFlight(final EntityToggleGlideEvent event) {
		if (this.disableFlight || (this.disableFlightInCombat && isInCombat(event.getEntity()))) {
			if (!hasBypassPermission(event.getEntity())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void disableElytraFireworkBoosting(final PlayerElytraBoostEvent event) {
		if (this.disableFireworkBoosting && !hasBypassPermission(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

}
