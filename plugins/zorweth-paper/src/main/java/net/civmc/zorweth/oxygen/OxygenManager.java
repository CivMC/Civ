package net.civmc.zorweth.oxygen;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.dre.brewery.api.events.brew.BrewDrinkEvent;
import com.dre.brewery.recipe.BRecipe;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import net.civmc.zorweth.ZorwethPlugin;
import net.civmc.zorweth.oxygen.ActivityManager.Activity;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class OxygenManager implements Listener {

    static final String OXYGEN_BREW_RECIPE_NAME = "Oxygen";
    private static final double CRUDE_OXYGEN_AMOUNT = 0.09;
    static final double OXYGEN_BREW_AMOUNT = 1.6;
    public static final double DEFAULT_MAX_OXYGEN = 1;
    private static final double REGENERATION_PREVENTION_OXYGEN = -0.15;
    public static final NamespacedKey NO_HEALTH_REGEN = new NamespacedKey("finale", "no_health_regen");

    private final NamespacedKey oxygenKey;
    private final Map<Biome, Double> biomeMultipliers;
    private final Map<Player, Long> lastMessage = new WeakHashMap<>();
    private final OxygenBladderMechanics oxygenBladderMechanics = new OxygenBladderMechanics();

    private final Collection<CraftingRecipe> recipes = new ArrayList<>();

    public OxygenManager(ZorwethPlugin plugin, String world, ActivityManager activityManager,
                          Map<ActivityManager.Activity, Double> activityMultiplier, Map<Biome, Double> biomeMultipliers, double baseOxygenConsumptionPerSecond) {
        this.oxygenKey = new NamespacedKey(plugin, "oxygen");
        this.biomeMultipliers = biomeMultipliers;

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().getName().equals(world) || player.getGameMode() != GameMode.SURVIVAL) {
                    continue;
                }

                double oxygen = getOxygen(player);

                Set<ActivityManager.Activity> activities = activityManager.getActivities(player);
                Activity activity = activities
                    .stream()
                    .max(Comparator.comparingDouble(activityMultiplier::get))
                    .orElseThrow();
                double playerActivityMultiplier = activityMultiplier.get(activity);

                double biomeMultiplier = biomeMultipliers.getOrDefault(player.getLocation().getBlock().getBiome(), 0D);

                if (oxygen < REGENERATION_PREVENTION_OXYGEN) {
                    player.getPersistentDataContainer().set(NO_HEALTH_REGEN, PersistentDataType.BOOLEAN, true);
                } else {
                    player.getPersistentDataContainer().remove(NO_HEALTH_REGEN);
                }

                double loss = playerActivityMultiplier * biomeMultiplier;
                if (loss == 0) {
                    if (activities.contains(ActivityManager.Activity.IDLE)) {
                        oxygen += baseOxygenConsumptionPerSecond;
                    } else {
                        oxygen += baseOxygenConsumptionPerSecond / 2;
                    }
                    setOxygen(player, oxygen);
                    continue;
                }

                drainOxygen(player, loss * baseOxygenConsumptionPerSecond, baseOxygenConsumptionPerSecond / 2, activity);
                applyOxygenEffects(player, getOxygen(player));
            }
        }, 20, 20);

        recipes.add(OxygenBottle.getRecipe(plugin));
        recipes.add(OxygenBladder.getRecipe(plugin));
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
        Player player = event.getPlayer();
        if (OxygenBottle.isCrudeOxygen(event.getItem())) {
            addOxygen(player, CRUDE_OXYGEN_AMOUNT);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(final BrewDrinkEvent event) {
        final BRecipe recipe = event.getBrew().getCurrentRecipe();
        if (recipe == null || !OXYGEN_BREW_RECIPE_NAME.equals(recipe.getRecipeName())) {
            return;
        }

        final Player player = event.getPlayer();
        addOxygen(player, OXYGEN_BREW_AMOUNT * (event.getQuality() / 10D));
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

    public boolean hasOxygen(final Biome biome) {
        return this.biomeMultipliers.getOrDefault(biome, 0D) == 0D;
    }

    private void addOxygen(final Player player, final double amount) {
        setOxygen(player, getOxygen(player) + amount);
    }

    private void drainOxygen(final Player player, final double amount, final double refillRate, Activity activity) {
        final double oxygen = getOxygen(player);
        final double remaining = this.oxygenBladderMechanics.drainOxygen(player, amount, CRUDE_OXYGEN_AMOUNT,
            oxygen <= OxygenBladderMechanics.CONSUME_ITEM_PLAYER_OXYGEN_THRESHOLD, activity);
        if (remaining > 0) {
            setOxygen(player, oxygen - remaining);
        }

        final double updatedOxygen = getOxygen(player);
        final double refill = this.oxygenBladderMechanics.refillPlayerOxygen(player, refillRate,
            CRUDE_OXYGEN_AMOUNT, updatedOxygen);
        if (refill > 0) {
            setOxygen(player, updatedOxygen + refill);
        }
    }

    public void setOxygen(final Player player, final double oxygen) {
        player.getPersistentDataContainer().set(oxygenKey, PersistentDataType.DOUBLE,
            Math.clamp(oxygen, -1, OxygenBladder.getMaxOxygen(player)));
    }

    private void sendDebouncedMessage(Player player, Component message) {
        Long lastMessageTime = lastMessage.get(player);
        if (lastMessageTime != null && lastMessageTime + 15_000 > System.currentTimeMillis()) {
            return;
        }

        player.sendMessage(message);
        lastMessage.put(player, System.currentTimeMillis());
    }
}
