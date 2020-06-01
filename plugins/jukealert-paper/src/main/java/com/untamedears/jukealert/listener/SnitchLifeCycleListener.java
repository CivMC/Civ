package com.untamedears.jukealert.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.untamedears.jukealert.SnitchManager;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchFactoryType;
import com.untamedears.jukealert.model.SnitchTypeManager;
import com.untamedears.jukealert.model.actions.internal.DestroySnitchAction.Cause;
import com.untamedears.jukealert.model.appender.AbstractSnitchAppender;

import vg.civcraft.mc.citadel.events.ReinforcementBypassEvent;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.events.ReinforcementDestructionEvent;
import vg.civcraft.mc.citadel.model.Reinforcement;

public class SnitchLifeCycleListener implements Listener {

	private SnitchTypeManager configManager;
	private SnitchManager snitchManager;
	private Map<Location, SnitchFactoryType> pendingSnitches;
	private Logger logger;

	public SnitchLifeCycleListener(SnitchManager snitchManager, SnitchTypeManager configManager, Logger logger) {
		this.configManager = configManager;
		this.snitchManager = snitchManager;
		this.logger = logger;
		this.pendingSnitches = new HashMap<>();
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		ItemStack inHand = event.getItemInHand();
		SnitchFactoryType type = configManager.getConfig(inHand);
		if (type != null) {
			pendingSnitches.put(event.getBlock().getLocation(), type);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		SnitchFactoryType snitchConfig = pendingSnitches.remove(block.getLocation());
		if (snitchConfig == null) {
			return;
		}
		if (block.getType() == snitchConfig.getItem().getType()) {
			event.setDropItems(false);
			block.getWorld().dropItemNaturally(block.getLocation(), snitchConfig.getItem());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void createReinforcement(ReinforcementCreationEvent e) {
		Location location = e.getReinforcement().getLocation();
		SnitchFactoryType snitchConfig = pendingSnitches.get(location);
		if (snitchConfig == null) {
			return;
		}
		pendingSnitches.remove(location);
		Snitch snitch = snitchConfig.create(-1, location, "", e.getReinforcement().getGroupId(), true);
		Player p = e.getPlayer();
		logger.info(String.format("Created snitch of type %s at %s by %s", snitch.getType().getName(),
				snitch.getLocation().toString(), p != null ? p.getName() : "null"));
		if (p != null) {
			p.sendMessage(String.format("%sCreated %s on group %s at [%d %d %d]", ChatColor.GREEN,
					snitchConfig.getName(), e.getReinforcement().getGroup().getName(), location.getBlockX(), location.getBlockY(),
					location.getBlockZ()));
		}
		snitchManager.addSnitch(snitch);
		snitch.applyToAppenders(AbstractSnitchAppender::postSetup);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void reinforcementDestroyed(ReinforcementDestructionEvent e) {
		Player source;
		if (e.getSource() instanceof Player) {
			source = (Player) e.getSource();
		} else {
			source = null;
		}
		reinforcementGone(e.getReinforcement(), source);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void reinforcementDestroyed(ReinforcementBypassEvent e) {
		reinforcementGone(e.getReinforcement(), e.getPlayer());
	}

	private void reinforcementGone(Reinforcement rein, Player source) {
		Snitch snitch = snitchManager.getSnitchAt(rein.getLocation());
		if (snitch != null) {
			UUID uuid = source != null ? source.getUniqueId() : null;
			String name =  source != null ? source.getName() : "ENVIRONMENT";
			snitch.destroy(uuid, Cause.PLAYER);
			logger.info(String.format("%s destroyed snitch of type %s at %s", name, snitch.getType().getName(),
					snitch.getLocation().toString()));
		}
	}

}
