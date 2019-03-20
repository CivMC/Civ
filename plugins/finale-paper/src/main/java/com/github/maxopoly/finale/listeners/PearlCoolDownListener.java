package com.github.maxopoly.finale.listeners;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.external.CombatTagPlusManager;

import vg.civcraft.mc.civmodcore.scoreboard.CivScoreBoard;
import vg.civcraft.mc.civmodcore.scoreboard.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.util.cooldowns.TickCoolDownHandler;

public class PearlCoolDownListener implements Listener {

	private static PearlCoolDownListener instance;

	public static long getPearlCoolDown(UUID uuid) {
		if (instance == null) {
			return -1;
		}
		return instance.cds.getRemainingCoolDown(uuid);
	}

	private TickCoolDownHandler<UUID> cds;
	private CombatTagPlusManager ctpManager;
	private boolean combatTag;
	private boolean setVanillaCooldown;
	private boolean useSideBar;
	private CivScoreBoard scoreBoard;
	private Set<UUID> onCooldown;

	public PearlCoolDownListener(long cooldown, boolean combatTag, CombatTagPlusManager ctpManager,
			boolean setVanillaCooldown, boolean useSideBar) {
		instance = this;
		this.cds = new TickCoolDownHandler<UUID>(Finale.getPlugin(), cooldown);
		this.ctpManager = ctpManager;
		this.combatTag = combatTag;
		this.setVanillaCooldown = setVanillaCooldown;
		this.useSideBar = useSideBar;
		if (useSideBar) {
			onCooldown = Collections.synchronizedSet(new TreeSet<>());
			scoreBoard = ScoreBoardAPI.createBoard("finalePearlCoolDown");
			Bukkit.getScheduler().scheduleSyncRepeatingTask(Finale.getPlugin(), () -> {
				Iterator<UUID> iter = onCooldown.iterator();
				while (iter.hasNext()) {
					UUID uuid = iter.next();
					Player p = Bukkit.getPlayer(uuid);
					if (!cds.onCoolDown(uuid)) {
						iter.remove();
						if (p != null) {
							scoreBoard.hide(p);
						}
						continue;
					}
					if (p != null) {
						scoreBoard.set(p, ChatColor.LIGHT_PURPLE + "Pearl: " + formatCoolDown(uuid) + " sec");
					}
				}

			}, 1L, 1L);
		}
	}

	public long getCoolDown() {
		return cds.getTotalCoolDown();
	}

	@EventHandler
	public void pearlThrow(ProjectileLaunchEvent e) {
		// ensure it's a pearl
		if (e.getEntityType() != EntityType.ENDER_PEARL) {
			return;
		}
		// ensure a player threw it
		if (!(e.getEntity().getShooter() instanceof Player)) {
			return;
		}
		Player shooter = (Player) e.getEntity().getShooter();
		// check whether on cooldown
		if (cds.onCoolDown(shooter.getUniqueId())) {
			e.setCancelled(true);
			shooter.sendMessage(
					ChatColor.RED + "You may pearl again in " + formatCoolDown(shooter.getUniqueId()) + " seconds");
			return;
		}
		// tag player if desired
		if (combatTag && ctpManager != null) {
			ctpManager.tag((Player) e.getEntity().getShooter(), null);
		}
		if (useSideBar) {
			onCooldown.add(shooter.getUniqueId());
		}
		// put pearl on cooldown
		cds.putOnCoolDown(shooter.getUniqueId());
		if (setVanillaCooldown) {
			Bukkit.getScheduler().runTaskLater(Finale.getPlugin(), new Runnable() {
				@Override
				public void run() {
					// -1, because this is delayed by one tick
					shooter.setCooldown(Material.ENDER_PEARL, (int) cds.getTotalCoolDown() - 1);
				}
			}, 1);
		}
	}

	private String formatCoolDown(UUID uuid) {
		long cd = cds.getRemainingCoolDown(uuid);
		DecimalFormat df = new DecimalFormat("0.0");
		return df.format((cd / 20.0));
	}

}
