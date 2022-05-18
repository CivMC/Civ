package com.biggestnerd.namecolors;



import me.neznamy.tab.api.TabAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;

public class NameColors extends ACivMod implements Listener {

	private static NameColors instance;

	private static final ChatColor[] rainbow = { ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN,
			ChatColor.DARK_AQUA, ChatColor.AQUA, ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE };

	public static NameColors getInstance() {
		return instance;
	}

	public static String rainbowify(String text) {
		StringBuilder nameBuilder = new StringBuilder();
		char[] letters = text.toCharArray();
		for (int i = 0; i < letters.length; i++) {
			nameBuilder.append(rainbow[i % rainbow.length]).append(letters[i]);
		}
		return nameBuilder.toString();
	}

	private NameColorSetting setting;

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
		setting = new NameColorSetting(this);
		PlayerSettingAPI.registerSetting(setting, PlayerSettingAPI.getMainMenu());
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		updatePlayerName(event.getPlayer(), setting.getValue(event.getPlayer()));
	}

	public void updatePlayerName(Player player, ChatColor color) {
		if (color == null || color == ChatColor.RESET) {
			CivChat2.getInstance().getCivChat2Manager().removeCustomName(player.getUniqueId());
			if(getServer().getPluginManager().isPluginEnabled("TAB")) {
				//TAB is enabled, so lets reset the player name.
				Bukkit.getScheduler().runTaskLater(this, () -> TabAPI.getInstance().getTablistFormatManager().resetName(TabAPI.getInstance()
						.getPlayer(player.getUniqueId())), 20l );
			}
		} else {
			if (color == NameColorSetting.RAINBOW_COLOR) {
				CivChat2.getInstance().getCivChat2Manager().setCustomName(player.getUniqueId(),
						rainbowify(player.getName()) + ChatColor.RESET);
				if(getServer().getPluginManager().isPluginEnabled("TAB")) {
					//TAB enabled, so now we need to re-apply this name as a "custom name"
					//Side note: we do this temporarily so players if they lose their permission don't keep their colored name in TAB.
					Bukkit.getScheduler().runTaskLater(this, () -> TabAPI.getInstance().getTablistFormatManager().setName(TabAPI.getInstance()
							.getPlayer(player.getUniqueId()), rainbowify(player.getName())), 20);
				}
				return;
			}
			CivChat2.getInstance().getCivChat2Manager().setCustomName(player.getUniqueId(),
					color + player.getName() + ChatColor.RESET);
			//Same deal as for rainbow
			if(getServer().getPluginManager().isPluginEnabled("TAB")) {
				//We delay just in-case TAB hasn't loaded the player into memory yet.
				Bukkit.getScheduler().runTaskLater(this, () -> TabAPI.getInstance().getTablistFormatManager().setName(TabAPI.getInstance()
						.getPlayer(player.getUniqueId()),color + player.getName() + ChatColor.RESET), 20);
			}
		}
	}
}
