package net.civmc.zorweth.transfer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class RocketManifestFileWriter {

    private RocketManifestFileWriter() {
    }

    public static File write(final JavaPlugin plugin, final RocketManifest manifest) throws IOException {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(manifest, "manifest");

        final long timestamp = System.currentTimeMillis();
        final File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Unable to create plugin directory " + dataFolder.getPath());
        }

        final File file = new File(dataFolder, "rocket-manifest-" + timestamp + ".yml");
        final YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("timestamp", timestamp);
        yaml.set("transfer-id", manifest.transferId().toString());
        yaml.set("source-server", manifest.sourceServer());
        yaml.set("destination-server", manifest.destinationServer());
        yaml.set("source-world", manifest.sourceWorld());
        yaml.set("destination-world", manifest.destinationWorld());
        writeBlockPosition(yaml.createSection("source-origin"), manifest.sourceOrigin());
        yaml.set("destination-requested.x", manifest.destinationRequestedX());
        yaml.set("destination-requested.z", manifest.destinationRequestedZ());
        yaml.set("fuelKg", manifest.fuelKg());

        final ConfigurationSection passengers = yaml.createSection("passengers");
        for (int index = 0; index < manifest.passengers().size(); index++) {
            final RocketManifestPassenger passenger = manifest.passengers().get(index);
            final ConfigurationSection section = passengers.createSection(String.valueOf(index));
            section.set("player-uuid", passenger.playerUuid().toString());
            writeEntityPosition(section.createSection("relative-position"), passenger.relativePosition());
            section.set("inventory-contents", Arrays.asList(passenger.inventoryContents()));
            section.set("health", passenger.health());
            section.set("xp-level", passenger.xpLevel());
            section.set("xp-progress", passenger.xpProgress());
            section.set("food-level", passenger.foodLevel());
            section.set("saturation", passenger.saturation());
            section.set("exhaustion", passenger.exhaustion());
            section.set("held-slot", passenger.heldSlot());
            section.set("game-mode", passenger.gameMode().name());
        }

        final ConfigurationSection chests = yaml.createSection("chests");
        for (int index = 0; index < manifest.chests().size(); index++) {
            final RocketManifestChest chest = manifest.chests().get(index);
            final ConfigurationSection section = chests.createSection(String.valueOf(index));
            writeBlockPosition(section.createSection("relative-position"), chest.relativePosition());
            section.set("contents", Arrays.asList(chest.contents()));
        }

        yaml.save(file);
        return file;
    }

    private static void writeBlockPosition(final ConfigurationSection section, final RocketBlockPosition position) {
        section.set("x", position.x());
        section.set("y", position.y());
        section.set("z", position.z());
    }

    private static void writeEntityPosition(final ConfigurationSection section, final RocketEntityPosition position) {
        section.set("x", position.x());
        section.set("y", position.y());
        section.set("z", position.z());
        section.set("yaw", position.yaw());
        section.set("pitch", position.pitch());
    }
}
