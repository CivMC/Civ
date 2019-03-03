package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Dispenser;
import org.bukkit.material.Hopper;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.GameFixesConfig;

public class GameFixes extends SimpleHack<GameFixesConfig> implements Listener {
	public static final String NAME = "GameFixes";

	public GameFixes(SimpleAdminHacks plugin, GameFixesConfig config) {
		super(plugin, config);
	}

	@Override
	public void registerListeners() {
		if (config != null && config.isEnabled()) {
			plugin().log("Registering GameFixes listeners");
			plugin().registerListener(this);
		}
	}

	@Override
	public void registerCommands() {
	}

	@Override
	public void dataBootstrap() {
	}

	@Override
	public void unregisterListeners() {
		//Bukkit does this for us (why is this a method then?)
		// incase your listener semantics include things beyond the default
		//  like maybe alerting bungee of a connected listener to disable, etc.
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
	}

	@Override
	public String status() {
		StringBuilder genStatus = new StringBuilder();
		genStatus.append("GameFixes is ");
		if (config != null && config.isEnabled()) {
			genStatus.append(ChatColor.GREEN).append("active\n").append(ChatColor.RESET);
			genStatus.append("   Block elytra break bug is ");
			if (config.isBlockElytraBreakBug()) {
				genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
				genStatus.append("   Will deal " + config.getDamageOnElytraBreakBug() + " damage to players\n");
			} else {
				genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
			genStatus.append("   Block storage entities from teleporting to prevents exploits ");
			if (!config.canStorageTeleport()) {
				genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
			genStatus.append("   Hopper self-feed duplication exploit fix ");
			if (config.isStopHopperDupe()) {
				genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
			genStatus.append("   Duplications using rails exploit fix ");
			if (config.isStopRailDupe()) {
				genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
			genStatus.append("  End Portal removal exploit fix ");
			if (config.isStopEndPortalDeletion()) {
				genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
			genStatus.append("  Bed Bombing in Nether / Hell Biomes fix ");
			if (config.stopBedBombing()) {
				genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
			genStatus.append("  Tree wraparound fix ");
			if (config.stopTreeWraparound()) {
				genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
			genStatus.append("  Maintain flat bedrock ");
			if (config.maintainFlatBedrock()) {
				genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
			genStatus.append("  Maintain flat bedrock ");
			if (config.maintainFlatBedrock()) {
				genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
			genStatus.append("  Fix pearl glitches ");
			if (config.fixPearlGlitch()) {
				genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
			genStatus.append("  Prevent long signs ");
			if (config.isPreventLongSigns()) {
				genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
		} else {
			genStatus.append(ChatColor.RED).append("inactive").append(ChatColor.RESET);
		}
		return genStatus.toString();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!config.isEnabled() || !config.isBlockElytraBreakBug()) return;
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (block == null || player == null) return;

		if (!player.getLocation().equals(block.getLocation())
				&& player.getEyeLocation().getBlock().getType() != Material.AIR) {
			event.setCancelled(true);
			player.damage(config.getDamageOnElytraBreakBug());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityTeleport(EntityTeleportEvent event) {
		if (!config.isEnabled() || config.canStorageTeleport()) return;
		if (event.getEntity() instanceof InventoryHolder) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityPortal(EntityPortalEvent event) {
		if (!config.isEnabled() || config.canStorageTeleport()) return;
		if (event.getEntity() instanceof InventoryHolder) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		if (!config.isEnabled() || !config.isStopHopperDupe()) return;
		if ((event.getDestination() == null) || (event.getSource() == null) ||
				!(InventoryType.HOPPER.equals(event.getDestination().getType())) ||
				!(InventoryType.HOPPER.equals(event.getSource().getType())) ||
				!(Material.HOPPER.equals(event.getDestination().getLocation().getBlock().getType())) || 
				!(Material.HOPPER.equals(event.getSource().getLocation().getBlock().getType()))) return;
		Hopper source = (Hopper) event.getSource().getLocation().getBlock().getState().getData();
		Hopper dest = (Hopper) event.getDestination().getLocation().getBlock().getState().getData();
		if (source.getFacing().getOppositeFace() == dest.getFacing()) {
			//They're pointing into each other and will eventually dupe
			event.setCancelled(true);
		}

	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPistonPushRail(BlockPistonExtendEvent event) {
		if (config.isEnabled() && config.isStopRailDupe()) {
			for (Block block : event.getBlocks()) {
				Material type = block.getType();

				if (config.getRailArray().contains(type)) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onRailPlace(BlockPlaceEvent event) {
		if (config.isEnabled() && config.isStopRailDupe()) {
			Block block = event.getBlock();
			Material type = block.getType();

			if (config.getRailArray().contains(type)) {
				for (BlockFace face : config.getBfArray()) {
					type = block.getRelative(face).getType();

					if (config.getPistonArray().contains(type)) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	//Trying to stop players from deleting end portals
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if (config.isEnabled() && config.isStopEndPortalDeletion()) {
			Block block = event.getBlockClicked().getRelative(event.getBlockFace());

			if (block.getType() == Material.ENDER_PORTAL) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onDispenseEvent(BlockDispenseEvent event) {
		if (config.isEnabled() && config.isStopEndPortalDeletion()) {
			if (event.getBlock().getType() == Material.DISPENSER) {
				Dispenser disp = (Dispenser) event.getBlock().getState().getData();
				Material type = event.getBlock().getRelative(disp.getFacing()).getType();

				if (type == Material.ENDER_PORTAL) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerEnterBed(BlockPlaceEvent event) {
		if (!config.isEnabled() || !config.stopBedBombing()) return;

		Block b = event.getBlock();
		if (!(b.getType() == Material.BED || b.getType() == Material.BED_BLOCK))
			return;

		Environment env = b.getLocation().getWorld().getEnvironment();
		Biome biome = b.getLocation().getBlock().getBiome();
		if (env == Environment.NETHER || env == Environment.THE_END || Biome.HELL == biome || Biome.SKY == biome) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void onStructureGrow(StructureGrowEvent event) {
		if(config.isEnabled() && config.stopTreeWraparound()) {
			int maxY = 0, minY = 257;
			for(BlockState bs : event.getBlocks()) {
				final int y = bs.getLocation().getBlockY();
				maxY = Math.max(maxY, y);
				minY = Math.min(minY, y);
			}
			if(maxY - minY > 240) {
				event.setCancelled(true);
				final Location loc = event.getLocation();
				plugin().log(Level.INFO, String.format("Prevented structure wraparound at %s: %d, %d, %d", 
						loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
			}
		}
	}

	@EventHandler
	public void onTouchBedrock(PlayerInteractEvent event) {
		if(config.isEnabled() && config.maintainFlatBedrock() && event.getClickedBlock() != null 
				&& event.getClickedBlock().getType() == Material.BEDROCK && event.getClickedBlock().getY() > 0) {
			Bukkit.getScheduler().runTask(plugin(), () -> {
				event.getClickedBlock().setType(Material.STONE);
			});
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPearlTeleport(PlayerTeleportEvent event) {
		if(config.isEnabled() && config.fixPearlGlitch() && event.getCause() == TeleportCause.ENDER_PEARL) {
			Location to = event.getTo();
			World world = to.getWorld();

			Block toBlock = to.getBlock();
			Block above = toBlock.getRelative(BlockFace.UP);
			Block below = toBlock.getRelative(BlockFace.DOWN);
			boolean lowerBlockBypass = false;
			double height = 0.0;
			switch (toBlock.getType()) {
			case CHEST:
			case ENDER_CHEST:
				height = 0.875;
				break;
			case STEP:
				lowerBlockBypass = true;
				height = 0.5;
				break;
			case WATER_LILY:
				height = 0.016;
				break;
			case ENCHANTMENT_TABLE:
				lowerBlockBypass = true;
				height = 0.75;
				break;
			case BED:
			case BED_BLOCK:
				break;
			case FLOWER_POT:
			case FLOWER_POT_ITEM:
				height = 0.375;
				break;
			case SKULL:
				height = 0.5;
				break;
			default:
				break;
			}
			switch (below.getType()) {
			case FENCE:
			case FENCE_GATE:
			case NETHER_FENCE:
			case COBBLE_WALL:
				height = 0.5;
				break;
			default:
				break;
			}

			boolean upperBlockBypass = false;
			if (height >= 0.5) {
				Block aboveHeadBlock = above.getRelative(BlockFace.UP);
				if (!aboveHeadBlock.getType().isSolid()) {
					height = 0.5;
				} else {
					upperBlockBypass = true;
				}
			}

			to.setX(Math.floor(to.getX() + 0.5000));
			to.setY(Math.floor(to.getY() + height));
			to.setZ(Math.floor(to.getZ() + 0.5000));

			if (above.getType().isSolid() || (toBlock.getType().isSolid() && !lowerBlockBypass) 
					|| upperBlockBypass) {
				boolean bypass = false;
				if (world.getEnvironment() == Environment.NETHER &&
						to.getBlockY() > 124 && to.getBlockY() < 129) {
					bypass = true;
				}
				if (!bypass) {
					event.setCancelled(true);
				}
			}
		}
	}

	//fixes a small side effect of the above
	BlockFace[] faces = new BlockFace[] {BlockFace.NORTH,BlockFace.SOUTH,BlockFace.EAST,BlockFace.WEST};
	@EventHandler
	public void preventWrongIce(BlockFormEvent event) {
		Block block = event.getBlock();
		if(event.getNewState().getType() == Material.ICE) {
			for(BlockFace face : faces) {
				if(block.getRelative(face).getType().isSolid()) return;
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		if(config.isEnabled() && config.isPreventLongSigns()) {
			String[] signdata = event.getLines();
			for(int i = 0; i < signdata.length; i++) {
				if(signdata[i] != null && signdata[i].length() > config.getSignLengthLimit()) {
					Player player = event.getPlayer();
					Location loc = event.getBlock().getLocation();
					plugin().log(Level.WARNING, String.format("Player '%s' [%s] attempted to place sign at ([%s] %d, %d, %d) with line %d having length %d > %d. Preventing.",
							player.getDisplayName(), player.getUniqueId(), loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
							i, signdata[i], config.getSignLengthLimit()));
					if(config.isCancelLongSignEvent()) {
						event.setCancelled(true);
						return;
					}
					if(config.isPreventLongSignsAbsolute()) {
						event.setLine(i, "");
					} else {
						event.setLine(i, signdata[i].substring(0, config.getSignLengthLimit()));
					}
				}
			}
		}
	}

	public static GameFixesConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new GameFixesConfig(plugin, config);
	}
}