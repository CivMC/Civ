package vg.civcraft.mc.civchat2.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civchat2.utility.CivChat2Config;
import vg.civcraft.mc.civchat2.utility.CivChat2SettingsManager;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.ItemNames;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;

public class KillListener implements Listener {

	private CivChat2SettingsManager settingsMan;
	private CivChat2Config config;
	private CivChatDAO dao;

	public KillListener(CivChat2Config config, CivChatDAO dao, CivChat2SettingsManager settingsMan) {
		this.config = config;
		this.dao = dao;
		this.settingsMan = settingsMan;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKill(PlayerDeathEvent event) {
		Player victim = event.getEntity();
		if (victim.getKiller() == null) {
			return;
		}
		Player killer = victim.getKiller();
		if (!settingsMan.getSendOwnKills(killer.getUniqueId())) {
			return;
		}
		String itemDescriptor;
		ItemStack item = killer.getInventory().getItemInMainHand();
		if (item == null || MaterialAPI.isAir(item.getType())) {
			itemDescriptor = "by hand";
		}
		else {
			String displayName = ItemAPI.getDisplayName(item);
			if (displayName == null) {
				itemDescriptor = "with " + ItemNames.getItemName(item);
			}
			else {
				itemDescriptor = "with " + displayName;
			}
		}
		Location killLoc = victim.getLocation();
		String msg = String.format("%s%s was killed by %s%s %s", victim.getDisplayName(), ChatColor.GOLD, killer.getDisplayName(),
				ChatColor.GOLD, itemDescriptor);
		for (Player p : Bukkit.getOnlinePlayers()) {
			Location loc = p.getLocation();
			if (!loc.getWorld().equals(killLoc.getWorld())) {
				continue;
			}
			if (loc.distance(killLoc) > config.getKillBroadcastRange()) {
				continue;
			}
			if (!settingsMan.getReceiveKills(p.getUniqueId())) {
				continue;
			}
			if (!settingsMan.getReceiveKillsFromIgnored(p.getUniqueId())
					&& dao.isIgnoringPlayer(p.getUniqueId(), killer.getUniqueId())) {
				continue;
			}
			p.sendMessage(msg);
		}
	}

}
