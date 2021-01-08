package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class StriderBreeding extends BasicHack {

	public StriderBreeding(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@EventHandler
	public void onStriderSpawn(CreatureSpawnEvent event) {
		if (event.getEntity().getType() != EntityType.STRIDER) {
			return;
		}
		rollSpeedStat(event.getEntity());
		rollHealthStat(event.getEntity());
	}

	public void rollSpeedStat(LivingEntity strider) {
		AttributeInstance moveSpeed = strider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		if (moveSpeed == null) {
			return;
		}
		Double newValue = Math.random() * (0.3375 - 0.175) + 0.175;
		moveSpeed.setBaseValue(newValue);
		plugin.getLogger().log(Level.INFO, "Setting Strider Speed to: " + newValue);
	}

	public void rollHealthStat(LivingEntity strider) {
		AttributeInstance moveSpeed = strider.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (moveSpeed == null) {
			return;
		}
		int newValue = Math.round((float) Math.random() * (50 - 20) + 20);
		moveSpeed.setBaseValue(newValue);
		//We set the health to the new value since when we update the health, they dont auto heal
		strider.setHealth(newValue);
		plugin.getLogger().log(Level.INFO, "Setting Strider Health to: " + newValue);
	}
}
