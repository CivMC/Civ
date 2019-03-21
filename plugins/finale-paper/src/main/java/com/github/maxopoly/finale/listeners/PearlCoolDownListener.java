package com.github.maxopoly.finale.listeners;

import java.text.DecimalFormat;
import java.util.Collections;
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
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.external.CombatTagPlusManager;

import vg.civcraft.mc.civmodcore.ui.ActionBarHandler;
import vg.civcraft.mc.civmodcore.ui.UI;
import vg.civcraft.mc.civmodcore.ui.UIHandler;
import vg.civcraft.mc.civmodcore.ui.UIManager;
import vg.civcraft.mc.civmodcore.ui.UIScoreboard;
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
	private boolean useActionBar;

	public PearlCoolDownListener(long cooldown, boolean combatTag, CombatTagPlusManager ctpManager,
			boolean setVanillaCooldown, boolean useSideBar, boolean useActionBar) {
		instance = this;
		this.cds = new TickCoolDownHandler<UUID>(Finale.getPlugin(), cooldown);
		this.ctpManager = ctpManager;
		this.combatTag = combatTag;
		this.setVanillaCooldown = setVanillaCooldown;
		this.useSideBar = useSideBar;
		this.useActionBar = useActionBar;
	}

	public long getCoolDown() {
		return cds.getTotalCoolDown();
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		
		UI ui = UIManager.getUIManager().getScoreboard(player);
		if (useSideBar) {
			ui.getUIHandlers().add(new UIHandler() {
				
				@Override
				public void handle(Player player, UIScoreboard board) {
					if (cds.onCoolDown(player.getUniqueId())) {
						board.add(ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Enderpearl: " + ChatColor.LIGHT_PURPLE + formatCoolDown(player.getUniqueId()) + ChatColor.DARK_PURPLE + "s", 5);
					} else {
						board.remove(5, "");
					}
				}
			});
		}
		if (useActionBar) {
			ui.getActionBarHandlers().add(new ActionBarHandler() {
				
				@Override
				public StringBuilder handle(Player player, StringBuilder sb) {
					if (!cds.onCoolDown(player.getUniqueId())) {
						return sb;
					}
					
					sb.append("   " + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Enderpearl: " + ChatColor.LIGHT_PURPLE + formatCoolDown(player.getUniqueId()) + ChatColor.DARK_PURPLE + "s" + "   ");
					return sb;
				}
			});
		}
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

	private DecimalFormat df = new DecimalFormat("#.#");
	
	private String formatCoolDown(UUID uuid) {
		long cd = cds.getRemainingCoolDown(uuid);
		return df.format((cd / 20.0));
	}

}
