package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;

public class DogFacts extends BasicHack {

	@AutoLoad
	private List<String> announcements;
	@AutoLoad
	private String intervalTime;
	private BooleanSetting disableAnnouncements;
	private int counter = 0;

	public DogFacts(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		startRunnable(announcements);
		initSettings();
	}

	public void startRunnable(List<String> announcements){
		long interval = ConfigHelper.parseTimeAsTicks(intervalTime);
		int tickOffset = (int) (Math.random() * (interval));
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin ,() -> {
			if (announcements.isEmpty()) {
				return;
			}
			if (counter >= announcements.size()) {
				counter = 0;
			}
			String message = announcements.get(counter);
			plugin.info("Broadcasting DogFact #" + counter);
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (getDisableAnnouncements(player.getUniqueId())){
					continue;
				}
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getDisplayName() + " " + message);
			}
			counter++;
		}, tickOffset, interval);
	}

	private void initSettings() {
		MenuSection menu = PlayerSettingAPI.getMainMenu()
				.createMenuSection("Announcement Settings", "Settings relating to Announcements", new ItemStack(
						Material.OAK_SIGN));
		disableAnnouncements =
				new BooleanSetting(plugin, false, "Disable Announcements", "disableAnnouncements",
						"Disable Announcements?");
		PlayerSettingAPI.registerSetting(disableAnnouncements, menu);
	}

	public boolean getDisableAnnouncements(UUID uuid) {
		return disableAnnouncements.getValue(uuid);
	}
}
