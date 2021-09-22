package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.util.ConfigParsing;

public class AntiStatisChamber extends BasicHack {

	@AutoLoad
	private String pearlLifetime;
	private NamespacedKey key;

	public AntiStatisChamber(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
		this.key = new NamespacedKey(plugin, "pearl_thrown_time");
	}

	@EventHandler
	public void onPlayerThrowPearl(PlayerLaunchProjectileEvent event) {
		Projectile projectile = event.getProjectile();
		if (!(projectile instanceof EnderPearl)) {
			return;
		}
		EnderPearl pearl = (EnderPearl) projectile;
		PersistentDataContainer pdc = pearl.getPersistentDataContainer();
		pdc.set(key, PersistentDataType.LONG, System.currentTimeMillis());
	}

	@EventHandler
	public void onPearlLand(ProjectileHitEvent event) {
		Projectile projectile = event.getEntity();
		if (!(projectile instanceof EnderPearl)) {
			return;
		}
		EnderPearl pearl = (EnderPearl) projectile;
		PersistentDataContainer pdc = pearl.getPersistentDataContainer();
		Long pearlThrownTime = pdc.get(key, PersistentDataType.LONG);
		if (pearlThrownTime == null) {
			pearl.remove();
			return;
		}
		if ((System.currentTimeMillis() - pearlThrownTime) > ConfigParsing.parseTime(pearlLifetime)) {
			//We remove the pearl here because cancelling the event doesn't prevent the pearl from actually landing
			pearl.remove();
			if (pearl.getShooter() instanceof Player) {
				((Player) pearl.getShooter()).sendMessage(Component.text("Your pearl failed to land for being in flight for more than " + pearlLifetime).color(NamedTextColor.RED));
			}
		}
	}
}
