package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.logging.Level;

import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;

public class StriderBreeding extends BasicHack {
	@AutoLoad
	private double minSpeed;
	@AutoLoad
	private double maxSpeed;
	@AutoLoad
	private int minHealth;
	@AutoLoad
	private int maxHealth;

	public StriderBreeding(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onStriderBreed(EntityBreedEvent event) {
		if (event.getEntity().getType() != EntityType.STRIDER) {
			return;
		}
		double dadSpeed = event.getFather().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
		double mumSpeed = event.getMother().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
		double randomSpeed = Math.random() * (this.maxSpeed - this.minSpeed) + this.minSpeed;
		//the 0.44.... number, I was given this number by Okx#5481, apparently it is the horse speed generation number
		//Also worth noting he told me it is not exactly a bell curve even if I did name it bellCurve
		double bellCurve = (0.44999998807907104 + randomSpeed * 0.3 + randomSpeed * 0.3 + randomSpeed * 0.3) * 0.25;
		double newStriderSpeed = (dadSpeed + mumSpeed + bellCurve) / 3;
		if (newStriderSpeed < minSpeed) {
			newStriderSpeed = minSpeed;
		} else if (newStriderSpeed > maxSpeed) {
			newStriderSpeed = maxSpeed;
		}
		event.getEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(newStriderSpeed);

		double dadHealth = event.getFather().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
		double mumHealth = event.getFather().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
		double randomHealth = Math.random() * (this.maxHealth - this.minHealth) + this.minHealth;
		double newStriderHealth = Math.round(((float) dadHealth + (float) mumHealth + (float) randomHealth) / 3);
		if (newStriderHealth < minHealth) {
			newStriderHealth = minHealth;
		} else if (newStriderHealth > maxHealth) {
			newStriderHealth = maxHealth;
		}
		event.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newStriderHealth);
		event.getEntity().setHealth(newStriderHealth);
		plugin.getLogger().log(Level.INFO, "Strider breed to have speed: " + newStriderSpeed
				+ " and health: " + newStriderHealth);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onStriderSpawn(CreatureSpawnEvent event) {
		if (event.getEntity().getType() != EntityType.STRIDER) {
			return;
		}
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
			return;
		}
		rollSpeedStat(event.getEntity(), this.minSpeed, this.maxSpeed);
		rollHealthStat(event.getEntity(), this.minHealth, this.maxHealth);
	}

	public void rollSpeedStat(LivingEntity strider, double minSpeed, double maxSpeed) {
		AttributeInstance moveSpeed = strider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		if (moveSpeed == null) {
			return;
		}
		double random = Math.random() * (maxSpeed - minSpeed) + minSpeed;
		double bellCurve = (0.44999998807907104 + random * 0.3 + random * 0.3 + random * 0.3) * 0.25;
		if (bellCurve < minSpeed) {
			bellCurve = minSpeed;
		} else if (bellCurve > maxSpeed) {
			bellCurve = maxSpeed;
		}
		moveSpeed.setBaseValue(bellCurve);
		plugin.getLogger().log(Level.INFO, "Setting Strider Speed to: " + bellCurve);
	}

	public void rollHealthStat(LivingEntity strider, int minHealth, int maxHealth) {
		AttributeInstance moveSpeed = strider.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (moveSpeed == null) {
			return;
		}
		int newValue = Math.round((float) Math.random() * (maxHealth - minHealth) + minHealth);
		if (newValue < minHealth) {
			newValue = minHealth;
		} else if (newValue > maxHealth) {
			newValue = maxHealth;
		}
		moveSpeed.setBaseValue(newValue);
		//We set the health to the new value since when we update the health, they dont auto heal
		strider.setHealth(newValue);
		plugin.getLogger().log(Level.INFO, "Setting Strider Health to: " + newValue);
	}
}
