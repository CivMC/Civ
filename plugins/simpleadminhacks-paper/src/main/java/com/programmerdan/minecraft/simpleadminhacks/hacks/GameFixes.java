package com.programmerdan.minecraft.simpleadminhacks.hacks;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Dispenser;
import org.bukkit.material.Hopper;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.GameFixesConfig;

import java.util.HashMap;
import java.util.HashSet;

public class GameFixes extends SimpleHack<GameFixesConfig> implements Listener {
	public static final String NAME = "GameFixes";
	
	public GameFixes(SimpleAdminHacks plugin, GameFixesConfig config) {
		super(plugin, config);
	}

	@Override
	public void registerListeners() {
		if(config != null && config.isEnabled()) {
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
		if(config != null && config.isEnabled()) {
			genStatus.append(ChatColor.GREEN).append("active\n").append(ChatColor.RESET);
			if(config.isBlockElytraBreakBug()) {
				genStatus.append("   Block elytra break bug is ").append(ChatColor.GREEN).append("enabled\n")
					.append(ChatColor.RESET);
				genStatus.append("   Will deal " + config.getDamageOnElytraBreakBug() + " damage to players\n");
			} else {
				genStatus.append("   Block elytra break bug is ").append(ChatColor.RED).append("disabled\n")
					.append(ChatColor.RESET);
			}
			if(!config.canStorageTeleport()) {
				genStatus.append("   Block storage entities from teleporting to prevents exploits ")
					.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append("   Block storage entities from teleporting to prevents exploits ")
					.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
			if(config.isStopHopperDupe()) {
				genStatus.append("   Hopper self-feed duplication exploit fix ")
					.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
			} else {
				genStatus.append("   Hopper self-feed duplication exploit fix ")
					.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
			}
		} else {
			genStatus.append(ChatColor.RED).append("inactive").append(ChatColor.RESET);
		}
		return genStatus.toString();
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!config.isEnabled() || !config.isBlockElytraBreakBug()) return;
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (block == null || player == null) return;
		
		if(!player.getLocation().equals(block.getLocation())
				&& player.getEyeLocation().getBlock().getType() != Material.AIR) {
			event.setCancelled(true);
			player.damage(config.getDamageOnElytraBreakBug());
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onEntityTeleport(EntityTeleportEvent event) {
		if (!config.isEnabled() || config.canStorageTeleport()) return;
		if (event.getEntity() instanceof InventoryHolder) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onEntityPortal(EntityPortalEvent event) {
		if (!config.isEnabled() || config.canStorageTeleport()) return;
		if (event.getEntity() instanceof InventoryHolder) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		if (!config.isEnabled() || !config.isStopHopperDupe()) return;
		if((event.getDestination() == null) || (event.getSource() == null) ||
				!(InventoryType.HOPPER.equals(event.getDestination().getType())) ||
				!(InventoryType.HOPPER.equals(event.getSource().getType()))) return;
		Hopper source = (Hopper) event.getSource().getLocation().getBlock().getState().getData();
		Hopper dest = (Hopper) event.getDestination().getLocation().getBlock().getState().getData();
		if(source.getFacing().getOppositeFace() == dest.getFacing()) {
			//They're pointing into each other and will eventually dupe
			event.setCancelled(true);
		}
		
	}

	//This is to stop rail dupe glitch via pistons
	private static final BlockFace[] faces_ = new BlockFace[]
	{
		BlockFace.NORTH,
		BlockFace.SOUTH,
		BlockFace.EAST,
		BlockFace.WEST,
		BlockFace.UP,
		BlockFace.DOWN
	};

	private static final Material[] stopDupes_ = new Material[]
	{
		Material.RAILS,
		Material.ACTIVATOR_RAIL,
		Material.DETECTOR_RAIL,
		Material.POWERED_RAIL,
		Material.CARPET
	};

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPistonPushRail(BlockPistonExtendEvent event)
	{
		if(config.isStopRailDupe())
		{
			for (Block block : event.getBlocks())
			{
				Material type = block.getType();
				for (Material mat : stopDupes_)
				{
					if (type == mat)
					{
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onRailPlace(BlockPlaceEvent event)
	{
		if(config.isStopRailDupe())
		{
			Block block = event.getBlock();
			Material type = block.getType();

			for (Material mat : stopDupes_)
			{
				if (type == mat)
				{
					for (BlockFace face : faces_)
					{
						type = block.getRelative(face).getType();

						if(type == Material.PISTON_BASE ||
								type == Material.PISTON_EXTENSION ||
								type == Material.PISTON_MOVING_PIECE ||
								type == Material.PISTON_STICKY_BASE)
						{
							event.setCancelled(true);
							return;
						}
					}
				}
			}
		}
	}

	//Trying to stop players from deleting end portals
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		if(config.isStopEndPortalDeletion())
		{
			Block block = event.getBlockClicked().getRelative(event.getBlockFace());

			if(block.getType() == Material.ENDER_PORTAL)
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDispenseEvent(BlockDispenseEvent event)
	{
		if(config.isStopEndPortalDeletion())
		{
			if(event.getBlock().getType() == Material.DISPENSER)
			{
				Dispenser disp = (Dispenser)event.getBlock().getState().getData();
				Material type = event.getBlock().getRelative(disp.getFacing()).getType();

				if(type == Material.ENDER_PORTAL)
				{
					event.setCancelled(true);
				}

			}
		}
	}

	public static GameFixesConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new GameFixesConfig(plugin, config);
	}
}