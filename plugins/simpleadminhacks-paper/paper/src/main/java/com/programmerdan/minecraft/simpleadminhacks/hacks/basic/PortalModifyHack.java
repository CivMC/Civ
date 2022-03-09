package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PortalModifyHack extends BasicHack {

	@AutoLoad
	private String targetWorld;

	//Player UUID -> World UUID they came from
	private Map<UUID, UUID> playerWorldTracker;

	private SimpleAdminHacks plugin;
	
	public PortalModifyHack(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
		this.plugin = plugin;
		this.playerWorldTracker = new HashMap<>();
	}

	//We want to go last incase any plugins want to cancel our attempt
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerEnterPortal(PlayerPortalEvent event) {
		Player player = event.getPlayer();
		if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
			return;
		}
		World world = Bukkit.getWorld(targetWorld);
		if (world == null) {
			return;
		}
		Location to = new Location(getTargetWorld(player), event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
		event.setTo(to);
		if (to.getWorld().getName().equals(targetWorld)) {
			System.out.println("Spawning portal");
			spawnExitPortal(to);

		}
		playerWorldTracker.putIfAbsent(player.getUniqueId(), player.getWorld().getUID());
	}

	private World getTargetWorld(Player player) {
		World world = Bukkit.getWorld(targetWorld);
		if (world == null) {
			return player.getWorld();
		}

		if (player.getWorld().equals(world)) {
			return Bukkit.getWorld(playerWorldTracker.computeIfAbsent(player.getUniqueId(), k -> Bukkit.getWorlds().get(0).getUID()));
		}
		return world;
	}

	private void spawnExitPortal(Location location) {
		if (location == null) {
			return;
		}
		//Taken from EndDragonFight#spawnExitPortal
		EndPodiumFeature podiumFeature = new EndPodiumFeature(true);
		ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
		podiumFeature.configured(FeatureConfiguration.NONE).place(level, level.getChunkSource().getGenerator(), new Random(), new BlockPos(location.getX(), location.getY() - 7, location.getZ()));
	}
}
