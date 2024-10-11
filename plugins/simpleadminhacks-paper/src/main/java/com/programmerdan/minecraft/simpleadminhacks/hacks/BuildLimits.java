package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.BuildLimitsConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.BuildLimit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class BuildLimits extends SimpleHack<BuildLimitsConfig> implements Listener {
	public BuildLimits(SimpleAdminHacks plugin, BuildLimitsConfig config) {
		super(plugin, config);
	}

	public static BuildLimitsConfig generate (
			final @NotNull SimpleAdminHacks plugin,
			final @NotNull ConfigurationSection config){
		return new BuildLimitsConfig(plugin, config);
	}

	@Override
	public void onEnable(){
		plugin.registerListener(this);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlaceEvent(BlockPlaceEvent e){
		if(!config.isEnabled()) return;

		Player player = e.getPlayer();
		Location loc = e.getBlock().getLocation();

		if(!withinLimits(loc)){
			e.setCancelled(true);
			player.sendMessage(Component.text().content("You cannot place blocks here").color(NamedTextColor.RED));
		}
	}

	private boolean withinLimits(Location loc){
		BuildLimit[] limits = config.getBuildLimits();

		for (BuildLimit limit : limits){
			if(!limit.getWorld().equals(loc.getWorld().getName())){
				continue;
			}

            if(limit.getType().equals("altitude")){
                if(loc.getBlock().getY() < limit.getMax_y() && loc.getBlock().getY() > limit.getMin_y()) {
                    continue;
                }
                return false;
            }
		}

		return true;
	}
}
