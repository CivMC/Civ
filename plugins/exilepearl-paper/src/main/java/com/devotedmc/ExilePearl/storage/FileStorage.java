package com.devotedmc.ExilePearl.storage;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.config.Document;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;

/**
 * File storage for pearls. Not done yet
 *
 * @author Gordon
 */
class FileStorage implements PluginStorage {
    private static final String PEARLS_KEY = "pearls";

    private final File pearlFile;
    private final PearlFactory pearlFactory;
    private final PearlLogger logger;

    private Document doc = new Document();
    private Document pearlDoc;

    public FileStorage(final File file, final PearlFactory pearlFactory, final PearlLogger logger) {
        Preconditions.checkNotNull(file, "file");
        Preconditions.checkNotNull(pearlFactory, "pearlFactory");
        Preconditions.checkNotNull(logger, "logger");

        this.pearlFile = file;
        this.pearlFactory = pearlFactory;
        this.logger = logger;
    }

    @Override
    public Collection<ExilePearl> loadAllPearls() {
        HashSet<ExilePearl> pearls = new HashSet<ExilePearl>();

        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(pearlFile);
        doc = new Document(fileConfig);
        pearlDoc = doc.getDocument(PEARLS_KEY);
        if (pearlDoc == null) {
            pearlDoc = new Document();
            doc.append(PEARLS_KEY, pearlDoc);
            writeFile();
            return pearls;
        }

        for (Entry<String, Object> entry : pearlDoc.entrySet()) {
            try {
                UUID playerId = UUID.fromString(entry.getKey());
                pearls.add(pearlFactory.createExilePearl(playerId, (Document) entry.getValue()));
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to load pearl record: %s", doc.get(entry.getKey()));
                ex.printStackTrace();
            }
        }

        return pearls;
    }

    @Override
    public void pearlInsert(ExilePearl pearl) {
        Document insert = new Document()
            .append("player_name", pearl.getPlayerName()) // Not needed, just makes it easier to search file
            .append(StorageKeys.KILLER_UUID, pearl.getKillerId().toString())
            .append(StorageKeys.PEARL_ID, pearl.getPearlId())
            .append(StorageKeys.PEARL_TYPE, pearl.getPearlType().toInt())
            .append(StorageKeys.PEARL_LOCATION, pearl.getLocation())
            .append(StorageKeys.PEARL_HEALTH, pearl.getHealth())
            .append(StorageKeys.PEARL_CAPTURE_DATE, pearl.getPearledOn())
            .append(StorageKeys.VICTIM_LAST_SEEN, pearl.getLastOnline())
            .append(StorageKeys.PEARL_FREED_WHILE_OFFLINE, pearl.getFreedOffline())
            .append(StorageKeys.VICTIM_SUMMONED, pearl.isSummoned());
        if (pearl.isSummoned()) {
            insert.append(StorageKeys.VICTIM_RETURN_LOCATION, pearl.getReturnLocation());
        }
        final Location captureLocation = pearl.getCaptureLocation();
        if (captureLocation != null) {
            insert.append(StorageKeys.PEARL_CAPTURE_LOCATION, captureLocation);
        }

        pearlDoc.append(pearl.getPlayerId().toString(), insert);
        writeFile();
    }

    @Override
    public void pearlRemove(ExilePearl pearl) {
        pearlDoc.remove(pearl.getPlayerId().toString());
        writeFile();
    }

    @Override
    public void updatePearlLocation(ExilePearl pearl) {
        pearlDoc.getDocument(pearl.getPlayerId().toString()).append(StorageKeys.PEARL_LOCATION, pearl.getLocation());
        writeFile();
    }

    @Override
    public void updatePearlHealth(ExilePearl pearl) {
        pearlDoc.getDocument(pearl.getPlayerId().toString()).append(StorageKeys.PEARL_HEALTH, pearl.getHealth());
        writeFile();
    }

    @Override
    public void updatePearlFreedOffline(ExilePearl pearl) {
        pearlDoc.getDocument(pearl.getPlayerId().toString()).append(StorageKeys.PEARL_FREED_WHILE_OFFLINE, pearl.getFreedOffline());
        writeFile();
    }

    @Override
    public void updatePearlType(ExilePearl pearl) {
        pearlDoc.getDocument(pearl.getPlayerId().toString()).append(StorageKeys.PEARL_TYPE, pearl.getPearlType().toInt());
        writeFile();
    }

    @Override
    public void updatePearlKiller(ExilePearl pearl) {
        pearlDoc.getDocument(pearl.getPlayerId().toString()).append(StorageKeys.KILLER_UUID, pearl.getKillerId().toString());
        writeFile();
    }

    @Override
    public void updatePearlLastOnline(ExilePearl pearl) {
        pearlDoc.getDocument(pearl.getPlayerId().toString()).append(StorageKeys.VICTIM_LAST_SEEN, pearl.getLastOnline());
        writeFile();
    }

    @Override
    public void updatePearlSummoned(ExilePearl pearl) {
        pearlDoc.getDocument(pearl.getPlayerId().toString()).append(StorageKeys.VICTIM_SUMMONED, pearl.isSummoned());
        writeFile();
    }

    @Override
    public void updateReturnLocation(ExilePearl pearl) {
        pearlDoc.getDocument(pearl.getPlayerId().toString()).append(StorageKeys.VICTIM_RETURN_LOCATION, pearl.getReturnLocation());
        writeFile();
    }

    @Override
    public void updateCaptureLocation(
        final @NotNull ExilePearl pearl
    ) {
        final Document pearlDoc = this.pearlDoc.getDocument(pearl.getPlayerId().toString());
        final Location location = pearl.getCaptureLocation();
        if (location == null) {
            pearlDoc.remove(StorageKeys.PEARL_CAPTURE_LOCATION);
        }
        else {
            pearlDoc.append(StorageKeys.PEARL_CAPTURE_LOCATION, location);
        }
        writeFile();
    }

    @Override
    public void updatePearledOnDate(ExilePearl pearl) {
        pearlDoc.getDocument(pearl.getPlayerId().toString()).append(StorageKeys.PEARL_CAPTURE_DATE, pearl.getPearledOn());
        writeFile();
    }

    private void writeFile() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(pearlFile);
        doc.savetoConfig(config);
        try {
            config.save(pearlFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to write pearl data to file");
            e.printStackTrace();
        }
    }

    @Override
    public boolean connect() {
        doc = new Document();
        pearlDoc = new Document();
        if (!pearlFile.exists()) {
            try {
                pearlFile.createNewFile();
                try (FileWriter writer = new FileWriter(pearlFile)) {
                    writer.write("# Do not edit this file directly while the server is running!");
                }
                doc.append(PEARLS_KEY, pearlDoc);
                writeFile();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void disconnect() {
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
