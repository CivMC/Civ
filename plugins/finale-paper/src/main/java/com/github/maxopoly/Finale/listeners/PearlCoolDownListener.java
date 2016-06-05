package com.github.maxopoly.listeners;

import java.text.DecimalFormat;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.github.maxopoly.Finale;
import com.github.maxopoly.external.CombatTagPlusManager;
import com.github.maxopoly.external.ProtocolLibManager;

import vg.civcraft.mc.civmodcore.util.cooldowns.TickCoolDownHandler;

public class PearlCoolDownListener implements Listener {
	
	private static PearlCoolDownListener instance;
	private TickCoolDownHandler<UUID> cds;
	private CombatTagPlusManager ctpManager;
	private boolean combatTag;
	
	public PearlCoolDownListener(long cooldown, boolean combatTag, CombatTagPlusManager ctpManager) {
		instance = this;
		this.cds = new TickCoolDownHandler<UUID>(Finale.getPlugin(), cooldown); 
		this.ctpManager = ctpManager;
		this.combatTag = combatTag;
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
		ProtocolLibManager plm = Finale.getProtocolLibManager();
		if (plm != null) {
			plm.sendPacketWithCoolDown(cds.getTotalCoolDown(), shooter);
		}
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
