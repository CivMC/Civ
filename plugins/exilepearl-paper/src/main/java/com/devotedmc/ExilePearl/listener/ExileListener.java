package com.devotedmc.ExilePearl.listener;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.config.Configurable;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;
import com.devotedmc.ExilePearl.util.EntityCombustEventWrapper;
import com.devotedmc.ExilePearl.util.EntityDamageEventWrapper;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Hopper;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

/**
 * Listener for disallowing certain actions of exiled players
 * @author Gordon
 * 
 * Loss of privileges (for Devoted) when exiled, each of these needs to be toggleable in the config:
 * Cannot break reinforced blocks. (Citadel can stop damage, use that)
 * Cannot break bastions by placing blocks. (Might need bastion change)
 * Cannot throw ender pearls at all. 
 * Cannot enter a bastion field they are not on. Same teleport back feature as world border.
 * Cannot do damage to other players.
 * Cannot light fires.
 * Cannot light TNT.
 * Cannot chat in local chat. Given a message suggesting chatting in a group chat in Citadel.
 * Cannot use water or lava buckets.
 * Cannot use any potions. (Supports Bypass for Brewery "potions")
 * Cannot set a bed.
 * Cannot enter within 1k of their ExilePearl. Same teleport back feature as world border.
 * Can use a /suicide command after a 180 second timeout. (In case they get stuck in a reinforced box).
 * Cannot place snitch or note-block.
 * Exiled players can still play, mine, enchant, trade, grind, and explore.
 *
 */
public class ExileListener extends RuleListener implements Configurable {

	private Set<String> protectedAnimals = new HashSet<String>();

	/**
	 * Creates a new ExileListener instance
	 * @param pearlApi The PearlApi instance
	 */
	public ExileListener(final ExilePearlApi pearlApi) {
		super(pearlApi);
	}


	/**
	 * Clears the bed of newly exiled players
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPearled(PlayerPearledEvent e) {
		if (config.canPerform(ExileRule.USE_BED)) {
			return;
		}
		Player p = e.getPearl().getPlayer();

		if (p != null && p.isOnline()) {
			e.getPearl().getPlayer().setBedSpawnLocation(null, true);
		}
	}


	/**
	 * Clears the bed of exiled players when they log in
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (config.canPerform(ExileRule.USE_BED)) {
			return;
		}
		if (pearlApi.isPlayerExiled(e.getPlayer())) {
			//if it's a prison pearl set their spawn to the prison world, probably the end
			boolean prison = pearlApi.getPearl(e.getPlayer().getUniqueId()).getPearlType() == PearlType.PRISON;
			e.getPlayer().setBedSpawnLocation(prison ? pearlApi.getPearlConfig().getPrisonWorld().getSpawnLocation().add(0, 0.5, 0) : null, true);
		}
	}


	/**
	 * Prevent exiled players from using a bed
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerEnterBed(PlayerBedEnterEvent e) {
		checkAndCancelRule(ExileRule.USE_BED, e, e.getPlayer());
	}

	/**
	 * Prevent exiled players from throwing ender pearls
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPearlThrow(ProjectileLaunchEvent e) {
		if (e.getEntity() instanceof EnderPearl) {
			checkAndCancelRule(ExileRule.THROW_PEARL, e, (Player)e.getEntity().getShooter());
		}
	}

	/**
	 * Prevent exiled players from using buckets
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerFillBucket(PlayerBucketFillEvent e) {
		if (e.getItemStack() != null && Material.MILK_BUCKET.equals(e.getItemStack().getType())) {
			checkAndCancelRule(ExileRule.MILK_COWS, e, e.getPlayer());
		} else {
			checkAndCancelRule(ExileRule.FILL_BUCKET, e, e.getPlayer());
		}
	}

	/**
	 * Prevent exiled players from using buckets
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerEmptyBucket(PlayerBucketEmptyEvent e) {
		if (e.getBlockClicked() != null && Material.CAULDRON.equals(e.getBlockClicked().getType())) {
			checkAndCancelRule(ExileRule.FILL_CAULDRON, e, e.getPlayer());
		} else {
			checkAndCancelRule(ExileRule.USE_BUCKET, e, e.getPlayer());
		}
	}

	/**
	 * Prevent exiled players from using certain blocks
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.hasBlock()) {
			Material type = e.getClickedBlock().getType();
			if (type == Material.BREWING_STAND) {
				checkAndCancelRule(ExileRule.BREW, e, e.getPlayer());
			} else if (type == Material.ANVIL) {
				checkAndCancelRule(ExileRule.USE_ANVIL, e, e.getPlayer());
			}
		}
	}

	/**
	 * Prevent exiled players from enchanting
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerEnchant(EnchantItemEvent e) {
		checkAndCancelRule(ExileRule.ENCHANT, e, e.getEnchanter());
	}

	/**
	 * Prevent exiled players from pvping and killing pets and protected mobs
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageByEntityEvent e) {
		Player player = new EntityDamageEventWrapper(e).getPlayerDamager();
		if (player == null) {
			return;
		}

		if (e.getEntity() instanceof Player) {
			checkAndCancelRule(ExileRule.PVP, e, player);
			return;
		}

		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}

		LivingEntity living = (LivingEntity)e.getEntity();
		String name = living.getCustomName();

		if (name != null && name != "") {
			checkAndCancelRule(ExileRule.KILL_PETS, e, player);
			return;
		}

		if (!isRuleActive(ExileRule.KILL_MOBS, player.getUniqueId())) {
			return;
		}

		for(String animal : protectedAnimals) {
			Class<?> clazz = null;

			try {
				clazz = Class.forName("org.bukkit.entity." + animal);
			} catch (Exception ex) {
				continue;
			}

			if (clazz != null && clazz.isInstance(living)) {
				checkAndCancelRule(ExileRule.KILL_MOBS, e, player);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerCombust(EntityCombustByEntityEvent e) {
		Player player = new EntityCombustEventWrapper(e).getPlayerDamager();
		if (player == null) {
			return;
		}

		if (e.getEntity() instanceof Player) {
			checkAndCancelRule(ExileRule.PVP, e, player);
			return;
		}

		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}

		LivingEntity living = (LivingEntity)e.getEntity();
		String name = living.getCustomName();

		if (name != null && name != "") {
			checkAndCancelRule(ExileRule.KILL_PETS, e, player);
			return;
		}

		if (!isRuleActive(ExileRule.KILL_MOBS, player.getUniqueId())) {
			return;
		}

		for(String animal : protectedAnimals) {
			Class<?> clazz = null;

			try {
				clazz = Class.forName("org.bukkit.entity." + animal);
			} catch (Exception ex) {
				continue;
			}

			if (clazz != null && clazz.isInstance(living)) {
				checkAndCancelRule(ExileRule.KILL_MOBS, e, player);
				return;
			}
		}
	}



	/**
	 * Prevent exiled players from drinking potions
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerDrinkPotion(PlayerItemConsumeEvent e) {
		if(e.getItem().getType() == Material.POTION) {
			if (pearlApi.getBrewHandler().isBrew(e.getItem())) {
				checkAndCancelRule(ExileRule.DRINK_BREWS, e, e.getPlayer());
			} else {
				checkAndCancelRule(ExileRule.USE_POTIONS, e, e.getPlayer());
			}
		}
	}

	/**
	 * Prevent exiled players from using splash potions
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerThrowPotion(PotionSplashEvent e) {
		if(e.getEntity() != null && e.getEntity().getShooter() instanceof Player) {
			checkAndCancelRule(ExileRule.USE_POTIONS, e, (Player)e.getEntity().getShooter());
		}
	}

	/**
	 * Prevent exiled players from using lingering splash potions
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerThrowLingeringPotion(LingeringPotionSplashEvent e) {
		if(e.getEntity() != null && e.getEntity().getShooter() instanceof Player) {
			checkAndCancelRule(ExileRule.USE_POTIONS, e, (Player)e.getEntity().getShooter());
		}
	}

	/**
	 * Prevents exiled players from breaking blocks
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerIgnite(BlockIgniteEvent e) {
		if (e.getCause() == IgniteCause.FLINT_AND_STEEL) {
			checkAndCancelRule(ExileRule.IGNITE, e, e.getPlayer());
		}
	}

	/**
	 * Prevents exiled players from breaking blocks
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		checkAndCancelRule(ExileRule.MINE, e, e.getPlayer());
	}

	/**
	 * Prevents exiled players from collecting xp
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCollectXp(PlayerExpChangeEvent e) {
		if (isRuleActive(ExileRule.COLLECT_XP, e.getPlayer().getUniqueId())) {
			e.setAmount(0);
			e.getPlayer().sendMessage(ChatUtils.parseColor(String.format(Lang.ruleCantDoThat, ExileRule.COLLECT_XP.getActionString())));
		}
	}

	/**
	 * Prevents exiled players from placing certain blocks
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPlaced(BlockPlaceEvent e) {
		Material m = e.getBlockPlaced().getType();
		if (m == Material.TNT) {
			checkAndCancelRule(ExileRule.PLACE_TNT, e, e.getPlayer());
		}
	}

	/**
	 * Prevents exiled players from moving restricted items like lava and TNT
	 * into dispensers, hoppers, and droppers
	 * @param e The event
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();

		InventoryAction action = e.getAction();
		ItemStack item;
		InventoryHolder holder = null; 

		if(action == InventoryAction.PLACE_ALL
				|| action == InventoryAction.PLACE_SOME
				|| action == InventoryAction.PLACE_ONE) {
			item = e.getCursor();
			boolean clickedTop = e.getRawSlot() < e.getView().getTopInventory().getSize(); // bukkit API has bad advice
			if (item == null || !clickedTop) {
				return;
			}
			holder = e.getView().getTopInventory().getHolder(false);

		} else if(action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
			item = e.getCurrentItem();
			boolean clickedTop = e.getRawSlot() < e.getView().getTopInventory().getSize();
			if (item == null || clickedTop) { // allow remove from top but not shift INTO top
				return;
			}

			holder = e.getView().getTopInventory().getHolder(false); // we're canceling bottom
			// inventory click but only if _top_ inventory is specific type.
		} else {
			return;
		}

		if (item == null || holder == null) {
			return;
		}

		if (!(holder instanceof Dispenser || holder instanceof Dropper || holder instanceof Hopper)) {
			return;
		}

		Material m = item.getType();
		if (m == Material.TNT) {
			checkAndCancelRule(ExileRule.PLACE_TNT, e, player, false);
		}
		else if (m == Material.LAVA_BUCKET || m == Material.WATER_BUCKET) {
			checkAndCancelRule(ExileRule.USE_BUCKET, e, player, false);
		}

		if (e.isCancelled()) {
			player.sendMessage(ChatUtils.parseColor(String.format(Lang.ruleCantDoThat, "do that")));
		}
	}

	@Override
	public void loadConfig(PearlConfig config) {
		protectedAnimals.addAll(config.getProtectedAnimals());
	}
}
