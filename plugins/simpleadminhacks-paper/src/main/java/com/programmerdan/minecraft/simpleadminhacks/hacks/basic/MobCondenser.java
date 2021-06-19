package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.DataParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MobCondenser extends BasicHack
{
	private Random rng;

	@AutoLoad
	private double mobSpawnMultiplier;

	@AutoLoad(processor = DataParser.ENTITY_TYPE)
	private List<EntityType> types;

	public MobCondenser(SimpleAdminHacks plugin, BasicHackConfig config)
	{
		super(plugin, config);
		rng = new Random();
	}

	private boolean roll(double chance) {
		return rng.nextDouble() <= chance;
	}

	@EventHandler(priority = EventPriority.LOW) //Run before the PortalSpawnModifier
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (types.contains(e.getEntityType())) {
			if (!roll(mobSpawnMultiplier)) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH) //Run after PortalSpawnModifier
	public void onEntityDeath(EntityDeathEvent e) {
		if (types.contains(e.getEntityType())) {
			Iterator<ItemStack> iterator = e.getDrops().iterator();

			while (iterator.hasNext()) {
				ItemStack itemStack = iterator.next();
				itemStack.setAmount((int) Math.round(itemStack.getAmount() / mobSpawnMultiplier));
			}
		}
	}

}
