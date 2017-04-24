package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.badbones69.crazycrates.api.PlayerPrizeEvent;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.CrazyCratesAnnounceConfig;

/**
 * Ties into CrazyCrates, listens for {@link PlayerPrizeEvent}
 * 
 * @author ProgrammerDan
 */
public class CrazyCratesAnnounce extends SimpleHack<CrazyCratesAnnounceConfig> implements Listener{
	
	public static final String NAME = "CrazyCratesAnnounce";
	
	public CrazyCratesAnnounce(SimpleAdminHacks plugin, CrazyCratesAnnounceConfig config) {
		super(plugin, config);

		if (!plugin.serverHasPlugin("CrazyCrates")){
			plugin.log("CrazyCrates not found, disabling broadcast hook.");
			config.setEnabled(false);
		}
	}
	
	
	
	/**
	 * Capture player combattag and broadcast it
	 * 
	 * @param event
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPrizeEvent(PlayerPrizeEvent event) {
		if (!config.isEnabled()) return; // ignore if off
		
		StringBuilder sb = new StringBuilder();
		sb.append("CrazyCrates opening crate ").append(event.getCrateName()).append(" [").append(event.getCrateType()).append("] for ");
		if (event.getPlayer() != null) {
			sb.append(event.getPlayer().getName());
		} else {
			sb.append("Unknown");
		}
		if (event.getPrize() != null) {
			sb.append(" holding ").append(event.getPrize().getName());
		} else {
			sb.append(" holding unnamed with empty");
			return;
		}

		sb.append(" with");
		
		if (event.getPrize().getItems() != null && !event.getPrize().getItems().isEmpty()) {
			for (ItemStack item : event.getPrize().getItems()) {
				sb.append(" ").append(item.getType());
				if (item.getAmount() > 1) {
					sb.append("x").append(item.getAmount());
				}
				if (item.hasItemMeta()) {
					ItemMeta meta = item.getItemMeta();
					if (meta.hasDisplayName()) {
						sb.append(" {").append(meta.getDisplayName()).append("}");
					}
					if (meta.hasLore()) {
						sb.append(" ");
						for (String lore : meta.getLore()) {
							sb.append(" [").append(lore).append("]");
						}
					}
					if (meta.hasEnchants()) {
						sb.append(" Ench");
					}
				}
			}
			
		} else {
			sb.append(" empty");
		}
		
		plugin().log(Level.INFO, sb.toString());
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering CrazyCrates listener");
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
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
	}

	@Override
	public String status() {
		if (config != null && config.isEnabled()) {
			return "CrazyCrates.PlayerPrizeEvent monitoring active";
		} else {
			return "CrazyCrates.PlayerPrizeEvent monitoring not active";
		}
	}
	
	public static CrazyCratesAnnounceConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new CrazyCratesAnnounceConfig(plugin, config);
	}
}
