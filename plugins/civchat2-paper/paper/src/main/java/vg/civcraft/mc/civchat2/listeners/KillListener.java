package vg.civcraft.mc.civchat2.listeners;

import java.util.Optional;
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
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;

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
		String msg;
		ItemStack item = killer.getInventory().getItemInMainHand();
		String victimFormattedName = String.format("%s%s", ChatColor.ITALIC, victim.getDisplayName());
		String killerFormattedName = String.format("%s%s", ChatColor.ITALIC, killer.getDisplayName());

		if (item == null || MaterialUtils.isAir(item.getType())) {
			msg = String.format("%s%s %swas killed by %s %sby hand", ChatColor.DARK_GRAY, victimFormattedName,
					ChatColor.DARK_GRAY, killerFormattedName, ChatColor.DARK_GRAY);
		} else {
			String itemName = ItemUtils.getItemName(item);
			String displayName = ItemUtils.getDisplayName(item);
			Boolean hasWordBank = Optional.ofNullable(ItemUtils.getItemMeta(item))
					.map(r -> r.displayName())
					.map(f -> f.children().size() > 0)
					.orElse(false);
			if (!hasWordBank) {
				displayName = String.format("with a %s", itemName);
			} else {
				displayName = String.format("with %s", displayName);
			}

			msg = String.format("%s%s %swas killed by %s %s%s", ChatColor.DARK_GRAY, victimFormattedName,
					ChatColor.DARK_GRAY, killerFormattedName, ChatColor.DARK_GRAY, displayName);
		}
		Location killLoc = victim.getLocation();
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
