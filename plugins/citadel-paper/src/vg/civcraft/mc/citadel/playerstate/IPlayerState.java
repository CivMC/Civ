package vg.civcraft.mc.citadel.playerstate;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.events.ReinforcementBypassEvent;
import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public abstract class IPlayerState {
	
	protected UUID uuid;
	private boolean bypass;
	
	public IPlayerState(Player p, boolean bypass) {
		if (p == null) {
			throw new IllegalArgumentException("Player for player state can not be null");
		}
		this.uuid = p.getUniqueId();
		this.bypass = bypass;
	}
	
	public abstract void handleBlockPlace(BlockPlaceEvent e);
	
	public abstract void handleInteractBlock(PlayerInteractEvent e);
	
	public void handleBreakBlock(BlockBreakEvent e) {
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(e.getBlock());
		if (rein == null) {
			//no reinforcement, normal break which we dont care about
			return;
		}
		boolean hasAccess = rein.hasPermission(e.getPlayer(), Citadel.bypassPerm);
		if (hasAccess && bypass) {
			ReinforcementBypassEvent bypassEvent = new ReinforcementBypassEvent(e.getPlayer(), rein);
			Bukkit.getPluginManager().callEvent(bypassEvent);
			if (bypassEvent.isCancelled()) {
				e.setCancelled(true);
			}
			return;
		}
		if (bypass) {
			Utility.sendAndLog(e.getPlayer(), ChatColor.GREEN, "You could bypass this reinforcement if you turn bypass mode on with '/ctb'");
		}
		e.setCancelled(true);
		double damage = ReinforcementLogic.getDamageApplied(e.getPlayer(), rein);
		ReinforcementDamageEvent dre = new ReinforcementDamageEvent(e.getPlayer(), rein, damage);
		Bukkit.getPluginManager().callEvent(dre);
		if (dre.isCancelled()) {
			return;
		}
		damage = dre.getDamageDone();
		rein.setHealth(rein.getHealth() - damage);
		rein.getType().getReinforcementEffect().playEffect(rein.getLocation().clone().add(0.5, 0.5, 0.5));
	}

}
