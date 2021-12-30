package com.github.devotedmc.hiddenore.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import com.github.devotedmc.hiddenore.BlockConfig;
import com.github.devotedmc.hiddenore.DropConfig;
import com.github.devotedmc.hiddenore.HiddenOre;
import com.github.devotedmc.hiddenore.Config;
import com.github.devotedmc.hiddenore.ToolConfig;
import com.github.devotedmc.hiddenore.VeinConfig;
import com.github.devotedmc.hiddenore.events.HiddenOreEvent;
import com.github.devotedmc.hiddenore.events.HiddenOreGenerateEvent;
import com.github.devotedmc.hiddenore.util.FakePlayer;

/**
 * Heart of ore generation, handles breaks.
 * 
 * @author soerxpso, programmerdan
 */
public class BlockBreakListener implements Listener {
	private final HiddenOre plugin;
	
	public BlockBreakListener(HiddenOre plugin) {
		this.plugin = plugin;
	}

	/**
	 * Core method of interest, captures block breaks and checks if we care; if we do, continue
	 *
	 * @param event The break event to check
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		try {
			doBlockBreak(event);
		} catch (NullPointerException npe) {
			plugin.getLogger().log(Level.WARNING, "Failure in Block Break handling", npe);
		}
	}

	public static void spoofBlockBreak(Location playerLoc, Block block, ItemStack inHand) {
		HiddenOre.getPlugin().getBreakListener().doBlockBreak(
					new BlockBreakEvent(block, new FakePlayer(playerLoc, inHand))
				);
	}
	/**
	 * The heart of the plugin; handles block breaks and what to drop from them.
	 * Every attempt has been made to keep this pretty lightweight but it has
	 * grown as the featureset has grown.
	 * 
	 * @param event
	 */
	private void doBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		BlockData bd = b.getBlockData();
		String blockName = b.getType().name();

		UUID world = b.getWorld().getUID();

		BlockConfig bc = Config.isDropBlock(world, bd);

		Player p = event.getPlayer();
		
		// Check if suppression is on (preventing all drops). Fires off a HiddenOreGenerateEvent in case
		// someone listening might object to our manipulation here.
		if (bc != null && bc.suppressDrops) {
			debug("Attempting to suppress break of tracked type {0}", blockName);
			HiddenOreGenerateEvent hoges = new HiddenOreGenerateEvent(p, b, Material.AIR);
			Bukkit.getPluginManager().callEvent(hoges);
			if (!hoges.isCancelled()) {
				b.setType(Material.AIR);
				event.setCancelled(true);				
			}
			bc = null;
		}

		// Check with out tracker to see if any more drops are available in this little slice of the world.
		if (!plugin.getTracking().trackBreak(event.getBlock().getLocation())) {
			debug("Drop skipped at {0} - layer break max met", event.getBlock().getLocation());
			return;
		}

		// We have no block config.
		if (bc == null) return;

		// There is no player responsible.
		if (p == null) return;

		debug("Break of tracked type {0} by {1}", blockName, p.getDisplayName());

		ItemStack inMainHand = p.getInventory().getItemInMainHand();
		
		// Check SilkTouch failfast, if configured.
		if (!Config.instance.ignoreSilktouch && inMainHand != null && inMainHand.hasItemMeta() && 
				inMainHand.getEnchantments() != null && inMainHand.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
			plugin.getTracking().postTrackBreak(event.getBlock().getLocation(), true); // before return, let's posttrack.
			return;
		}

		boolean hasDrop = false;

		StringBuilder alertUser = new StringBuilder().append(Config.instance.defaultPrefix);

		String biomeName = b.getBiome().name();

		if (bc.dropMultiple) {
			for (String drop : bc.getDrops()) {
				DropConfig dc = bc.getDropConfig(drop);

				if (!dc.dropsWithTool(biomeName, inMainHand)) {
 					debug("Cannot drop {0} - wrong tool", drop);
					continue;
				}

				if (b.getLocation().getBlockY() > dc.getMaxY(biomeName)
						|| b.getLocation().getBlockY() < dc.getMinY(biomeName)) {
					debug("Cannot drop {0} - wrong Y", drop);
					continue;
				}
				
				ToolConfig dropModifier = dc.dropsWithToolConfig(biomeName, inMainHand);

				double dropChance = dc.getChance(biomeName) 
						* (dropModifier == null ? 1.0 : dropModifier.getDropChanceModifier())
						* dc.getStateChance(biomeName, p);
				
				VeinConfig vc = dc.getVeinNature(); // handle vein externally
				if (vc != null) {
					dropChance *= vc.getOreChance(b.getLocation());
				}

				// Random check to decide whether or not the special drop should be dropped
				if (dropChance > Math.random()) {
					hasDrop = doDrops(hasDrop, b, event, p, biomeName, dropModifier, 
							drop, dc, blockName, bc, alertUser);
					if (!hasDrop) {
						// Core of event cancelled!
						plugin.getTracking().postTrackBreak(event.getBlock().getLocation(), true);
						return;
					} else {
						doXP(dc, biomeName, dropModifier, b.getLocation(), p);
					}
				}
			}
		} else {
			String drop = bc.getDropConfig(Math.random(), biomeName, inMainHand, 
					p, b.getLocation()); // handles vein internally

			if (drop != null) {
				DropConfig dc = bc.getDropConfig(drop);
				ToolConfig tc = dc.dropsWithToolConfig(biomeName, inMainHand);
				
				hasDrop = doDrops(hasDrop, b, event, p, biomeName, tc, 
						drop, dc, blockName, bc, alertUser);
				if (!hasDrop) {
					// Core of event cancelled!
					plugin.getTracking().postTrackBreak(event.getBlock().getLocation(), true);
					return;
				} else {
					doXP(dc, biomeName, tc, b.getLocation(), p);
				}
			}
		}
		if (Config.isAlertUser() && hasDrop) {
			if (Config.isListDrops()) {
				alertUser.deleteCharAt(alertUser.length() - 1);
			}
			String alert = alertUser.toString();
			if (alert.length() > 0) {
				event.getPlayer().sendMessage(ChatColor.GOLD + alert);
			}
		}
		
		plugin.getTracking().postTrackBreak(event.getBlock().getLocation(), true);
	}

	private void doXP(DropConfig dc, String biomeName, ToolConfig dropModifier, Location loc, Player p) {
		double xpChance = dc.getXPChance(biomeName) 
				* (dropModifier == null ? 1.0 : dropModifier.getDropChanceModifier())
				* dc.getStateChance(biomeName, p);
		
		VeinConfig vc = dc.getVeinNature();
		if (vc != null) {
			xpChance *= vc.getOreChance(loc);
		}
		
		if (xpChance > Math.random()) {
			int toXP = dc.renderXP(biomeName, dropModifier);
			if (toXP > 0) {
				Entity xp = loc.getWorld().spawnEntity(loc, EntityType.EXPERIENCE_ORB);
				if (xp instanceof ExperienceOrb) {
					ExperienceOrb eo = (ExperienceOrb) xp;
					eo.setExperience(toXP);
				}
			}
		}
	}
	
	/**
	 * Reuse! Handles the actual rendering and dropping of drops. 
	 * 
	 * @param clearBlock Should we clear the block which was broken? Also fires a HiddenOreGenerateEvent for this block incase it shouldn't be HiddenOre'd.
	 * @param sourceBlock The Block that was broken
	 * @param event the BlockBreakEvent that started this whole chain of events
	 * @param player the Player going around breaking stuff.
	 * @param biomeName the Biome Name in which things were broken.
	 * @param dropTool the ToolConfig that best matches for this block config, drop config, and biome.
	 * @param dropName The configured name of this drop
	 * @param dropConfig The actual drop config being invoked
	 * @param blockName The configured name of the block config
	 * @param blockConfig The actual block config being invoked.
	 * @param alertBuffer a StringBuffer used to report to the user on what was found, if configured.
	 * @return true if everything went well, false if the generate was cancelled or other error.
	 */
	private Boolean doDrops(boolean clearBlock, Block sourceBlock, BlockBreakEvent event, Player player, String biomeName, ToolConfig dropTool, 
			String dropName, DropConfig dropConfig, String blockName, BlockConfig blockConfig, StringBuilder alertBuffer) {
		// Remove block, drop special drop and cancel the event
		if (!clearBlock) {
			HiddenOreGenerateEvent hoge = new HiddenOreGenerateEvent(player, sourceBlock, Material.AIR);
			Bukkit.getPluginManager().callEvent(hoge);
			if (!hoge.isCancelled()) {
				sourceBlock.setType(Material.AIR);
				event.setCancelled(true);				
			} else {
				log("For {0} at {1}, HiddenOre for {2} cancelled.", player.getDisplayName(), player.getLocation(), sourceBlock);
				debug("Generate cancelled, cancelling HiddenOre drop.");
				return false;
			}
		}
		
		final List<ItemStack> items = dropConfig.renderDrop(biomeName, dropTool);
		final Location sourceLocation = sourceBlock.getLocation();
		if (!items.isEmpty()) {
			doActualDrops(items, sourceLocation, player, dropName, blockName, blockConfig, alertBuffer);
		}

		if (dropConfig.transformIfAble) {
			final List<ItemStack> transform = dropConfig.renderTransform(biomeName, dropTool);
			if (!transform.isEmpty()) {
				doActualGenerate(transform, sourceLocation, player, dropName, blockName, blockConfig, 
						alertBuffer, dropConfig);
			}
		}
		
		runCommand(player, dropConfig.command);

		// xp handled upstream

		return true;
	}

	private void doActualDrops(final List<ItemStack> items, final Location sourceLocation, final Player player,
			String dropName, String blockName, BlockConfig blockConfig, StringBuilder alertBuffer) {
		final HiddenOreEvent hoe = new HiddenOreEvent(player, sourceLocation, items);
		Bukkit.getPluginManager().callEvent(hoe);
		if (!hoe.isCancelled()) {
			// Schedule drop.
			new BukkitRunnable() {
				@Override
				public void run() {
					for (ItemStack item: hoe.getDrops()) {
						sourceLocation.getWorld().dropItem(sourceLocation.add(0.5, 0.5, 0.5), item).setVelocity(new Vector(0, 0.05, 0));
					}
				}
			}.runTaskLater(plugin, 1l);

			// Correct stats output.
			for (ItemStack item: hoe.getDrops()) {
				String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
				log("STAT: Player {0} at {1} broke {2} - dropping {3} {4}", 
						player.getDisplayName(), player.getLocation(), blockName, 
						item.getAmount(), name);
			}
			
			if (Config.isAlertUser()) {
				if (blockConfig.hasCustomPrefix(dropName)) {
					// Custom prefix items are immediately reported to a player
					StringBuilder customAlerts = new StringBuilder(blockConfig.getPrefix(dropName));

					for (ItemStack item : hoe.getDrops()) {
						buildAlert(customAlerts, item, null, item.getAmount(), null);
					}
					player.sendMessage(ChatColor.GOLD + customAlerts.toString());
				} else {
					// otherwise, if list drops are enabled we aggregate and report items when done everything
					if (Config.isListDrops()) {
						for (ItemStack item : hoe.getDrops()) {
							buildAlert(alertBuffer, item, null, item.getAmount(), ",");

						}
					}
				}
			}
		} else {
			log("For {0} at {1}, HiddenOre {2} cancelled.", player.getDisplayName(), player.getLocation(), dropName);
		}
		
	}

	private void doActualGenerate(final List<ItemStack> items, final Location sourceLocation, final Player player,
			String dropName, String blockName, BlockConfig blockConfig, StringBuilder alertBuffer, DropConfig dropConfig) {
		int maxWalk = 0;
		int cPlace = 0;
		double cAttempt = 0;
		boolean tryFacing = false; // pick a facing block of attacked block
		int forceFacing = -1;
		VeinConfig vc = dropConfig.getVeinNature();
		Block origin = sourceLocation.getBlock();
		for (ItemStack xform : items) {
			Material sample = xform.getType();
			Material expressed = sample;
			forceFacing = (vc == null ? -1 : (vc.getForceVisibleTransform() ? 0 : -1 )); // do index traverse on visible faces
			// to ensure overall fairness but density in discovery, we add walk attempts to cover forced facing reveal
			// to make sure we test them all before moving on.
			maxWalk += xform.getAmount() * Config.getTransformAttemptMultiplier() + (forceFacing == 0 ? visibleFaces.length : 0);
			cPlace = xform.getAmount();
			while (cPlace > 0 && maxWalk > 0) {
				Block walk = null;
				if (forceFacing > -1 && forceFacing < visibleFaces.length) {
					walk = this.getVisibleFacing(origin);//origin.getRelative(this.visibleFaces[forceFacing++]);
					forceFacing++;
					tryFacing = true;
				} else if (!tryFacing) {
					// Try to ensure something of the generation is visible.
					walk = this.getVisibleFacing(origin);
				} else {
					// Use a kind of radial bloom to try to place the discovered blocks.
					// expose u0, uA (multiplier on cube root of attempts) in config
					double z = Math.random() * 2.0 - 1.0;
					double zsq = Math.sqrt(1-Math.pow(z, 2));
					double u = 0.5 + Math.floor(Math.cbrt(cAttempt++));
					double theta = Math.random() * 2.0 * Math.PI;
					walk = origin.getRelative(
							(int) Math.round(u * zsq * Math.cos(theta)),
							(int) Math.round(u * zsq * Math.sin(theta)),
							(int) Math.round(u * z));
				}
				if (plugin.getTracking().testGen(walk.getLocation()) && blockConfig.checkGenerateBlock(walk)) {
					HiddenOreGenerateEvent hoge = new HiddenOreGenerateEvent(player, walk, sample);
					Bukkit.getPluginManager().callEvent(hoge);
					if (!hoge.isCancelled()) {
						walk.setType(hoge.getTransform());
						expressed = hoge.getTransform();
						cPlace --;
						tryFacing = true;
						plugin.getTracking().trackGen(walk.getLocation());
					}
				}
				maxWalk --;
			}

			int placed = xform.getAmount() - cPlace;
			if (placed < 1 && dropConfig.dropIfTransformFails) { // total failure.
				ItemStack toDrop = xform.clone();
				toDrop.setAmount(Math.min(xform.getAmount(), dropConfig.maxDropsIfTransformFails));
				final List<ItemStack> newDrops = new ArrayList<ItemStack>();
				newDrops.add(toDrop);
				doActualDrops(newDrops, sourceLocation, player, dropName, blockName, blockConfig, alertBuffer);
			} else {
				String name = xform.hasItemMeta() && xform.getItemMeta().hasDisplayName() ? 
						xform.getItemMeta().getDisplayName() : Config.getPrettyName(xform.getType().name());
						
				log("STAT: Player {0} at {1} broke {2} - replacing with {3} {4} as {6}", 
						player.getDisplayName(), player.getLocation(), blockName, 
						placed, name, expressed);
				
				// Anything to tell anyone about?
				if (placed > 0 && Config.isAlertUser()) {
					if (blockConfig.hasCustomPrefix(dropName)) {
						// if this block has a custom prefix we alert immediately
						StringBuilder customAlerts = new StringBuilder(blockConfig.getPrefix(dropName));
						
						buildAlert(customAlerts, null, name, placed, " nearby");

						player.sendMessage(ChatColor.GOLD + customAlerts.toString());
					} else {
						// otherwise, we aggregate our notices and send them after all drop / gen is done.
						if (Config.isListDrops()) {
							buildAlert(alertBuffer, null, name, placed, " nearby,");
						}
					}
				}
			}
		}
	}
	
	private void buildAlert(StringBuilder alertBuilder, ItemStack item, String nameOverride, int amount, String postfix) {
		String name = nameOverride;
		if (name == null && item != null) {
			name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? 
					item.getItemMeta().getDisplayName() : Config.getPrettyName(item.getType().name());
		}
		
		alertBuilder.append(" ").append(amount).append(" ").append(name);
		if (postfix != null) {
			alertBuilder.append(postfix);
		}
	}

	private static BlockFace[] visibleFaces = new BlockFace[] {
				BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH
			};
	private static Random facerandom = new Random();
	
	private Block getVisibleFacing(Block origin) {
		Block face = origin.getRelative(visibleFaces[facerandom.nextInt(visibleFaces.length)]);
		return face;
	}

	private void log(String message, Object...replace) {
		plugin.getLogger().log(Level.INFO, message, replace);
	}

	private void debug(String message, Object...replace) {
		if (Config.isDebug) {
			plugin.getLogger().log(Level.INFO, message, replace);
		}
	}
	
	private void runCommand(Player player, String command) {
		if (player != null && command != null) {
			try {
				String fCommand = command.replaceAll("%player%", player.getName()).replaceAll("%uuid%", player.getUniqueId().toString());
				
				if (Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), fCommand)) {
					log("Fired off {0} for {1}.", fCommand, player.getName());
				} else {
					log("Failed to fire off {0} for {1}.", fCommand, player.getName());
				}
			} catch (Exception e) {
				log("Failure during command processing: {0}", e);
			}
		}
	}
}
