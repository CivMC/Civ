package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.dre.brewery.api.events.IngedientAddEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SafeAddBreweryIngredient extends BasicHack {

    private BooleanSetting setting;

    private Cache<UUID, Long> playerTimestampCache;

    public SafeAddBreweryIngredient(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("BreweryX")) {
            super.onEnable();

            playerTimestampCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(5, TimeUnit.SECONDS)
                    .build();

            MenuSection mainMenu = plugin.getSettingManager().getMainMenu();

            setting = new BooleanSetting(plugin, false,
                    "Safely Add Brewery Ingredients",
                    "safe_add_brewery_ingredient",
                    "Prevents you from accidentally emptying a cauldron after adding a Brewery ingredient.");

            PlayerSettingAPI.registerSetting(setting, mainMenu);
        } else {
            plugin.getLogger().severe("SafeAddBreweryIngredient hack requires BreweryX to be installed and enabled.");
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreweryIngredientAdd(IngedientAddEvent event) {
        Player player = event.getPlayer();
        if (!setting.getValue(player.getUniqueId())) {
            return; // Not enabled for this player.
        }

        long now = System.currentTimeMillis();

        // Update the timestamp for this player.
        playerTimestampCache.put(player.getUniqueId(), now);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!setting.getValue(player.getUniqueId())) {
            return; // Not enabled for this player.
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return; // Only handle right-click interactions.
        }

        if (event.getClickedBlock() != null && event.getClickedBlock().getType() != Material.WATER_CAULDRON) {
            return; // Only handle interactions with water cauldrons.
        }

        if (!List.of(Material.GLASS_BOTTLE, Material.WATER_BUCKET, Material.BUCKET).contains(event.getMaterial())) {
            return; // Only handle interactions with specific items.
        }

        // Check if the player has added an ingredient recently.
        Long lastAdded = playerTimestampCache.getIfPresent(player.getUniqueId());
        long now = System.currentTimeMillis();

        if (lastAdded != null && now - lastAdded < 5000) {
            // If they added an ingredient within the last 5 seconds, cancel the event.
            event.setCancelled(true);
            player.sendMessage(Component.text("You cannot empty the cauldron so soon after adding an ingredient.", NamedTextColor.RED));
            return;
        }
    }
}
