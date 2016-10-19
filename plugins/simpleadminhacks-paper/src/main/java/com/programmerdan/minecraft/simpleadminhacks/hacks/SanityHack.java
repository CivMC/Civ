package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.SanityHackConfig;

public class SanityHack extends SimpleHack<SanityHackConfig> implements Listener {

	public static final String NAME = "SanityHack";
	
	private final int trackingLevel;
	private int caughtPlayers; // Just to give a useful message in status tbh
	
	public SanityHack(SimpleAdminHacks plugin, SanityHackConfig config) {
		super(plugin, config);
		this.trackingLevel = config.getTrackingLevel();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event){
		if(!config.isEnabled()) return;
		if(!config.isTrackingBreak()) return;
		
		
		Block block = event.getBlock();
		Player player = event.getPlayer();

		if (player == null) return;
		if (block == null) return;
		
		if(block.getY() < trackingLevel){
			plugin().log(Level.INFO, "Player({0}, {1}) caught breaking a block({2}) below Y {3}", player.getName(), player.getUniqueId().toString(), 
					block.getType().toString(), trackingLevel);
			caughtPlayers++;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
	public void onBlockPlace(BlockPlaceEvent event){
		if(!config.isEnabled()) return;
		if(!config.isTrackingPlace()) return;
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		if (player == null) return;
		if (block == null) return;

		if(block.getY() < trackingLevel && player != null){
			plugin().log(Level.INFO, "Player({0}, {1}) caught placing a block({2}) below Y {3}", player.getName(), player.getUniqueId().toString(), 
					block.getType().toString(), trackingLevel);
			caughtPlayers++;
		}
	}
	
	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering SanityHack listeners");
			plugin().registerListener(this);
		}
	}

	@Override
	public void registerCommands() {
	}

	@Override
	public void dataBootstrap() {
		caughtPlayers = 0;
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		caughtPlayers = 0;
	}

	@Override
	public String status() {
		if (!config.isEnabled()) {
			return "Sanity Hack listening disabled.";
		}
		
		return caughtPlayers + " caught below Y " + trackingLevel;
	}
	
	public static SanityHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new SanityHackConfig(plugin, config);
	}

}
	
