package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.buildlimit.BuildLimitsConfig;
import com.programmerdan.minecraft.simpleadminhacks.configs.buildlimit.LimitType;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.BuildLimit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
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
	public void onEnable() {
		plugin.registerListener(this);
	}

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlaceEvent(BlockPlaceEvent e) {
		if(!withinLimits(e.getBlock().getLocation())) {
			e.setCancelled(true);
            e.getPlayer().sendMessage(Component.text("You cannot place blocks here", NamedTextColor.RED));
		}
	}

	private boolean withinLimits(Location loc) {
		for (BuildLimit limit : config.getBuildLimits()) {
            if(limit == null) continue;

			if(!limit.world().equals(loc.getWorld().getName())) {
				continue;
			}

            if(LimitType.valueOf(limit.type()) == LimitType.ALTITUDE) {
                if(loc.getBlock().getY() < limit.maxY() && loc.getBlock().getY() > limit.minY()) {
                    continue;
                }
                return false;
            }
		}

		return true;
	}
}


