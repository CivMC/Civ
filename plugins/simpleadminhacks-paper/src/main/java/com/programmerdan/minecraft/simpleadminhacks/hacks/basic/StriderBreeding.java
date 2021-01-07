package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.Random;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class StriderBreeding extends BasicHack {

	Random random = new Random();

	public StriderBreeding(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void onEnable() {

	}

	@Override
	public void onDisable() {

	}

	@EventHandler
	public void onStriderSpawn(CreatureSpawnEvent event) {
		if (event.getEntity().getType() != EntityType.STRIDER) {
			return;
		}
		rollSpeedStat(event.getEntity());
		rollHealthStat(event.getEntity());
	}

	public void rollSpeedStat(Entity strider) {
		LivingEntity str = (LivingEntity) strider;
		AttributeInstance moveSpeed = str.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		if (moveSpeed == null) {
			return;
		}
		moveSpeed.setBaseValue(random.nextDouble());
	}

	public void rollHealthStat(Entity Strider) {

	}
}
