package com.devotedmc.ExilePearl.core;

import com.devotedmc.ExilePearl.BrewHandler;
import com.devotedmc.ExilePearl.DamageLogger;
import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.LoreProvider;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.SuicideHandler;
import com.devotedmc.ExilePearl.config.Document;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.holder.BlockHolder;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.storage.StorageKeys;
import com.devotedmc.ExilePearl.util.ExilePearlRunnable;
import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Factory class for creating new core class instances
 *
 * @author Gordon
 */
public final class CorePluginFactory implements PearlFactory {

    private final ExilePearlApi pearlApi;

    public static ExilePearlApi createCore(final Plugin plugin) {
        return new ExilePearlCore(plugin);
    }

    /**
     * Creates a new ExilePearlFactory instance
     *
     * @param plugin The plugin instance
     */
    public CorePluginFactory(final ExilePearlApi plugin) {
        Preconditions.checkNotNull(plugin, "plugin");

        this.pearlApi = plugin;
    }

    @Override
    public ExilePearl createExilePearl(UUID uid, Document doc) {
        Preconditions.checkNotNull(uid, "uid");
        Preconditions.checkNotNull(doc, "doc");

        try {
            UUID killerUuid = doc.getUUID(StorageKeys.KILLER_UUID);
            int pearlId = doc.getInteger(StorageKeys.PEARL_ID);
            Location loc = doc.getLocation(StorageKeys.PEARL_LOCATION);
            int health = doc.getInteger(StorageKeys.PEARL_HEALTH);
            Date pearledOn = doc.getDate(StorageKeys.PEARL_CAPTURE_DATE, new Date());
            Date lastSeen = doc.getDate(StorageKeys.VICTIM_LAST_SEEN, new Date());
            boolean freedOffline = doc.getBoolean(StorageKeys.PEARL_FREED_WHILE_OFFLINE, false);
            boolean summoned = doc.getBoolean(StorageKeys.VICTIM_SUMMONED, false);
            Location returnLoc = doc.getLocation(StorageKeys.VICTIM_RETURN_LOCATION);
            Location captureLoc = doc.getLocation(StorageKeys.PEARL_CAPTURE_LOCATION);

            ExilePearl pearl = new CoreExilePearl(
                pearlApi,
                pearlApi.getStorageProvider().getStorage(),
                uid,
                killerUuid,
                pearlId,
                new BlockHolder(loc.getBlock()),
                pearlApi.getPearlConfig().getDefaultPearlType(),
                pearlApi.getPearlConfig().getPearlHealthDecayTimeout()
            );
            pearl.setPearlType(PearlType.valueOf(doc.getInteger(StorageKeys.PEARL_TYPE, 0)));
            pearl.setHealth(health);
            pearl.setPearledOn(pearledOn);
            pearl.setLastOnline(lastSeen);
            pearl.setFreedOffline(freedOffline);
            pearl.setSummoned(summoned);
            pearl.setReturnLocation(returnLoc);
            pearl.setCaptureLocation(captureLoc);
            pearl.enableStorage();
            return pearl;

        } catch (Exception ex) {
            pearlApi.log(Level.SEVERE, "Failed to create pearl for ID=%s, ", uid.toString(), doc);
            return null;
        }
    }

    @Override
    public ExilePearl createExilePearl(UUID uid, Player killedBy, int pearlId) {
        Preconditions.checkNotNull(uid, "uid");
        Preconditions.checkNotNull(killedBy, "killedBy");

        ExilePearl pearl = new CoreExilePearl(
            pearlApi,
            pearlApi.getStorageProvider().getStorage(),
            uid,
            killedBy.getUniqueId(),
            pearlId,
            new PlayerHolder(killedBy),
            pearlApi.getPearlConfig().getDefaultPearlType(),
            pearlApi.getPearlConfig().getPearlHealthDecayTimeout()
        );
        pearl.enableStorage();
        return pearl;
    }

    @Override
    public ExilePearl createExilePearl(UUID uid, UUID killedById, int pearlId, PearlHolder holder) {
        Preconditions.checkNotNull(uid, "uid");
        Preconditions.checkNotNull(killedById, "killedById");
        Preconditions.checkNotNull(holder, "holder");

        ExilePearl pearl = new CoreExilePearl(
            pearlApi,
            pearlApi.getStorageProvider().getStorage(),
            uid,
            killedById,
            pearlId,
            holder,
            pearlApi.getPearlConfig().getDefaultPearlType(),
            pearlApi.getPearlConfig().getPearlHealthDecayTimeout()
        );
        pearl.enableStorage();
        return pearl;
    }

    @Override
    public ExilePearl createdMigratedPearl(UUID uid, Document doc) {
        Preconditions.checkNotNull(uid, "uid");
        Preconditions.checkNotNull(doc, "doc");

        doc.append(StorageKeys.PEARL_HEALTH, pearlApi.getPearlConfig().getPearlHealthMaxValue() / 2); // set health to half max health
        return createExilePearl(uid, doc);
    }

    public PearlManager createPearlManager() {
        return new CorePearlManager(pearlApi, this, pearlApi.getStorageProvider());
    }

    public ExilePearlRunnable createPearlDecayWorker() {
        return new PearlDecayTask(pearlApi);
    }

    public SuicideHandler createSuicideHandler() {
        return new PlayerSuicideTask(pearlApi);
    }

    public BrewHandler createBrewHandler() {
        if (Bukkit.getPluginManager().isPluginEnabled("Brewery")) {
            return new BreweryHandler();
        } else {
            pearlApi.log("Brewery not found, defaulting to no-brew handler");
            return new NoBrewHandler(pearlApi);
        }
    }

    public LoreProvider createLoreGenerator() {
        return new CoreLoreGenerator(pearlApi.getPearlConfig(), new NamespacedKey(pearlApi, "exile_pearl_id"));
    }

    public PearlConfig createPearlConfig() {
        return new CorePearlConfig(pearlApi, pearlApi);
    }

    public DamageLogger createDamageLogger() {
        return new CoreDamageLogger(pearlApi);
    }
}
