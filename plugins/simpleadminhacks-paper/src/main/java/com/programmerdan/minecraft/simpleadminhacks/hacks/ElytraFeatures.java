package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.ElytraFeaturesConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Comparator;
import javax.annotation.Nonnull;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class ElytraFeatures extends SimpleHack<ElytraFeaturesConfig> implements Listener {

	public ElytraFeatures(@Nonnull final  SimpleAdminHacks plugin,
						  @Nonnull final  ElytraFeaturesConfig config) {
		super(plugin, config);
	}

	public static ElytraFeaturesConfig generate(@Nonnull final SimpleAdminHacks plugin,
												@Nonnull final ConfigurationSection config) {
		return new ElytraFeaturesConfig(plugin, config);
	}

	@Override
	public void onEnable() {
		plugin().registerListener(this);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
	}

	private boolean INTERNAL_hasBypassPermission(final Entity entity) {
		return entity instanceof Player && entity.hasPermission("simpleadmin.elytrabypass");
	}

	private boolean INTERNAL_isInCombat(final Entity entity) {
		if (!(entity instanceof Player)) {
			return false;
		}
		final PluginManager pluginManager = Bukkit.getPluginManager();
		final Plugin foundPlugin = pluginManager.getPlugin("CombatTagPlus");
		if (pluginManager.isPluginEnabled(foundPlugin)) {
			final CombatTagPlus combatTagPlugin = (CombatTagPlus) foundPlugin;
			return combatTagPlugin.getTagManager().isTagged(entity.getUniqueId());
		}
		return false;
	}

	// ------------------------------------------------------------
	// Elytra flight
	// ------------------------------------------------------------

	@EventHandler(ignoreCancelled = true)
	public void regulateElytraFlight(@Nonnull final EntityToggleGlideEvent event) {
		boolean disableFlight = false;
		if (config().isFlightDisabled()) {
			disableFlight = true;
		}
		else {
			if (config().isFlightDisabledInCombat()) {
				disableFlight = INTERNAL_isInCombat(event.getEntity());
			}
		}
		if (disableFlight) {
			if (!INTERNAL_hasBypassPermission(event.getEntity())) {
				event.setCancelled(true);
			}
		}
	}

	// ------------------------------------------------------------
	// Elytra boosting
	// ------------------------------------------------------------

	@EventHandler(ignoreCancelled = true)
	public void regulateElytraBoosting(@Nonnull final PlayerElytraBoostEvent event) {
		boolean disableBoosting = false;
		if (config().isBoostingDisabled()) {
			disableBoosting = true;
		}
		else {
			if (config().isBoostingDisabledInCombat()) {
				if (INTERNAL_isInCombat(event.getPlayer())) {
					disableBoosting = true;
				}
			}
			if (config().isSafeBoostingDisabled()) {
				if (!event.getFirework().getFireworkMeta().hasEffects()) {
					disableBoosting = true;
				}
			}
		}
		if (disableBoosting) {
			if (!INTERNAL_hasBypassPermission(event.getPlayer())) {
				event.setCancelled(true);
			}
		}
	}

	// ------------------------------------------------------------
	// Elytra damage
	// ------------------------------------------------------------

	private static final class ElytraDamage {
		private long lastDamageTime;
		private int damageToDeal;
	}

	private final Object2ObjectMap<Player, ElytraDamage> damageElytraFlightTracker =
			new Object2ObjectAVLTreeMap<>(Comparator.comparing(Player::getUniqueId));

	@EventHandler(ignoreCancelled = true)
	public void damageFliersAboveWorldHeight(final PlayerMoveEvent event) {
		if (config().getHeightDamage() <= 0) {
			return;
		}
		final Player player = event.getPlayer();
		if (!player.isGliding() || INTERNAL_hasBypassPermission(player)) {
			return;
		}
		final Location fromLocation = event.getFrom();
		final int allowedHeight = fromLocation.getWorld().getMaxHeight() + config().getHeightBuffer();
		final int blocksAboveAllowedHeight = fromLocation.getBlockY() - allowedHeight;
		// If the player is within the allowed height, do nothing
		if (blocksAboveAllowedHeight <= 0) {
			return;
		}
		final ElytraDamage details = this.damageElytraFlightTracker.computeIfAbsent(
				player, (_key) -> new ElytraDamage());
		final int damageToDeal = !config().isHeightDamageScaling() ? config().getHeightDamage() :
				config().getHeightDamage() * blocksAboveAllowedHeight;
		if (details.damageToDeal < damageToDeal) {
			details.damageToDeal = damageToDeal;
		}
		final long now = System.currentTimeMillis();
		final long deltaInterval = now - details.lastDamageTime;
		// If it's not the right time to damage the player, do nothing
		if (deltaInterval < config().getHeightDamageInterval()) {
			return;
		}
		player.damage(details.damageToDeal);
		details.damageToDeal = 0;
		details.lastDamageTime = now;
	}

	@EventHandler
	public void resetDamageElytraFlightOnLogout(final PlayerQuitEvent event) {
		this.damageElytraFlightTracker.remove(event.getPlayer());
	}

}
