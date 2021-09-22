package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;

public class AntiStatisChamber extends BasicHack {

	@AutoLoad
	private double maxPearlDistance;

	public AntiStatisChamber(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@EventHandler
	public void onPlayerPearl(PlayerTeleportEvent event) {
		if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
			return;
		}
		Location from = event.getFrom();
		Location to = event.getTo();
		double distance = from.distance(to);
		if (distance > maxPearlDistance) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(Component.text("Your pearl was cancelled since it exceeded the maximum teleporting distance of " + maxPearlDistance + "!").color(
					NamedTextColor.RED));
		}
	}
}
