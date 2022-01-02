package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.DataParser;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.entities.EntityUtils;
import vg.civcraft.mc.civmodcore.inventory.items.SpawnEggUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MobCondenser extends BasicHack
{
	private Random rng;
	private Map<EntityType, Double> mobSpawnModifiers;

	@AutoLoad(processor = DataParser.MATERIAL)
	private List<Material> materialModificationWhitelist;

	public MobCondenser(SimpleAdminHacks plugin, BasicHackConfig config)
	{
		super(plugin, config);
		this.rng = new Random();
		this.mobSpawnModifiers = new HashMap<>();
	}

	@Override
	public void onEnable()
	{
		super.onEnable();

		final ConfigurationSection base = this.config.getBase();
		final ConfigurationSection spawnModifiers = base.getConfigurationSection("mobSpawnModifiers");
		if (spawnModifiers != null) {
			for (final String key : spawnModifiers.getKeys(false)) {
				final EntityType type = EntityUtils.getEntityType(key);
				if (type == null) {
					this.plugin.warning("[" + getClass().getSimpleName() + "] EntityType: \"" + key + "\" does not exist, skipping.");
					continue;
				}
				double modifier = spawnModifiers.getDouble(key, 1.0d);
				if (modifier < 0.0d) {
					this.plugin.warning("[" + getClass().getSimpleName() + "] Mob Spawn Modifier: \"" + modifier + "\" for \"" + key + "\" is unsupported, defaulting to 1.0");
					modifier = 1.0d;
				}
				this.mobSpawnModifiers.put(type, modifier);
				this.plugin.info("[" + getClass().getSimpleName() + "] Registered Mob Spawn Modifier: [" + type + ": " + modifier + "]");
			}
		}
	}

	@Override
	public void onDisable()
	{
		this.mobSpawnModifiers.clear();

		super.onDisable();
	}

	private boolean roll(double chance) {
		return rng.nextDouble() <= chance;
	}

	@EventHandler(priority = EventPriority.LOW) //Run before the PortalSpawnModifier
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (mobSpawnModifiers.containsKey(e.getEntityType())) {
			if (!roll(mobSpawnModifiers.get(e.getEntityType()))) {
				if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
					e.setCancelled(true);
				}
				else if (e.getEntity() instanceof Ageable) // Is from a spawn egg, and is Ageable
				{
					Ageable ageable = (Ageable) e.getEntity();

					if (!ageable.isAdult()) { //Only spawns from right clicking other mobs with a spawn egg
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onMobEggUse(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null) {
			try {
				EntityType type = SpawnEggUtils.getEntityType(e.getItem().getType());

				if (mobSpawnModifiers.containsKey(type)) {
					if (!roll(mobSpawnModifiers.get(type))) {
						e.setCancelled(true);
						e.getItem().setAmount(e.getItem().getAmount() -1);
					}
				}
			} catch (IllegalArgumentException ignored) {}
		}
	}

	@EventHandler(priority = EventPriority.HIGH) //Run after PortalSpawnModifier
	public void onEntityDeath(EntityDeathEvent e) {
		if (mobSpawnModifiers.containsKey(e.getEntityType())) {
			Iterator<ItemStack> iterator = e.getDrops().iterator();

			while (iterator.hasNext()) {
				ItemStack itemStack = iterator.next();
				if (materialModificationWhitelist.contains(itemStack.getType())) {
					itemStack.setAmount((int) Math.round(itemStack.getAmount() / mobSpawnModifiers.get(e.getEntityType())));
				}
			}
		}
	}

}
