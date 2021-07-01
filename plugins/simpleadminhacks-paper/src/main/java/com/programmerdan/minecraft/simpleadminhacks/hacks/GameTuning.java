package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.GameTuningConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.TeleportUtil;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

/**
 * This is a grab-bag class to hold any _tuning_ related configurations that impact the
 * game, server-wide.
 *
 * It's part of a series of focused hacks.
 *
 * {@link GameFixes} is focused on things that are broken or don't work, and attempts to fix them.
 * {@link GameFeatures} focuses on enabling and disabling features, like elytra, various potion states.
 * {@link GameTuning} neither fixes nor disables, but rather adjusts and reconfigures.
 *
 * Currently you can control the following:
 * - BlockEntity limits per chunk
 * - Setting bed during the day instead of just at night
 */
public class GameTuning extends SimpleHack<GameTuningConfig> implements Listener, CommandExecutor {
	public static final String NAME = "GameTuning";

	public GameTuning(SimpleAdminHacks plugin, GameTuningConfig config) {
		super(plugin, config);
	}

	@Override
	public void registerListeners() {
		if (config != null && config.isEnabled()) {
			plugin().log("Registering GameTuning listeners");
			plugin().registerListener(this);
		}
	}

	@Override
	public void unregisterListeners() {
		// Bukkit does this for us.
	}

	@Override
	public void registerCommands() {
		if (config.isEnabled()) {
			plugin.registerCommand("chunklimits", this);
		}
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataBootstrap() {
	}

	@Override
	public void dataCleanup() {
		// NO-OP
	}

	@Override
	public String status() {
		StringBuilder genStatus = new StringBuilder();
		genStatus.append("GameTuning is ");
		if (config != null && config.isEnabled()) {
			genStatus.append("active\n");
			if (config.areChunkLimitsEnabled()) {
				genStatus.append("  Chunk Limits are enabled\n");
			} else {
				genStatus.append("  Chunk Limits are disabled\n");
			}
			if (config.areDaytimeBedsEnabled()) {
				genStatus.append("  Daytime Beds are enabled\n");
			} else {
				genStatus.append("  Daytime Beds are disabled\n");
			}

			genStatus.append("  One To One Nether is ");
			if (config.isOneToOneNether()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}
			genStatus.append("  Stop trap horses is ");
			if (config.stopTrapHorses()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Kill trap horses is ");
			if (config.killTrapHorses()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Changing spawner type is ");
			if (config.canChangeSpawnerType()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Villager trading is ");
			if (config.allowVillagerTrading()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Ender grief is ");
			if (config.isEnderGrief()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Wither grief is ");
			if (config.isWitherGrief()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Dragon grief is ");
			if (config.isDragonGrief()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Prevent falling through bedrock is ");
			if (config.isPreventFallingThroughBedrock()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Bad Omen is ");
			if (config.isBadOmenEnabled()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

			genStatus.append("  Rain reduction is ");
			if (config.isRainReductionEnabled()) {
				genStatus.append("enabled\n");
			} else {
				genStatus.append("disabled\n");
			}

		} else {
			genStatus.append("inactive");
		}
		return genStatus.toString();
	}

	public static GameTuningConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new GameTuningConfig(plugin, config);
	}

	/* From here on, the actual meat of the hack. Above is basically boilerplate for micro-plugins.*/

	/**
	 * Many thanks to BlackXNT for his work on this event in Humbug, which I have largely copied and expanded.
	 *
	 * This tracks block placements, and if a limit is configured and the block is a TileEntity w/ state,
	 * will reject the placement if otherwise it would exceed limits for the Chunk.
	 *
	 * @param event the Placement event.
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void chunkLimits(BlockPlaceEvent event) {
		if (!config.isEnabled() || !config.areChunkLimitsEnabled()) return;
		try {
			Player player = event.getPlayer();
			Block block = event.getBlock();

			if (!config.applyChunkLimits(player.getUniqueId())) return;

			Material mat = block.getType();

			Integer limit = config.getChunkLimit(mat);
			if (limit == null) return;

			int current = 0;
			for (BlockState state : block.getChunk().getTileEntities()) {
				if (state != null && mat.equals(state.getType())) {
					if (++current > limit) {
						event.setCancelled(true);
						player.sendMessage(config.getChunkLimitsExceededMessage()
								.replaceAll("%Limit%",Integer.toString(limit))
								.replaceAll("%Material%", ItemUtils.getItemName(mat))
						);
						return;
					}
				}
			}
		} catch (Exception e) {
			plugin().log(Level.WARNING, "Failed to measure chunk limit", e);
		}
	}

	// If any limit at all, cancel the piston event.
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void chunkLimitsExploitExtend(BlockPistonExtendEvent event) {
		if (!config.isEnabled() || !config.areChunkLimitsEnabled()) return;
		List<Block> blocks = event.getBlocks();
		if (!blocks.isEmpty()) {
			for (Block b : blocks) {
				if (b != null && config.getChunkLimit(b.getType()) != null) {
					event.setCancelled(true);
					return; // TODO send message to nearby player warning of reason for stopping.
				}
			}
		}
	}

	// Yes, this is identical ...
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void chunkLimitsExploitRetract(BlockPistonRetractEvent event) {
		if (!config.isEnabled() || !config.areChunkLimitsEnabled()) return;
		List<Block> blocks = event.getBlocks();
		if (!blocks.isEmpty()) {
			for (Block b : blocks) {
				if (b != null && config.getChunkLimit(b.getType()) != null) {
					event.setCancelled(true);
					return; // TODO send message to nearby player warning of reason for stopping.
				}
			}
		}
	}
	
	@EventHandler
	public void enterBed(PlayerBedEnterEvent e) {
		if (!config.isEnabled() || !config.areDaytimeBedsEnabled()) {
			return;
		}
		if (BedEnterResult.NOT_POSSIBLE_NOW.equals(e.getBedEnterResult()) || BedEnterResult.NOT_SAFE.equals(e.getBedEnterResult())) {
			e.getPlayer().setBedSpawnLocation(e.getBed().getLocation(), false);
			e.getPlayer().sendTitle("", config.getDaytimeBedSpawnSetMessage());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPortalTravel(EntityPortalEvent event) {
		if (!config.isEnabled()) {
			return;
		}
		if (!config.allowNetherTravel() && event.getTo() != null && event.getTo().getWorld().getEnvironment() == Environment.NETHER) {
			event.setCancelled(true);
			return;
		}
		if (config.isOneToOneNether()) {
			Location newLoc = event.getFrom();
			newLoc.setWorld(event.getTo().getWorld());
			event.setTo(newLoc);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPortalTravel(PlayerPortalEvent event) {
		if(config.isEnabled() && PlayerTeleportEvent.TeleportCause.NETHER_PORTAL == event.getCause()) {
			if(!config.allowNetherTravel()) {
				event.setCancelled(true);
			} else if (config.isOneToOneNether()) {
				Location newLoc = event.getFrom();
				newLoc.setWorld(event.getTo().getWorld());
				event.setTo(newLoc);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPortalCreate(PortalCreateEvent event) {
		if (config.isEnabled() && !config.isReturnNetherPortal()) {
			if (PortalCreateEvent.CreateReason.FIRE.equals(event.getReason()) && Environment.NETHER.equals(event.getWorld().getEnvironment())) {
				event.setCancelled(true);
			}
		}
	}

	//Trying to stop dupe bugs via minecart inventories
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityRightClick(PlayerInteractEntityEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isChestedMinecartInventories() || !config.isHopperMinecartInventories()) {
			Entity target = event.getRightClicked();

			if (target.getType().equals(EntityType.MINECART_CHEST) && !config.isChestedMinecartInventories()) {
				event.setCancelled(true);
			}

			if (target.getType().equals(EntityType.MINECART_HOPPER) && !config.isHopperMinecartInventories()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isChestedMinecartInventories() || !config.isHopperMinecartInventories()) {
			InventoryHolder holder = event.getDestination().getHolder();
			if (holder instanceof StorageMinecart && !config.isChestedMinecartInventories()) {
				event.setCancelled(true);
			}

			if (holder instanceof HopperMinecart && !config.isHopperMinecartInventories()) {
				event.setCancelled(true);
			}

		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if(config.isEnabled() && config.stopTrapHorses()) {
			Entity entity = event.getEntity();
			if(entity instanceof Skeleton && entity.isInsideVehicle() && entity.getVehicle() instanceof SkeletonHorse) {
				if(config.killTrapHorses()) {
					entity.getVehicle().remove();
				}
				entity.remove();
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(!config.isEnabled() || !Action.RIGHT_CLICK_BLOCK.equals(event.getAction())) {
			return;
		}
		boolean cancel = !config.canChangeSpawnerType() && Material.SPAWNER.equals(event.getClickedBlock().getType())
				&& event.getItem() != null && event.getItem().getItemMeta() instanceof SpawnEggMeta;
		if (cancel) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(config.isEnabled() && !config.canPlace(event.getBlock().getType())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You're not allowed to place that!");
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(config.isEnabled() && !config.allowVillagerTrading()) {
			Entity npc = event.getRightClicked();
			// consistency, preserving null check
			if(npc != null && EntityType.VILLAGER.equals(npc.getType())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if(config.isEnabled() &&
				(!config.isEnderGrief() && EntityType.ENDERMAN.equals(event.getEntityType())) ||
				(!config.isWitherGrief() && EntityType.WITHER.equals(event.getEntityType())) ||
				(!config.isDragonGrief() && EntityType.ENDER_DRAGON.equals(event.getEntityType()))) {
			event.setCancelled(true);
		}
	}

	/**
	 * Speculative handler for dragon fireballs and exploding wither skulls
	 *
	 * Some examples online prefer event.getEntity() instanceof DragonFireball and WitherSkull, could try that
	 * if this does not work
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		if (config.isEnabled() &&
				(!config.isDragonGrief() && EntityType.DRAGON_FIREBALL.equals(event.getEntityType())) ||
				(!config.isWitherGrief() && EntityType.WITHER_SKULL.equals(event.getEntityType()))) {
			event.setCancelled(true);
			// note this might not prevent block breaks, check on that (1.14)
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(config.isEnabled() && config.isPreventFallingThroughBedrock() && event.getTo().getY() < 1
				&& GameMode.SURVIVAL.equals(event.getPlayer().getGameMode())) {
			TeleportUtil.tryToTeleportVertically(event.getPlayer(), event.getTo(), "falling into the void");
		}
	}

	@EventHandler
	public void onBadOmenEffect(EntityPotionEffectEvent event) {
		if (!config.isEnabled() || config.isBadOmenEnabled()) {
			return;
		}
		PotionEffect effect = event.getNewEffect();
		if (effect != null && effect.getType().equals(PotionEffectType.BAD_OMEN)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onRain(WeatherChangeEvent event) {
		if (!config.isEnabled() || !config.isRainReductionEnabled()) {
			return;
		}
		if (event.toWeatherState() && Math.random() >= config.getRainOccurrenceChance()) {
			event.setCancelled(true);
			plugin().getLogger().info("Rain event was cancelled");
		}
	}

	@Override
	public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] strings) {
		Map<Material, Integer> blockLimits = config.getBlockEntityLimits();

		StringBuilder stringB = new StringBuilder();

		stringB.append(ChatColor.AQUA + "-- Chunk Limitations for the following blocks --\n");
		stringB.append("\n");

		for (Map.Entry<Material, Integer> entries: blockLimits.entrySet()) {
			Material material = entries.getKey();
			Integer blockLimit = entries.getValue();
			stringB.append("" + ChatColor.WHITE + material + " : " + blockLimit+ "\n");
		}
		commandSender.sendMessage(stringB.toString());
		return true;
	}


}
