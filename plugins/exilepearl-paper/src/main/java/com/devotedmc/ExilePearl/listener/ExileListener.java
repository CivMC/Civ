package com.devotedmc.ExilePearl.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;

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
 * Cannot use any potions.
 * Cannot set a bed.
 * Cannot enter within 1k of their ExilePearl. Same teleport back feature as world border.
 * Can use a /suicide command after a 180 second timeout. (In case they get stuck in a reinforced box).
 * Cannot place snitch or note-block.
 * Exiled players can still play, mine, enchant, trade, grind, and explore.
 *
 */
public class ExileListener extends RuleListener {

	/**
	 * Creates a new ExileListener instance
	 * @param logger The logger instance
	 * @param pearls The pearl manger
	 * @param config The plugin configuration
	 */
	public ExileListener(final ExilePearlApi pearlApi) {
		super(pearlApi);
	}


	/**
	 * Clears the bed of newly exiled players
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void exileRuleClearBed(PlayerPearledEvent e) {
		if (config.getRuleCanUseBed()) {
			return;
		}
		if (e.getPearl().getPlayer().isOnline()) {
			e.getPearl().getPlayer().getPlayer().setBedSpawnLocation(null, true);
		}
	}

	/**
	 * Prevent exiled players from using a bed
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerEnterBed(PlayerBedEnterEvent e) {
		checkAndCancelRule(ExileRule.USE_BED, e, e.getPlayer());
	}

	/**
	 * Prevent exiled players from throwing ender pearls
	 * @param e The event
	 */
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPearlThrow(PlayerInteractEvent e) {
		if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if(e.getPlayer().getItemInHand().getType().equals(Material.ENDER_PEARL)) {
				checkAndCancelRule(ExileRule.THROW_PEARL, e, e.getPlayer());
			}
		}
	}

	/**
	 * Prevent exiled players from using buckets
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerFillBucket(PlayerBucketFillEvent e) {
		checkAndCancelRule(ExileRule.USE_BUCKET, e, e.getPlayer());
	}

	/**
	 * Prevent exiled players from using buckets
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerEmptyBucket(PlayerBucketEmptyEvent e) {
		checkAndCancelRule(ExileRule.USE_BUCKET, e, e.getPlayer());
	}

	/**
	 * Prevent exiled players from using local chat
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent e) {

		// TODO check chat channel
		checkAndCancelRule(ExileRule.CHAT, e, e.getPlayer());
	}

	/**
	 * Prevent exiled players from using brewing stands
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.hasBlock()) {
			if (e.getClickedBlock().getType().equals(Material.BREWING_STAND)) {
				checkAndCancelRule(ExileRule.BREW, e, e.getPlayer());
			}
		}
	}

	/**
	 * Prevent exiled players from enchanting
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerEnchant(EnchantItemEvent e) {
		checkAndCancelRule(ExileRule.ENCHANT, e, e.getEnchanter());
	}

	/**
	 * Prevent exiled players from pvping
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerPvp(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player && e.getDamager() instanceof Player)) {
			return;
		}

		checkAndCancelRule(ExileRule.PVP, e, (Player)e.getDamager());
	}

	/**
	 * Prevent exiled players from drinking potions
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDrinkPotion(PlayerItemConsumeEvent e) {
		if(e.getItem().getType() == Material.POTION) {
			checkAndCancelRule(ExileRule.USE_POTIONS, e, e.getPlayer());
		}
	}

	/**
	 * Prevent exiled players from using splash potions
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerThrowPotion(PotionSplashEvent e) {
		checkAndCancelRule(ExileRule.USE_POTIONS, e, (Player)e.getEntity().getShooter());
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
	 * Prevents exiled players from placing snitches
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSnitchPlaced(BlockPlaceEvent e) {
		Material m = e.getBlockPlaced().getType();
		if (m == Material.JUKEBOX || m == Material.NOTE_BLOCK) {
			checkAndCancelRule(ExileRule.SNITCH, e, e.getPlayer());
		}
	}
}
