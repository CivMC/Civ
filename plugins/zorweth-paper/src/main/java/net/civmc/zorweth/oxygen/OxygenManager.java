package net.civmc.zorweth.oxygen;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import net.civmc.zorweth.ZorwethPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class OxygenManager implements Listener {

    private static final double REGENERATION_PREVENTION_OXYGEN = -0.15;
    public static final NamespacedKey NO_HEALTH_REGEN = new NamespacedKey("finale", "no_health_regen");

    private final NamespacedKey oxygenKey;
    private final Map<Player, Long> lastMessage = new WeakHashMap<>();

    private final Collection<CraftingRecipe> recipes = new ArrayList<>();

    public OxygenManager(ZorwethPlugin plugin, String world, ActivityManager activityManager,
                         Map<ActivityManager.Activity, Double> activityMultiplier, Map<Biome, Double> biomeMultipliers, double baseOxygenConsumptionPerSecond) {
        this.oxygenKey = new NamespacedKey(plugin, "oxygen");

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().getName().equals(world) || player.getGameMode() != GameMode.SURVIVAL) {
                    continue;
                }

                double oxygen = getOxygen(player);

                ActivityManager.Activity activity = activityManager.getActivity(player);
                player.sendMessage("activity -> " + activity);
                double playerActivityMultiplier = activityMultiplier.getOrDefault(activity, 0D);

                double biomeMultiplier = biomeMultipliers.getOrDefault(player.getLocation().getBlock().getBiome(), 0D);

                double loss = playerActivityMultiplier * biomeMultiplier;
                if (loss == 0) {
                    if (activity == ActivityManager.Activity.IDLE) {
                        oxygen += baseOxygenConsumptionPerSecond;
                    } else {
                        oxygen += baseOxygenConsumptionPerSecond / 2;
                    }
                    setOxygen(player, oxygen);
                    continue;
                }

                oxygen -= loss * baseOxygenConsumptionPerSecond;

                setOxygen(player, oxygen);

                applyOxygenEffects(player, oxygen);
            }
        }, 20, 20);

        recipes.add(OxygenBottle.getRecipe(plugin));
        for (CraftingRecipe recipe : recipes) {
            Bukkit.addRecipe(recipe);
        }
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        for (CraftingRecipe recipe : recipes) {
            event.getPlayer().discoverRecipe(recipe.getKey());
        }
    }

    @EventHandler
    public void on(PlayerItemConsumeEvent event) {
        if (!OxygenBottle.isCrudeOxygen(event.getItem())) {
            return;
        }

        Player player = event.getPlayer();
        setOxygen(player, getOxygen(player) + 0.09);
    }

    public static OxygenManager deserialize(ZorwethPlugin plugin, ActivityManager activityManager, ConfigurationSection section) {
        ConfigurationSection activitiesSection = section.getConfigurationSection("activities");
        Map<ActivityManager.Activity, Double> activityMultiplier = new HashMap<>();
        for (String key : activitiesSection.getKeys(false)) {
            activityMultiplier.put(ActivityManager.Activity.valueOf(key.toUpperCase()), activitiesSection.getDouble(key));
        }

        ConfigurationSection biomeSection = section.getConfigurationSection("biomes");
        Map<Biome, Double> biomeMultiplier = new HashMap<>();
        Registry<Biome> biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        for (String key : biomeSection.getKeys(false)) {
            biomeMultiplier.put(biomeRegistry.getOrThrow(NamespacedKey.fromString(key)), biomeSection.getDouble(key));
        }

        return new OxygenManager(
            plugin,
            section.getString("world"),
            activityManager,
            activityMultiplier,
            biomeMultiplier,
            section.getDouble("consumption_per_second")
        );
    }

    private void applyOxygenEffects(Player player, double oxygen) {
        if (oxygen < REGENERATION_PREVENTION_OXYGEN) {
            player.getPersistentDataContainer().set(NO_HEALTH_REGEN, PersistentDataType.BOOLEAN, true);
        } else {
            player.getPersistentDataContainer().remove(NO_HEALTH_REGEN);
        }

        if (oxygen > 0) {
            return;
        } else if (oxygen > -0.05) {
            sendDebouncedMessage(player, Component.text("You feel out of breath.", NamedTextColor.RED, TextDecoration.BOLD));
        } else if (oxygen > -0.15) {
            sendDebouncedMessage(player, Component.text("You are beginning to suffocate. Seek oxygen immediately.", NamedTextColor.RED, TextDecoration.BOLD));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 0, true, false));
        } else if (oxygen > -0.3) {
            sendDebouncedMessage(player, Component.text("Hypoxia is preventing you from regenerating.", NamedTextColor.DARK_RED, TextDecoration.BOLD));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 1, true, false));

            player.setHealth(Math.max(0.0, player.getHealth() - 0.1));
        } else if (oxygen > -0.5) {
            sendDebouncedMessage(player, Component.text("Low oxygen is bringing you to the brink of death.", NamedTextColor.DARK_RED, TextDecoration.BOLD));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, true, false));

            player.setHealth(Math.max(0.0, player.getHealth() - 0.25));
        } else {
            sendDebouncedMessage(player, Component.text("You are collapsing due to lack of oxygen.", NamedTextColor.DARK_RED, TextDecoration.BOLD));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 4, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 4, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 4, true, false));

            player.setHealth(Math.max(1.0, player.getHealth() - 1));
        }
    }

    @EventHandler
    public void on(PlayerPostRespawnEvent event) {
        event.getPlayer().getPersistentDataContainer().set(oxygenKey, PersistentDataType.DOUBLE, 1D);
    }

    public double getOxygen(Player player) {
        return Objects.requireNonNullElse(player.getPersistentDataContainer().get(oxygenKey, PersistentDataType.DOUBLE), 1D);
    }

    public void setOxygen(final Player player, final double oxygen) {
        player.getPersistentDataContainer().set(oxygenKey, PersistentDataType.DOUBLE, Math.clamp(oxygen, -1, 1));
    }

    private void sendDebouncedMessage(Player player, Component message) {
        Long lastMessageTime = lastMessage.get(player);
        if (lastMessageTime != null && lastMessageTime + 30_000 > System.currentTimeMillis()) {
            return;
        }

        player.sendMessage(message);
        lastMessage.put(player, System.currentTimeMillis());
    }
}
