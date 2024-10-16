package com.github.maxopoly.finale.misc.crossbow;

import com.github.maxopoly.finale.misc.ItemUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public class AntiAirMissile {

	private String key;

	private String itemName;
	private Color itemColor;

	private double power;
	private double speed;
	private double damage;
	private double damageRadius;
	private double homingRadius;
	private double homingStrength;
	private double activateRadius;
	private double maxRange;
	private double gravity;

	private List<AntiAirMissileInstance> instances = new ArrayList<>();

	public AntiAirMissile(String key, String itemName, Color itemColor, double power, double speed, double damage, double damageRadius,
						  double homingRadius, double homingStrength, double activateRadius, double maxRange, double gravity) {
		this.key = key;
		this.itemName = itemName;
		this.itemColor = itemColor;
		this.power = power;
		this.speed = speed;
		this.damage = damage;
		this.damageRadius = damageRadius;
		this.homingRadius = homingRadius;
		this.homingStrength = homingStrength;
		this.activateRadius = activateRadius;
		this.maxRange = maxRange;
		this.gravity = gravity;
	}

	public double getPower() {
		return power;
	}

	public double getSpeed() {
		return speed;
	}

	public double getDamage() {
		return damage;
	}

	public double getDamageRadius() {
		return damageRadius;
	}

	public double getHomingRadius() {
		return homingRadius;
	}

	public double getHomingStrength() {
		return homingStrength;
	}

	public double getActivateRadius() {
		return activateRadius;
	}

	public double getMaxRange() {
		return maxRange;
	}

	public double getGravity() {
		return gravity;
	}

	public ItemStack getItemStack() {
		ItemStack result = new ItemStack(Material.TIPPED_ARROW, 1);

		PotionMeta meta = (PotionMeta) result.getItemMeta();
		meta.displayName(Component.text(itemName));
		meta.setColor(itemColor);
		result.setItemMeta(meta);

		result = ItemUtil.setAAKey(result, key);

		return result;
	}

	public void fireAAMissile(Player shooter) {
		AntiAirMissileInstance antiAirMissileInstance = new AntiAirMissileInstance(this, shooter);
		instances.add(antiAirMissileInstance);
	}

	public void progressInstances() {
		Iterator<AntiAirMissileInstance> instancesIt = instances.iterator();
		while (instancesIt.hasNext()) {
			AntiAirMissileInstance instance = instancesIt.next();
			if (instance.progress()) {
				instancesIt.remove();
			}
		}
	}

}
