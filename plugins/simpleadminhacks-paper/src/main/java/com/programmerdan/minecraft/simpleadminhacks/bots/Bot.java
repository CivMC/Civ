package com.programmerdan.minecraft.simpleadminhacks.bots;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import de.inventivegames.npc.NPCLib;
import de.inventivegames.npc.living.NPCPlayer;

/**
 * 
 * @author Daniel
 *
 */
public class Bot {
	private String botName;
	private Location spawnLocation;
	private Location location;
	private NPCPlayer bot;
	private ConfigurationSection config;
	private boolean viable;
	private boolean alive;
	private String skinName;
	private boolean collision;
	private boolean invulnerable;
	private boolean listed;
	private boolean frozen;
	private boolean controllable;

	/**
	 * Initializes a non-viable bot with an immutable 'name'.
	 * 
	 * @param botName the name of the bot.
	 */
	public Bot(String botName) {
		this.botName = botName;
		this.bot = null;
		this.spawnLocation = null;
		this.location = null;
		this.config = null;
		this.viable = false;
		this.alive = false;
		this.collision = true;
		this.invulnerable = false;
		this.listed = true;
		this.frozen = true;
		this.controllable = true;
	}
	
	public Bot(ConfigurationSection config) throws InvalidConfigurationException {
		setConfig(config);
	}
	
	public void invalidate() {
		if (this.bot != null) {
			this.despawn();
		}
		this.viable = false;
		this.alive = false;
	}
	
	/**
	 * The only way to set BotName; wires up the bot, despawns any prior iteration and spawns a new
	 * iteration regardless.
	 * See default config.yml for examples.
	 * 
	 * @param config section used to wire up the NPC.
	 * @throws InvalidConfigurationException if name or spawn location is missing, the bot is completely
	 *   invalid and this exception is thrown.
	 */
	public void setConfig(ConfigurationSection config) throws InvalidConfigurationException {
		if (config == null) {
			throw new InvalidConfigurationException("config cannot be null");
		}
		if (this.botName != null && !this.botName.equals(config.getString("name"))) {
			throw new InvalidConfigurationException("Cannot change bot name; this config is for a different bot");
		}
		
		this.config = config;
		reloadConfig();
	} 
	
	public void reloadConfig() throws InvalidConfigurationException {
		this.botName = config.getString("name");
		if (botName == null) { // invalid.
			invalidate();
			throw new InvalidConfigurationException("name is a required bot attribute");
		}
		
		this.skinName = config.getString("skin");
		
		this.spawnLocation = (Location) config.get("spawnLocation");
		if (spawnLocation == null) {
			invalidate();
			throw new InvalidConfigurationException("spawnLocation is a required bot attribute");
		}
		this.location = (Location) config.get("location");
		if (this.bot != null) {
			this.despawn();
		}
		
		this.collision = config.getBoolean("collision", true);
		this.invulnerable = config.getBoolean("invulnerable", false);
		this.listed = config.getBoolean("listed", true);
		this.frozen = config.getBoolean("frozen", false);
		this.controllable = config.getBoolean("controllable", true);

		// If the bot is set to alive, spawn it.
		if (config.getBoolean("alive", false)) {
			this.spawn();
		} else {
			this.alive = false;
		}
		this.viable = true;
	}
	
	/**
	 * Use this to set spawn point, such as "bed" or other asset as appropriate.
	 * For a guard bot, might be a "guard room" or something similar.
	 * 
	 * @param spawn the new spawn location. Is also written to the config.
	 */
	public void setSpawnLocation(Location spawn) {
		if (!viable) return;
		config.set("spawnLocation", spawn);
		this.spawnLocation = spawn;
	}
	
	/**
	 * Gets the current spawn location.
	 * 
	 * @return Location to spawn the bot at on death or regeneration.
	 */
	public Location getSpawnLocation() {
		return this.spawnLocation;
	}
	
	/**
	 * Saves the location of the bot. Do not call this all the time! Only call when saving the
	 * bot or some other big event.
	 * 
	 * Note: this does NOT update the bot's location. For that, grab the bot itself and use those
	 * methods.
	 * 
	 * @param here the location to save.
	 */
	public void setLocation(Location here) {
		if (!viable) return;
		config.set("location", here);
		this.location = here;
	}
	
	/**
	 * Gets the last saved location.
	 * @return Location at last save
	 */
	public Location getLocation() {
		return this.location;
	}
	
	/**
	 * Sets the saved skin name. As others, does not apply the skin! use the NPC accessors or
	 * reload.
	 * 
	 * @param skinName
	 */
	public void setSkinName(String skinName) {
		if (!viable) return;
		config.set("skinName", skinName);
		this.skinName = skinName;
	}

	/**
	 * Gets the skin name if any
	 * 
	 * @return the skinName
	 */
	public String getSkinName() {
		return this.skinName;
	}
	
	/**
	 * Spawns the bot if its not already alive. Sets alive to true.
	 */
	public void spawn() {
		if (this.alive) return;
		
		if (this.skinName != null) {
			this.bot = (NPCPlayer) NPCLib.spawnPlayerNPC(
					(this.location == null ? this.spawnLocation: this.location),
					botName, this.skinName);
		} else {
			this.bot = (NPCPlayer) NPCLib.spawnPlayerNPC(
					(this.location == null ? this.spawnLocation: this.location),
					botName);
		}
		
		this.bot.setCollision(this.collision);
		this.bot.setControllable(this.controllable);
		this.bot.setFrozen(this.frozen);
		this.bot.setInvulnerable(this.invulnerable);
		this.bot.setShownInList(this.listed);
		this.alive = true;
	}
	
	/**
	 * Despawns the bot if alive. Sets alive to false.
	 */
	public void despawn() {
		if (this.bot != null) {
			this.bot.despawn();
			this.alive = false;
		}
		this.bot = null;
	}
	
	/**
	 * Set alive via spawn / despawn
	 * @return
	 */
	public boolean isAlive() {
		return this.alive;
	}
	
	/**
	 * If false, means the configuration is invalid.
	 * @return
	 */
	public boolean viable() {
		return this.viable;
	}
	
	/**
	 * Returns the underlying NPC / bot if available.
	 * 
	 * @return the NPCPlayer object
	 */
	public NPCPlayer npc() {
		return this.bot;
	}
	
	/**
	 * The underlying configuration. Changes aren't automatically synced, so if you do
	 * change something call {@link #reloadConfig()} afterwards. Oh, and save the current
	 * location before you do that.
	 * 
	 * @return the underlying ConfigurationSection
	 */
	public ConfigurationSection config() {
		return this.config;
	}
	
	/**
	 * Retrieve the BotName (immutable) as set.
	 * 
	 * @return the bot name
	 */
	public String getName() {
		return this.botName;
	}

	/**
	 * Syncs the internal NPC to the config for saving. Does not save.
	 */
	public void flushToConfig() {
		if (!this.viable || !this.alive) return;
		this.setLocation(bot.getLocation());
	}
	
	/**
	 * Brings the attributes / settings of this bot
	 * @return String version of attributes / settings
	 */
	public String status() {
		if (this.viable && !this.alive) return "None - despawned";
		StringBuilder sb = new StringBuilder();
		sb.append("Skin: ").append(ChatColor.WHITE).append(this.getSkinName()).append(ChatColor.RESET);
		if (this.bot.hasGravity()) {
			sb.append(" Gravity: ").append(ChatColor.WHITE).append(this.bot.getGravity()).append(ChatColor.RESET);
		} else {
			sb.append(" Gravity: ").append(ChatColor.RED).append("off").append(ChatColor.RESET);
		}
		if (this.bot.hasCollision()) {
			sb.append(" Collision: ").append(ChatColor.WHITE).append("on").append(ChatColor.RESET);
		} else {
			sb.append(" Collision: ").append(ChatColor.RED).append("off").append(ChatColor.RESET);
		}
		if (this.bot.isShownInList()) {
			sb.append(" Listed: ").append(ChatColor.WHITE).append("yes").append(ChatColor.RESET);
		} else {
			sb.append(" Listed: ").append(ChatColor.RED).append("no").append(ChatColor.RESET);
		}
		if (this.bot.isFrozen()) {
			sb.append(" Frozen: ").append(ChatColor.WHITE).append("yes").append(ChatColor.RESET);
		} else {
			sb.append(" Frozen: ").append(ChatColor.RED).append("no").append(ChatColor.RESET);
		}
		if (this.bot.isInvulnerable()) {
			sb.append(" Safety: ").append(ChatColor.WHITE).append("on").append(ChatColor.RESET);
		} else {
			sb.append(" Safety: ").append(ChatColor.RED).append("off").append(ChatColor.RESET);
		}
		if (this.bot.isControllable()) {
			sb.append(" CnC'd: ").append(ChatColor.WHITE).append("yes").append(ChatColor.RESET);
		} else {
			sb.append(" CnC'd: ").append(ChatColor.RED).append("no").append(ChatColor.RESET);
		}

		return sb.toString();
	}
}
