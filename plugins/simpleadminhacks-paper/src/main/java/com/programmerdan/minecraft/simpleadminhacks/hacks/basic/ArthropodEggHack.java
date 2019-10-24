package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import java.util.List;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.simpleadminhacks.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.autoload.AutoLoad;

import vg.civcraft.mc.civmodcore.api.SpawnEggAPI;

public class ArthropodEggHack extends BasicHack {

	@AutoLoad
	private double eggChance;

	@AutoLoad
	private double lootingChance;
	
	@AutoLoad
	private boolean removeDrops;
	
	@AutoLoad
	private List<String> allowedTypes;

	public ArthropodEggHack(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Player targetPlayer = event.getEntity().getKiller();
		if (null == targetPlayer) {
			return;
		}

		String type = event.getEntity().getType().toString();
		if (allowedTypes == null || !allowedTypes.contains(type)) {
			return;
		}

		// Check for a baby animal
		if (event.getEntity() instanceof Ageable) {
			Ageable ageableEntity = (Ageable) event.getEntity();
			if (!ageableEntity.isAdult()) {
				return;
			}
		}

		// Check the player's currently equipped weapon
		ItemStack handstack = targetPlayer.getEquipment().getItemInMainHand();
		// Get the map of enchantments on that item
		Map<Enchantment, Integer> itemEnchants = handstack.getEnchantments();
		if (itemEnchants.isEmpty()) {
			return;
		}

		// Check if one enchantment is BaneOfArthropods
		if (null == itemEnchants.get(Enchantment.DAMAGE_ARTHROPODS)) {
			return;
		}

		double randomNum = Math.random();
		double levelOfArthropod = handstack.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS);
		double levelOfLooting = handstack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);

		double targetPercentage = (eggChance * levelOfArthropod) + (lootingChance * levelOfLooting);

		// Check if egg should be spawned
		if (randomNum < targetPercentage) {
			ItemStack item = new ItemStack(SpawnEggAPI.getSpawnEgg(event.getEntityType()), 1);
			if (removeDrops) {
				event.getDrops().clear();
				event.setDroppedExp(0);
			}
			event.getDrops().add(item);
		}
	}

}
