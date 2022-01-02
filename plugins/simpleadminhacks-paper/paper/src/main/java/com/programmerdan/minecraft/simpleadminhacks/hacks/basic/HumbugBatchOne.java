package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class HumbugBatchOne extends BasicHack {

	@AutoLoad
	private boolean allowSheepDying;

	@AutoLoad
	private boolean disableGapples;

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

	@AutoLoad
	private boolean disableLavaCobbleMountains;

	@AutoLoad
	private boolean disableWanderingTrader;

	@AutoLoad
	private boolean preventPearlGlitching;

	@AutoLoad
	private boolean preventUsingEyeOfEnder;

	@AutoLoad
	private boolean disableEndGatewayTP;

	@AutoLoad
	private boolean disablePiglins;

	public HumbugBatchOne(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onDyeWool(SheepDyeWoolEvent event) {
		if (!allowSheepDying) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
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
	public void disableGapples(PlayerItemConsumeEvent event) {
		if(!disableGapples) {
			return;
		}
		if(event.getItem().getType() == Material.ENCHANTED_GOLDEN_APPLE) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to eat Golden Apples.");
		}
	}

	@EventHandler
	public void cauldronEmpty(CauldronLevelChangeEvent e) {
		if (infiniteCauldrons && e.getReason() == CauldronLevelChangeEvent.ChangeReason.BOTTLE_FILL) {
			e.setNewLevel(e.getOldLevel());
		}
	}

	@EventHandler
	public void preventEndCrystalUsage(PlayerInteractEvent e) {
		if (!disableEnderDragon) {
			return;
		}

		if (e.getItem() == null) {
			return;
		}

		Player p = e.getPlayer();
		Environment env = p.getWorld().getEnvironment();

		if (!(env == Environment.THE_END)) {
			return;
		}

		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem().getType() == Material.END_CRYSTAL) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Sorry, placing end crystals is disabled in this world!");
		}
	}

	@EventHandler
	public void gatewayTP(PlayerTeleportEvent e) {
		if (!disableEndGatewayTP) {
			return;
		}
		if (e.getCause() == TeleportCause.END_GATEWAY) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Sorry, these are disabled");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
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

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void potionEffect(EntityPotionEffectEvent event) {
		if (!disableMiningFatigue) {
			return;
		}
		if (event.getAction() == org.bukkit.event.entity.EntityPotionEffectEvent.Action.REMOVED || event.getCause() == Cause.PLUGIN) {
			return;
		}
		if (event.getModifiedType() == PotionEffectType.SLOW_DIGGING) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void equipBanner(PlayerInteractEvent event) {
		if (!canEquipBanners) {
			return;
		}
		if (event.getItem() == null || !Tag.BANNERS.isTagged(event.getItem().getType()) // API
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

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void tradeWanderer(PlayerInteractEntityEvent event) {
		if (!disableWanderingTrader) {
			return;
		}
		if (event.getRightClicked().getType() == EntityType.WANDERING_TRADER) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		if (!preventPearlGlitching || event.getCause() != TeleportCause.ENDER_PEARL) {
			return;
		}
		Location to = event.getTo();

		// From and To are feet positions. Check and make sure we can teleport to a
		// location with air
		// above the To location.
		Block toBlock = to.getBlock();
		Block aboveBlock = toBlock.getRelative(BlockFace.UP);
		Block belowBlock = toBlock.getRelative(BlockFace.DOWN);
		boolean lowerBlockBypass = false;
		double height = 0.0;
		Material mat = toBlock.getType();
		if (Tag.SLABS.isTagged(mat)) {
			lowerBlockBypass = true;
			height = 0.5;
		}
		else if (Tag.BEDS.isTagged(mat)) {
			height = 0.562;
		}
		else if (Tag.FLOWER_POTS.isTagged(mat)) {
			height = 0.375;
		}
		else switch (mat) {
		case CHEST:
		case TRAPPED_CHEST:
		case ENDER_CHEST:
			height = 0.875;
			break;
		case LILY_PAD:
			height = 0.016;
			break;
		case ENCHANTING_TABLE:
			height = 0.016;
			break;
		case PLAYER_WALL_HEAD:
		case PLAYER_HEAD:
			height = 0.5;
			break;
		}

		// Check if the below block is difficult
		// This is added because if you face downward directly on a gate, it will
		// teleport your feet INTO the gate, thus bypassing the gate until you leave
		// that block.
		Material belowMat = belowBlock.getType();
		if (Tag.FENCE_GATES.isTagged(belowMat) || Tag.FENCES.isTagged(belowMat) || Tag.WALLS.isTagged(belowMat)) {
			height = 0.5;
		}

		boolean upperBlockBypass = false;
		if (height >= 0.5) {
			Block aboveHeadBlock = aboveBlock.getRelative(BlockFace.UP);
			if (!aboveHeadBlock.getType().isSolid()) {
				height = 0.5;
			} else {
				upperBlockBypass = true; // Cancel this event. What's happening is the user is going to get stuck due to
											// the height.
			}
		}

		// Normalize teleport to the center of the block. Feet ON the ground, plz.
		// Leave Yaw and Pitch alone
		to.setX(Math.floor(to.getX()) + 0.5);
		to.setY(Math.floor(to.getY()) + height);
		to.setZ(Math.floor(to.getZ()) + 0.5);

		if (aboveBlock.getType().isSolid() || (toBlock.getType().isSolid() && !lowerBlockBypass) || upperBlockBypass) {
			boolean bypass = false;
			if ((to.getWorld().getEnvironment() == Environment.NETHER) && (to.getBlockY() > 124) && (to.getBlockY() < 129)) {
				bypass = true;
			}
			if (!bypass) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void throwEyeOfEnder(PlayerInteractEvent pie) {
		if (!preventUsingEyeOfEnder) {
			return;
		}
		if (pie.getAction() != Action.RIGHT_CLICK_AIR && pie.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (pie.getItem() != null && pie.getItem().getType() == Material.ENDER_EYE) {
			pie.setCancelled(true);
			pie.getPlayer().sendMessage(ChatColor.RED + "Throwing Eyes of Ender is disabled.");
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void lavaToCobble(BlockPhysicsEvent e) {
		if (!disableLavaCobbleMountains) {
			return;
		}
		if (e.getBlock().getType() != Material.LAVA) {
			return;
		}
		if (isLavaSourceNear(e.getBlock(), 3)) {
			return;
		}
		boolean foundWater = false;
		for (Block block : WorldUtils.getAllBlockSides(e.getBlock(), true)) {
			if (block.getType() == Material.WATER) {
				foundWater = true;
				break;
			}
		}
		if (foundWater) {
			Bukkit.getScheduler().runTask(SimpleAdminHacks.instance(), () -> e.getBlock().setType(Material.AIR));
		}
	}

	private static boolean isLavaSourceNear(Block block, int ttl) {
		if (ttl <= 0) {
			return false;
		}
		if (block.getType() != Material.LAVA) {
			return false;
		}
		if (((Levelled) block.getBlockData()).getLevel() == 0) {
			// source block
			return true;
		}
		for (Block relative : WorldUtils.getAllBlockSides(block, true)) {
			if (isLavaSourceNear(relative, ttl - 1)) {
				return true;
			}
		}
		return false;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBartering(EntitySpawnEvent event) {
		if (disablePiglins && event.getEntityType() == EntityType.PIGLIN) {
			event.setCancelled(true);
		}
	}
}
