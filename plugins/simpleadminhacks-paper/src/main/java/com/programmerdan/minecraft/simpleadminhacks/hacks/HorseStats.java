package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.HorseStatsConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Strider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class HorseStats extends SimpleHack<HorseStatsConfig> implements Listener {
	private static final double INTERNAL_TO_METRES_PER_SECOND = 42.15778758471;

	public HorseStats(SimpleAdminHacks plugin, HorseStatsConfig config) {
		super(plugin, config);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onHorseStatCheck(PlayerInteractEntityEvent event) {
		if (!config.isEnabled()) {
			return;
		}
		ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
		if (!item.getType().equals(config.getHorseCheckerItem())) {
			return;
		}
		Entity entity = event.getRightClicked();
		if (entity instanceof AbstractHorse) {
			AbstractHorse horse = (AbstractHorse)entity;
			AttributeInstance attrHealth = horse.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			AttributeInstance attrSpeed = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
			event.getPlayer().sendMessage(String.format("%sHealth = %f, Speed = %fm/s, Jump height = %f blocks",
					ChatColor.YELLOW,
					attrHealth.getBaseValue(),
					attrSpeed.getBaseValue() * INTERNAL_TO_METRES_PER_SECOND,
					jumpHeightInBlocks(horse.getJumpStrength())));
			event.setCancelled(true);
		} else if (entity instanceof Strider) {
			Strider strider = (Strider) entity;
			AttributeInstance attrHealth = strider.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			AttributeInstance attrSpeed = strider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
			event.getPlayer().sendMessage(String.format("%sHealth = %f, Speed = %fm/s",
					ChatColor.YELLOW,
					attrHealth.getBaseValue(),
					attrSpeed.getBaseValue() * INTERNAL_TO_METRES_PER_SECOND));
			event.setCancelled(true);
		} else {
			return;
		}
	}

	private double jumpHeightInBlocks(double x) {
		// This is a curve-fitted formula, so not 100% accurate
		return -0.1817584952 * x * x * x + 3.689713992 * x * x + 2.128599134 * x - 0.343930367;
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering HorseStats listeners");
			plugin().registerListener(this);
		}
	}

	@Override
	public void registerCommands() {

	}

	@Override
	public void dataBootstrap() {

	}

	@Override
	public void unregisterListeners() {

	}

	@Override
	public void unregisterCommands() {

	}

	@Override
	public void dataCleanup() {

	}

	public static HorseStatsConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new HorseStatsConfig(plugin, config);
	}

	@Override
	public String status() {
		return config.isEnabled() ? "HorseStats enabled." : "HorseStats disabled.";
	}
}
