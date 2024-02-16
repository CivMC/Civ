package me.josvth.randomspawn;

import me.josvth.randomspawn.handlers.CommandHandler;
import me.josvth.randomspawn.handlers.YamlHandler;
import me.josvth.randomspawn.listeners.*;
import me.josvth.randomspawn.spawn.AsyncSpawnSelector;
import me.josvth.randomspawn.spawn.BlockingSpawnSelector;
import me.josvth.randomspawn.spawn.SpawnSelector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Set;

public class RandomSpawn extends JavaPlugin {

	public YamlHandler yamlHandler;
	CommandHandler commandHandler;
	RespawnListener respawnListener;
	JoinListener joinListener;
	WorldChangeListener worldChangeListener;
	SignListener signListener;
	DamageListener damageListener;

	private SpawnSelector spawnSelector;

	@Override
	public void onEnable() {

		// setup handlers
		yamlHandler = new YamlHandler(this);
		logDebug("Yamls loaded!");

		commandHandler = new CommandHandler(this);
		logDebug("Commands registered!");

		// setup listeners
		respawnListener = new RespawnListener(this);
		joinListener = new JoinListener(this);
		worldChangeListener = new WorldChangeListener(this);
		signListener = new SignListener(this);
		damageListener = new DamageListener(this);

		Set<String> keys = yamlHandler.worlds.getKeys(false);
		spawnSelector = new AsyncSpawnSelector(this, new BlockingSpawnSelector(this, yamlHandler, this.getLogger()), new ArrayList<>(keys));
	}

	public void logInfo(String message) {
		getLogger().info(message);
	}

	public void logDebug(String message) {
		if (yamlHandler.config.getBoolean("debug", false)) {
			getLogger().info("(DEBUG) " + message);
		}
	}

	public void logWarning(String message) {
		getLogger().warning(message);
	}

	public void playerInfo(Player player, String message) {
		player.sendMessage(ChatColor.AQUA + "[RandomSpawn] " + ChatColor.RESET + message);
	}

	// *------------------------------------------------------------------------------------------------------------*
	// | The following chooseSpawn methods contains code made by NuclearW |
	// | based on his SpawnArea plugin: |
	// |
	// http://forums.bukkit.org/threads/tp-spawnarea-v0-1-spawns-targetPlayers-in-a-set-area-randomly-1060.20408/
	// |
	// *------------------------------------------------------------------------------------------------------------*


	// Methods for a safe landing :)
	public void sendGround(Player player, Location location) {
		if (!location.getChunk().isLoaded()) {
			location.getChunk().load();
		}
	}

	public SpawnSelector getSpawnSelector() {
		return spawnSelector;
	}

	@Deprecated
	public Location chooseSpawn(World to) {
		return spawnSelector.getRandomSpawnLocation(to);
	}
}
