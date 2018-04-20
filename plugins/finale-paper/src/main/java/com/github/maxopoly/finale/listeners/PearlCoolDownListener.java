package com.github.maxopoly.finale.listeners;

import java.text.DecimalFormat;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.external.CombatTagPlusManager;

import vg.civcraft.mc.civmodcore.util.cooldowns.TickCoolDownHandler;

public class PearlCoolDownListener implements Listener {
	
	private static PearlCoolDownListener instance;
	private TickCoolDownHandler<UUID> cds;
	private CombatTagPlusManager ctpManager;
	private boolean combatTag;
	private boolean refundPearl;
	
	public PearlCoolDownListener(long cooldown, boolean combatTag, CombatTagPlusManager ctpManager, boolean refundPearl) {
		instance = this;
		this.cds = new TickCoolDownHandler<UUID>(Finale.getPlugin(), cooldown); 
		this.ctpManager = ctpManager;
		this.combatTag = combatTag;
		this.refundPearl = refundPearl;
	}
	
	@EventHandler
	public void pearlThrow(ProjectileLaunchEvent e) {
		//ensure it's a pearl
		if (e.getEntityType() != EntityType.ENDER_PEARL) {
			return;
		}
		//ensure a player threw it
		if (!(e.getEntity().getShooter() instanceof Player)) {
			return;
		}
		Player shooter = (Player) e.getEntity().getShooter();
		//check whether on cooldown
		if (cds.onCoolDown(shooter.getUniqueId())) {
			long cd = cds.getRemainingCoolDown(shooter.getUniqueId());
			e.setCancelled(true);
			DecimalFormat df = new DecimalFormat("#.##");
			shooter.sendMessage(ChatColor.RED + "You may pearl again in "
					+ df.format(((double) cd / 20.0))+  " seconds");
			return;
		}
		//tag player if desired
		if (combatTag && ctpManager != null) {
			ctpManager.tag((Player) e.getEntity().getShooter(), null);
		}
		//put pearl on cooldown		
		cds.putOnCoolDown(shooter.getUniqueId());
		
		Bukkit.getScheduler().runTaskLater(Finale.getPlugin(), new Runnable() {
			public void run() {
				shooter.setCooldown(Material.ENDER_PEARL, (int) cds.getTotalCoolDown());
			}
		}, 1);
	}
	
	public static long getPearlCoolDown(UUID uuid) {
		if (instance == null) {
			return -1;
		}
		return instance.cds.getRemainingCoolDown(uuid);
	}
	
	public long getCoolDown() {
		return cds.getTotalCoolDown();
	}

}
