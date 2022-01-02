package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Holds configurations for the GameFeatures module.
 *
 * @author ProgrammerDan
 */
public class GameFeaturesConfig extends SimpleHackConfig {

	private boolean potatoXPEnabled;
	private boolean villagerTrading;
	private boolean witherSpawning;
	private boolean patrolSpawning;
	private boolean isPhantomSpawning;
	private boolean enderChestPlacement;
	private boolean enderChestUse;
	private boolean shulkerBoxUse;
	private boolean totemPowers;
	private boolean chorusFruitUse;

	private boolean weepingAngel;
	private int weepingAngelEnv;
	private int weepingAngelPlayer;

	private boolean blockWaterInHell;

	private boolean minecartTeleport;
	private boolean obsidianGenerators;
	private boolean personalDeathMessages;
	private boolean disableNetheriteCrafting;
	
	private boolean goldBlockTeleport;

	public GameFeaturesConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.potatoXPEnabled = !config.getBoolean("disablePotatoXP", false);
		if (!this.potatoXPEnabled) plugin().log("  Potato XP Disabled");

		this.villagerTrading = config.getBoolean("villagerTrading", false);
		if (!villagerTrading) plugin().log("  VillagerTrading is disabled");

		this.witherSpawning = config.getBoolean("witherSpawning", false);
		if (!this.witherSpawning) plugin().log("  Wither Spawning is disabled");

		this.patrolSpawning = config.getBoolean("patrolSpawning", false);
		if (!this.patrolSpawning) plugin().log("Patrol Spawning is disabled");

		this.isPhantomSpawning = config.getBoolean("phantomSpawning", false);
		if (!this.isPhantomSpawning) plugin().log("Phantom Spawning is disabled");

		this.enderChestPlacement = config.getBoolean("enderChestPlacement", true);
		if (!this.enderChestPlacement) plugin().log("  Placing EnderChests is disabled");

		this.enderChestUse = config.getBoolean("enderChestUse", false);
		if (!this.enderChestUse) plugin().log("  Using EnderChests is disabled");

		this.shulkerBoxUse = config.getBoolean("shulkerBoxUse", false);
		if (!this.shulkerBoxUse) plugin().log("  Using Shulker Boxes is disabled");

		this.totemPowers = config.getBoolean("totemPower", false);
		if (!this.totemPowers) plugin().log("  Undeath via totems is disabled");

		this.chorusFruitUse = config.getBoolean("chorusFruitTeleportation", false);
		if (!this.chorusFruitUse) {
			plugin().log("  Chorus Fruit Teleportation is disabled");
		}

 		this.weepingAngel = config.getBoolean("weepingAngel.enabled", false);
		if (this.weepingAngel) {
			this.weepingAngelEnv = config.getInt("weepingAngel.environment", 1);
			this.weepingAngelPlayer = config.getInt("weepingAngel.playerKill", 5);

			plugin().log("  Weeping Angel is enabled. Times | Env[" + weepingAngelEnv + "] PK[" + weepingAngelPlayer + "]");
		}

		this.blockWaterInHell = config.getBoolean("blockWaterInHell", true);
		if (this.blockWaterInHell) plugin().log("  Blocking bucket use in hell biomes");

		this.minecartTeleport = config.getBoolean("minecartTeleport", false);
		if (this.minecartTeleport) plugin().log("  Minecart teleporter enabled");

		this.obsidianGenerators = config.getBoolean("obsidianGenerators", false);
		if (this.obsidianGenerators) plugin().log("  Obsidian generators enabled.");

		this.personalDeathMessages = config.getBoolean("personalDeathMessages", false);
		if (this.personalDeathMessages) plugin().log("  Personal death messages enabled.");

		this.disableNetheriteCrafting = config.getBoolean("disableNetheriteCrafting", true);
		if (this.disableNetheriteCrafting) plugin().log("  Disable Netherite Crafting enabled.");
		
		this.goldBlockTeleport = config.getBoolean("goldBlockTeleport", false);
		if (this.personalDeathMessages) plugin().log("  Gold block teleporter enabled.");

		/* Add additional feature config grabs here. */
	}

	/**
	 * @return If getting XP from potatos is enabled.
	 */
	public boolean isPotatoXPEnabled() {
		return this.potatoXPEnabled;
	}

	public boolean isVillagerTrading() {
		return this.villagerTrading;
	}

	public boolean isWitherSpawning() {
		return this.witherSpawning;
	}

	public boolean isPatrolSpawning() {
		return this.patrolSpawning;
	}

	public boolean isPhantomSpawning() {
		return this.isPhantomSpawning;
	}

	public boolean isEnderChestPlacement() {
		return this.enderChestPlacement;
	}

	public boolean isEnderChestUse() {
		return this.enderChestUse;
	}

	public boolean isShulkerBoxUse() {
		return this.shulkerBoxUse;
	}

	public boolean isTotemPowers() {
		return this.totemPowers;
	}

	public boolean isChorusFruitTeleportation() {
		return this.chorusFruitUse;
	}

	public boolean isWeepingAngel() {
		return this.weepingAngel;
	}

	public int getWeepingAngelEnv() {
		return this.weepingAngelEnv;
	}

	public int getWeepingAngelPlayer() {
		return this.weepingAngelPlayer;
	}

	public boolean isBlockWaterInHell() {
		return this.blockWaterInHell;
	}

	public boolean isMinecartTeleport() {
		return this.minecartTeleport;
	}

	public boolean isObsidianGenerators() {
		return this.obsidianGenerators;
	}

	public boolean isPersonalDeathMessages() {
		return this.personalDeathMessages;
	}

	public boolean isDisableNetheriteCrafting() {
		return this.disableNetheriteCrafting;
	}
	
	public boolean isGoldblockTeleport() {
		return goldBlockTeleport;
	}

}

