package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import com.programmerdan.minecraft.simpleadminhacks.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.autoload.AutoLoad;

public class HumbugBatchOne extends BasicHack {

	@AutoLoad
	private boolean allowSheepDying;

	@AutoLoad
	private boolean allowUsingAnvils;
	@AutoLoad
	private boolean allowUsingEnchantingTables;

	@AutoLoad
	private boolean infiniteCauldrons;

	@AutoLoad
	private boolean disableEnderDragon;

	@AutoLoad
	private boolean disableIronFarms;

	@AutoLoad
	private boolean disableEnderCrystalDamage;

	@AutoLoad
	private boolean disableMiningFatigue;

	@AutoLoad
	private boolean canEquipBanners;

	public HumbugBatchOne(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@EventHandler
	public void onDyeWool(SheepDyeWoolEvent event) {
		if (!allowSheepDying) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDisallowedBlockUse(PlayerInteractEvent event) {
		if (event.getClickedBlock() == null) {
			return;
		}
		Material mat = event.getClickedBlock().getType();
		if (!allowUsingEnchantingTables
				&& (mat == Material.ANVIL || mat == Material.CHIPPED_ANVIL || mat == Material.DAMAGED_ANVIL)) {
			event.setCancelled(true);
			return;
		}
		if (!allowUsingEnchantingTables && mat == Material.ENCHANTING_TABLE) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void cauldronEmpty(CauldronLevelChangeEvent e) {
		if (infiniteCauldrons && e.getReason() == CauldronLevelChangeEvent.ChangeReason.BOTTLE_FILL) {
			e.setNewLevel(e.getOldLevel());
		}
	}

	@EventHandler
	public void dragonSpawn(EntitySpawnEvent e) {
		if (disableEnderDragon && e.getEntityType() == EntityType.ENDER_DRAGON) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void golemDeath(EntityDeathEvent e) {
		if (!disableIronFarms) {
			return;
		}
		if (e.getEntity().getType() != EntityType.IRON_GOLEM) {
			return;
		}
		Iterator<ItemStack> iter = e.getDrops().iterator();
		while (iter.hasNext()) {
			ItemStack is = iter.next();
			if (is.getType() == Material.IRON_INGOT) {
				iter.remove();
			}
		}
	}

	@EventHandler
	public void disableEnderCrystal(EntityDamageByEntityEvent e) {
		if (disableEnderCrystalDamage && e.getDamager().getType() == EntityType.ENDER_CRYSTAL) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void adminAccessBlockedChest(PlayerInteractEvent e) {
		if (!e.getPlayer().hasPermission("simpleadmin.chestsee") && !e.getPlayer().isOp()) {
			return;
		}
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		Player p = e.getPlayer();
		Set<Material> s = new TreeSet<>();
		s.add(Material.AIR);
		s.add(Material.OBSIDIAN); // probably in a vault
		List<Block> blocks = p.getLineOfSight(s, 8);
		for (Block b : blocks) {
			Material m = b.getType();
			if (m == Material.CHEST || m == Material.TRAPPED_CHEST) {
				if (b.getRelative(BlockFace.UP).getType().isOccluding()) {
					// dont show inventory twice if a normal chest is opened
					final Inventory chestInv = ((InventoryHolder) b.getState()).getInventory();
					p.openInventory(chestInv);
					p.updateInventory();
				}
				break;
			}

		}
	}

	@EventHandler
	public void potionEffect(EntityPotionEffectEvent event) {
		if (!disableMiningFatigue) {
			return;
		}
		if (event.getAction() == org.bukkit.event.entity.EntityPotionEffectEvent.Action.REMOVED) {
			return;
		}
		if (event.getModifiedType() == PotionEffectType.SLOW_DIGGING) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void equipBanner(PlayerInteractEvent event) {
		if (!canEquipBanners) {
			return;
		}
		if (event.getItem() == null || !event.getItem().getType().equals(Material.STONE) // TODO, waiting for material
																							// API
				|| (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)) {
			return;
		}
		Player player = event.getPlayer();
		ItemStack banner = new ItemStack(event.getItem());
		banner.setAmount(1);
		player.getInventory().removeItem(banner);
		if (player.getEquipment().getHelmet() != null) {
			if (player.getInventory().addItem(player.getEquipment().getHelmet()).size() != 0) {
				player.getWorld().dropItem(player.getLocation(), player.getEquipment().getHelmet());
			}
		}
		player.getEquipment().setHelmet(banner);
	}
}
