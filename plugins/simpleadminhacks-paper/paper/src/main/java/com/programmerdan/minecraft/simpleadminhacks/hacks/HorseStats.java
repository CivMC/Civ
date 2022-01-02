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
			event.getPlayer().sendMessage(String.format("%sHealth = %f, Speed = %f, Jump height = %f",
					ChatColor.YELLOW,
					attrHealth.getBaseValue(),
					attrSpeed.getBaseValue(),
					horse.getJumpStrength()));
		} else if (entity instanceof Strider) {
			Strider strider = (Strider) entity;
			AttributeInstance attrHealth = strider.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			AttributeInstance attrSpeed = strider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
			event.getPlayer().sendMessage(String.format("%sHealth = %f, Speed = %f",
					ChatColor.YELLOW,
					attrHealth.getBaseValue(),
					attrSpeed.getBaseValue()));
		} else {
			return;
		}
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
