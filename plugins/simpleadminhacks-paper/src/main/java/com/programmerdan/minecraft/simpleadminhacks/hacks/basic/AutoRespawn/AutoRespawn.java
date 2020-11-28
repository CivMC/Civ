package com.programmerdan.minecraft.simpleadminhacks.hacks.basic.AutoRespawn;

import com.google.common.base.Strings;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import vg.civcraft.mc.civmodcore.util.Iteration;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public final class AutoRespawn extends BasicHack {

	private final Map<Player, _RespawnTimer> respawnTimers = new HashMap<>();

	@AutoLoad
	private long respawnDelay;

	@AutoLoad
	private long loginRespawnDelay;

	@AutoLoad
	private List<String> respawnQuotes;
	
	public AutoRespawn(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
		this.respawnDelay = Math.max(0L, this.respawnDelay);
		this.loginRespawnDelay = Math.max(0L, this.loginRespawnDelay);
		if (this.respawnQuotes == null) {
			this.respawnQuotes = new ArrayList<>();
		}
	}

	@Override
	public void onDisable() {
		this.respawnTimers.forEach((player, timer) -> timer.stop());
		this.respawnTimers.clear();
		super.onDisable();
	}
	
	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		if (this.respawnDelay <= 0) {
			plugin().info("Player [" + player.getName() + "] died, respawning.");
			// This is necessary as respawning the player IMMEDIATELY means also not allowing the
			// death process to occur (such as dropping items) to occur prior to the repsawn.
			Bukkit.getScheduler().runTask(this.plugin, () -> autoRespawnPlayer(player));
		}
		else {
			plugin().info("Player [" + player.getName() + "] died, " +
					"setting respawn timer: " + this.respawnDelay);
			this.respawnTimers.computeIfPresent(player, (_player, timer) -> timer.stop());
			this.respawnTimers.put(player, new _RespawnTimer(this, player,
					this.respawnDelay, this::autoRespawnPlayer));
		}
	}

	@EventHandler
	public void onPlayerLogin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if (!player.isDead()) {
			return;
		}
		if (this.loginRespawnDelay <= 0) {
			plugin().info("Player [" + player.getName() + "] logged in while dead, respawning.");
			// This is necessary as respawning the player IMMEDIATELY means also not allowing the
			// death process to occur (such as dropping items) to occur prior to the repsawn.
			Bukkit.getScheduler().runTask(this.plugin, () -> autoRespawnPlayer(player));
		}
		else {
			plugin().info("Player [" + player.getName() + "] logged in while dead, " +
					"setting respawn timer: " + this.loginRespawnDelay);
			this.respawnTimers.computeIfPresent(player, (_player, timer) -> timer.stop());
			this.respawnTimers.put(player, new _RespawnTimer(this, player,
					this.loginRespawnDelay, this::autoRespawnPlayer));
		}
	}
	
	@EventHandler
	public void onPlayerLogout(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		if (player.isDead()) {
			this.respawnTimers.computeIfPresent(player, (_player, timer) -> timer.stop());
			plugin().info("Player [" + player.getName() + "] logged out while dead.");
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		this.respawnTimers.computeIfPresent(event.getPlayer(), (player, timer) -> timer.stop());
	}

	private void autoRespawnPlayer(final Player player) {
		player.spigot().respawn();
		final String message = Iteration.randomElement(this.respawnQuotes);
		if (Strings.isNullOrEmpty(message)) {
			return;
		}
		player.sendMessage(TextUtil.parse(message));
	}

	public static BasicHackConfig generate(final SimpleAdminHacks plugin, final ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}
	
}
