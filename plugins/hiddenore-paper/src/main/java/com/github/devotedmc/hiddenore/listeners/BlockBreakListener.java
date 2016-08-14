package com.github.devotedmc.hiddenore.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
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
import java.util.logging.Level;

import com.github.devotedmc.hiddenore.BlockConfig;
import com.github.devotedmc.hiddenore.DropConfig;
import com.github.devotedmc.hiddenore.HiddenOre;
import com.github.devotedmc.hiddenore.Config;
import com.github.devotedmc.hiddenore.ToolConfig;
import com.github.devotedmc.hiddenore.events.HiddenOreEvent;
import com.github.devotedmc.hiddenore.events.HiddenOreGenerateEvent;
import com.github.devotedmc.hiddenore.util.FakePlayer;

public class BlockBreakListener implements Listener {
	private final HiddenOre plugin;
	
	public BlockBreakListener(HiddenOre plugin) {
		this.plugin = plugin;
	}

	/**
	 * Core method of interest, captures block breaks and checks if we care; if we do, continue
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
	@SuppressWarnings("deprecation")
	private void doBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		String blockName = b.getType().name();
		Byte sb = b.getData();

		BlockConfig bc = Config.isDropBlock(blockName, sb);

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
			return;
		}

		boolean hasDrop = false;

		StringBuffer alertUser = new StringBuffer().append(Config.instance.defaultPrefix);

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
						* (dropModifier == null ? 1.0 : dropModifier.getDropChanceModifier());

				// Random check to decide whether or not the special drop should be dropped
				if (dropChance > Math.random()) {
					hasDrop = doDrops(hasDrop, b, event, p, biomeName, dropModifier, 
							drop, dc, blockName, bc, sb, alertUser);
					if (!hasDrop) {
						// Core of event cancelled!
						return;
					}
				}
			}
		} else {
			String drop = bc.getDropConfig(Math.random(), biomeName, inMainHand, 
					b.getLocation().getBlockY());

			if (drop != null) {
				DropConfig dc = bc.getDropConfig(drop);
				ToolConfig tc = dc.dropsWithToolConfig(biomeName, inMainHand);
				
				hasDrop = doDrops(hasDrop, b, event, p, biomeName, tc, 
						drop, dc, blockName, bc, sb, alertUser);
				if (!hasDrop) {
					// Core of event cancelled!
					return;
				}
			}
		}
		if (Config.isAlertUser() && hasDrop) {
			if (Config.isListDrops()) {
				alertUser.deleteCharAt(alertUser.length() - 1);
			}

			event.getPlayer().sendMessage(ChatColor.GOLD + alertUser.toString());
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
	 * @param blockSubType The block being broken's subtype
	 * @param alertBuffer a StringBuffer used to report to the user on what was found, if configured.
	 * @return true if everything went well, false if the generate was cancelled or other error.
	 */
	private Boolean doDrops(boolean clearBlock, Block sourceBlock, BlockBreakEvent event, Player player, String biomeName, ToolConfig dropTool, 
			String dropName, DropConfig dropConfig, String blockName, BlockConfig blockConfig, byte blockSubType, StringBuffer alertBuffer) {
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
		if (items.size() > 0) {
			doActualDrops(items, sourceLocation, player, dropName, blockName, blockConfig, blockSubType, alertBuffer);
		}

		if (dropConfig.transformIfAble) {
			final List<ItemStack> transform = dropConfig.renderTransform(biomeName, dropTool);
			if (transform.size() > 0) {
				doActualGenerate(transform, sourceLocation, player, dropName, blockName, blockConfig, 
						blockSubType, alertBuffer, dropConfig);
			}
		}

		return true;
	}

	private void doActualDrops(final List<ItemStack> items, final Location sourceLocation, final Player player,
			String dropName, String blockName, BlockConfig blockConfig, byte blockSubType, StringBuffer alertBuffer) {
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
				log("STAT: Player {0} at {1} broke {2}:{3} - dropping {4} {5}:{6}", 
						player.getDisplayName(), player.getLocation(), blockName, blockSubType, 
						item.getAmount(), item.getType().name(), item.getDurability());
			}
			
			if (Config.isAlertUser()) {
				if (blockConfig.hasCustomPrefix(dropName)) {
					StringBuffer customAlerts = new StringBuffer(blockConfig.getPrefix(dropName));

					for (ItemStack item : hoe.getDrops()) {
						customAlerts.append(" ").append(item.getAmount()).append(" ")
							.append(Config.getPrettyName(item.getType().name(), item.getDurability()));
					}
					player.sendMessage(ChatColor.GOLD + customAlerts.toString());
				} else {
					if (Config.isListDrops()) {
						for (ItemStack item : hoe.getDrops()) {
							alertBuffer.append(" ").append(item.getAmount()).append(" ").append(
									Config.getPrettyName(item.getType().name(), item.getDurability())
								).append(",");
						}
					}
				}
			}
		} else {
			log("For {0} at {1}, HiddenOre {2} cancelled.", player.getDisplayName(), player.getLocation(), dropName);
		}
		
	}

	private void doActualGenerate(final List<ItemStack> items, final Location sourceLocation, final Player player,
			String dropName, String blockName, BlockConfig blockConfig, byte blockSubType, StringBuffer alertBuffer,
			DropConfig dropConfig) {
		int maxWalk = 0;
		int cPlace = 0;
		double cAttempt = 0;
		boolean tryFacing = false; // pick a facing block of attacked block
		Block origin = sourceLocation.getBlock();
		for (ItemStack xform : items) {
			Material sample = xform.getType();
			Material expressed = sample;
			maxWalk += xform.getAmount() * Config.getTransformAttemptMultiplier();
			cPlace = xform.getAmount();
			while (cPlace > 0 && maxWalk > 0) {
				Block walk = null;
				if (!tryFacing) {
					// Try to ensure something of the generation is visible.
					walk = this.getVisibleFacing(origin);
				} else {
					// Use a kind of radial bloom to try to place the discovered blocks.
					double z = Math.random() * 2.0 - 1.0;
					double zsq = Math.sqrt(1-Math.pow(z, 2));
					double u = 0.5 + Math.floor(Math.cbrt(cAttempt++));
					double theta = Math.random() * 2.0 * Math.PI;
					walk = origin.getRelative(
							(int) Math.round(u * zsq * Math.cos(theta)),
							(int) Math.round(u * zsq * Math.sin(theta)),
							(int) Math.round(u * z));
				}
				if (blockConfig.checkGenerateBlock(walk)) {
					HiddenOreGenerateEvent hoge = new HiddenOreGenerateEvent(player, walk, sample);
					Bukkit.getPluginManager().callEvent(hoge);
					if (!hoge.isCancelled()) {
						walk.setType(hoge.getTransform());
						expressed = hoge.getTransform();
						cPlace --;
						tryFacing = true;
					}
				}
				maxWalk --;
			}

			if (xform.getAmount() - cPlace < 1 && dropConfig.dropIfTransformFails) { // total failure.
				ItemStack toDrop = xform.clone();
				toDrop.setAmount(Math.min(xform.getAmount(), dropConfig.maxDropsIfTransformFails));
				final List<ItemStack> newDrops = new ArrayList<ItemStack>();
				newDrops.add(toDrop);
				doActualDrops(newDrops, sourceLocation, player, dropName, blockName, blockConfig, blockSubType, alertBuffer);
			} else {
				log("STAT: Player {0} at {1} broke {2}:{3} - replacing with {4} {5}:{6} as {7}", 
						player.getDisplayName(), player.getLocation(), blockName, blockSubType, 
						xform.getAmount()- cPlace, xform.getType().name(), xform.getDurability(),
						expressed);
				
				// Anything to tell anyone about?
				if (cPlace < xform.getAmount() && Config.isAlertUser()) {
					if (blockConfig.hasCustomPrefix(dropName)) {
						StringBuffer customAlerts = new StringBuffer(blockConfig.getPrefix(dropName));

						customAlerts.append(" ").append(xform.getAmount() - cPlace).append(" ").append(
									Config.getPrettyName(xform.getType().name(), xform.getDurability())
								).append(" nearby"); // TODO: Replace with configured suffix
						player.sendMessage(ChatColor.GOLD + customAlerts.toString());
					} else {
						if (Config.isListDrops()) {
							alertBuffer.append(" ").append(xform.getAmount() - cPlace).append(" ").append(
										Config.getPrettyName(xform.getType().name(), xform.getDurability())
									).append(" nearby,"); // TODO: Replace with configured suffix
						}
					}
				}
			}
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
}
