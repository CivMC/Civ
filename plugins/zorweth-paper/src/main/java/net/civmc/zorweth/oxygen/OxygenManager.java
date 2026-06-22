package net.civmc.zorweth.oxygen;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.dre.brewery.api.events.brew.BrewDrinkEvent;
import com.dre.brewery.recipe.BRecipe;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.text.DecimalFormat;
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
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class OxygenManager implements Listener {

    static final String OXYGEN_BREW_RECIPE_NAME = "Oxygen";
    private static final double CRUDE_OXYGEN_AMOUNT = 0.09;
    static final double OXYGEN_BREW_AMOUNT = 2.2;
    public static final double DEFAULT_MAX_OXYGEN = 1;
    private static final double DEFAULT_TANK_BREAK_CHANCE = 0.05;
    private static final long DEFAULT_OFFLINE_CONSUMPTION_SECONDS = 300;
    private static final DecimalFormat OFFLINE_OXYGEN_FORMAT = new DecimalFormat("#.#");
    private static final double REGENERATION_PREVENTION_OXYGEN = -0.15;
    public static final NamespacedKey NO_HEALTH_REGEN = new NamespacedKey("finale", "no_health_regen");

    private final NamespacedKey oxygenKey;
    private final NamespacedKey offlineOxygenLogoutTimeKey;
    private final NamespacedKey offlineOxygenDrainPerSecondKey;
    private final String world;
    private final Map<ActivityManager.Activity, Double> activityMultiplier;
    private final Map<Biome, Double> biomeMultipliers;
    private final double baseOxygenConsumptionPerSecond;
    private final long offlineConsumptionSeconds;
    private final Map<Player, Long> lastMessage = new WeakHashMap<>();
    private final Map<Player, Double> lastOxygenTickChange = new WeakHashMap<>();
    private final OxygenBladderMechanics oxygenBladderMechanics;

    private final Collection<CraftingRecipe> recipes = new ArrayList<>();

    public OxygenManager(ZorwethPlugin plugin, String world, ActivityManager activityManager,
                          Map<ActivityManager.Activity, Double> activityMultiplier,
                          Map<Biome, Double> biomeMultipliers, double baseOxygenConsumptionPerSecond,
                          double tankBreakChance, long offlineConsumptionSeconds) {
        this.oxygenKey = new NamespacedKey(plugin, "oxygen");
        this.offlineOxygenLogoutTimeKey = new NamespacedKey(plugin, "offline_oxygen_logout_time");
        this.offlineOxygenDrainPerSecondKey = new NamespacedKey(plugin, "offline_oxygen_drain_per_second");
        this.world = world;
        this.activityMultiplier = activityMultiplier;
        this.biomeMultipliers = biomeMultipliers;
        this.baseOxygenConsumptionPerSecond = baseOxygenConsumptionPerSecond;
        this.offlineConsumptionSeconds = offlineConsumptionSeconds;
        this.oxygenBladderMechanics = new OxygenBladderMechanics(tankBreakChance);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!isOxygenTracked(player)) {
                    this.lastOxygenTickChange.remove(player);
                    continue;
                }
                if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
                    player.getPersistentDataContainer().remove(NO_HEALTH_REGEN);
                    this.lastOxygenTickChange.put(player, 0D);
                    continue;
                }

                final double oxygen = getOxygen(player);

                Set<ActivityManager.Activity> activities = activityManager.getActivities(player);
                Activity activity = activities
                    .stream()
                    .max(Comparator.comparingDouble(this.activityMultiplier::get))
                    .orElseThrow();
                double playerActivityMultiplier = this.activityMultiplier.get(activity);

                double biomeMultiplier = getBiomeMultiplier(player);

                if (oxygen < REGENERATION_PREVENTION_OXYGEN) {
                    player.getPersistentDataContainer().set(NO_HEALTH_REGEN, PersistentDataType.BOOLEAN, true);
                } else {
                    player.getPersistentDataContainer().remove(NO_HEALTH_REGEN);
                }

                double loss = playerActivityMultiplier * biomeMultiplier;
                if (loss == 0) {
                    final double originalOxygen = oxygen;
                    if (activities.contains(ActivityManager.Activity.IDLE)) {
                        setOxygen(player, oxygen + baseOxygenConsumptionPerSecond * 2);
                    } else {
                        setOxygen(player, oxygen + baseOxygenConsumptionPerSecond);
                    }
                    this.lastOxygenTickChange.put(player, getOxygen(player) - originalOxygen);
                    continue;
                }

                Entity vehicle = player.getVehicle();
                if (vehicle instanceof LivingEntity living) {
                    living.damage(2);
                }

                double amount = loss * baseOxygenConsumptionPerSecond;
                double change = this.oxygenBladderMechanics.getOxygenDrain(player, amount, activity);
                drainOxygen(player, amount, activity);
                this.lastOxygenTickChange.put(player, change);
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
        applyOfflineOxygen(event.getPlayer());
        for (CraftingRecipe recipe : recipes) {
            event.getPlayer().discoverRecipe(recipe.getKey());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(final PlayerQuitEvent event) {
        recordOfflineOxygen(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void on(final PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();
        if (OxygenBottle.isCrudeOxygen(event.getItem())) {
            if (isAtMaxOxygen(player)) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Your oxygen is already full.", NamedTextColor.RED));
                return;
            }
            addOxygen(player, CRUDE_OXYGEN_AMOUNT);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void on(final BrewDrinkEvent event) {
        final BRecipe recipe = event.getBrew().getCurrentRecipe();
        if (recipe == null || !OXYGEN_BREW_RECIPE_NAME.equals(recipe.getRecipeName())) {
            return;
        }

        final Player player = event.getPlayer();
        if (isAtMaxOxygen(player)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Your oxygen is already full.", NamedTextColor.RED));
            return;
        }
        addOxygen(player, OXYGEN_BREW_AMOUNT * (event.getQuality() / 10D));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void on(final PlayerBedEnterEvent event) {
        if (hasOxygen(event.getBed().getBiome())) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("You cannot set your bed in a low-oxygen biome.",
            NamedTextColor.RED));
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
            section.getDouble("consumption_per_second"),
            Math.clamp(section.getDouble("tank_break_chance", DEFAULT_TANK_BREAK_CHANCE), 0D, 1D),
            Math.max(0L, section.getLong("offline_consumption_seconds", DEFAULT_OFFLINE_CONSUMPTION_SECONDS))
        );
    }

    private boolean isOxygenTracked(final Player player) {
        return player.getWorld().getName().equals(this.world) && player.getGameMode() == GameMode.SURVIVAL;
    }

    private double getBiomeMultiplier(final Player player) {
        return this.biomeMultipliers.getOrDefault(player.getLocation().getBlock().getBiome(), 0D);
    }

    private void recordOfflineOxygen(final Player player) {
        if (!isOxygenTracked(player) || player.getWorld().getEnvironment() == World.Environment.NETHER) {
            return;
        }

        final double biomeMultiplier = getBiomeMultiplier(player);
        if (biomeMultiplier == 0D) {
            return;
        }

        final Double idleMultiplier = this.activityMultiplier.get(Activity.IDLE);
        if (idleMultiplier == null) {
            return;
        }

        player.getPersistentDataContainer().set(this.offlineOxygenLogoutTimeKey, PersistentDataType.LONG,
            System.currentTimeMillis());
        player.getPersistentDataContainer().set(this.offlineOxygenDrainPerSecondKey, PersistentDataType.DOUBLE,
            idleMultiplier * biomeMultiplier * this.baseOxygenConsumptionPerSecond);
    }

    private void applyOfflineOxygen(final Player player) {
        final Long logoutTime = player.getPersistentDataContainer().get(this.offlineOxygenLogoutTimeKey,
            PersistentDataType.LONG);
        final Double drainPerSecond = player.getPersistentDataContainer().get(this.offlineOxygenDrainPerSecondKey,
            PersistentDataType.DOUBLE);
        clearOfflineOxygen(player);
        if (logoutTime == null || drainPerSecond == null || drainPerSecond <= 0D || this.offlineConsumptionSeconds <= 0) {
            return;
        }

        final double elapsedSeconds = Math.clamp((System.currentTimeMillis() - logoutTime) / 1000D, 0D,
            this.offlineConsumptionSeconds);
        if (elapsedSeconds <= 0D) {
            return;
        }
        double biomeMultiplier = getBiomeMultiplier(player);
        final double consumed = -drainOxygen(player, elapsedSeconds * drainPerSecond * biomeMultiplier, Activity.IDLE);
        if (consumed > 0D) {
            player.sendMessage(Component.text("You consumed ", NamedTextColor.GRAY)
                .append(Component.text(OFFLINE_OXYGEN_FORMAT.format(consumed * 1000), NamedTextColor.AQUA))
                .append(Component.text(" oxygen while offline.", NamedTextColor.GRAY)));
        }
    }

    private void clearOfflineOxygen(final Player player) {
        player.getPersistentDataContainer().remove(this.offlineOxygenLogoutTimeKey);
        player.getPersistentDataContainer().remove(this.offlineOxygenDrainPerSecondKey);
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

            player.setHealth(Math.max(0.0, player.getHealth() - 1));
        }
    }

    @EventHandler
    public void on(PlayerPostRespawnEvent event) {
        event.getPlayer().getPersistentDataContainer().set(oxygenKey, PersistentDataType.DOUBLE, 1D);
    }

    public double getOxygen(Player player) {
        return Objects.requireNonNullElse(player.getPersistentDataContainer().get(oxygenKey, PersistentDataType.DOUBLE), 1D);
    }

    public double getLastOxygenTickChange(final Player player) {
        return this.lastOxygenTickChange.getOrDefault(player, 0D);
    }

    public boolean hasOxygen(final Biome biome) {
        return this.biomeMultipliers.getOrDefault(biome, 0D) == 0D;
    }

    private void addOxygen(final Player player, final double amount) {
        setOxygen(player, getOxygen(player) + amount);
    }

    private boolean isAtMaxOxygen(final Player player) {
        return getOxygen(player) >= OxygenBladder.getMaxOxygen(player);
    }

    private double drainOxygen(final Player player, final double amount, final Activity activity) {
        final double oxygen = getOxygen(player);
        double remaining = this.oxygenBladderMechanics.getOxygenDrain(player, amount, activity);
        while (remaining > 0D) {
            final double beforeDrain = getOxygen(player);
            setOxygen(player, beforeDrain - remaining);
            remaining -= beforeDrain - getOxygen(player);

            final double refill = this.oxygenBladderMechanics.refillPlayerOxygen(player, CRUDE_OXYGEN_AMOUNT,
                getOxygen(player));
            if (refill <= 0D) {
                break;
            }
            setOxygen(player, getOxygen(player) + refill);
        }
        return getOxygen(player) - oxygen;
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
