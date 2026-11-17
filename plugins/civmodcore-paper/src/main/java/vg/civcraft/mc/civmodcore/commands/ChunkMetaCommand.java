package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.ChunkMetaMemoryStats;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.ChunkMetaAPI;

@CommandAlias("cmc")
public class ChunkMetaCommand extends BaseCommand {

    private static final Logger CHUNK_META_LOGGER = LogManager.getLogger("Chunk meta");

    @Subcommand("memory")
    @Description("Inspect chunk meta array memory and occupied slots")
    @CommandPermission("cmc.debug")
    public void memory(final @NotNull CommandSender sender) {
        if (CivModCorePlugin.getInstance().getChunkMetaManager() == null) {
            sender.sendMessage(Component.text("Chunk meta is not available.", NamedTextColor.RED));
            return;
        }
        final Map<String, ChunkMetaMemoryStats> statistics = ChunkMetaAPI.getMemoryStats();
        ChunkMetaMemoryStats total = ChunkMetaMemoryStats.EMPTY;
        for (final ChunkMetaMemoryStats stats : statistics.values()) {
            total = total.add(stats);
        }
        sender.sendMessage(Component.text("Chunk meta 4D array memory (all cached worlds/chunks)", NamedTextColor.GOLD));
        sendMemoryStats(sender, "Total", total);
        statistics.forEach((name, stats) -> sendMemoryStats(sender, name, stats));
    }

    private static void sendMemoryStats(final @NotNull CommandSender sender, final @NotNull String name,
                                        final @NotNull ChunkMetaMemoryStats stats) {
        final double occupancy = stats.allocatedSlots() == 0 ? 0.0
            : 100.0 * stats.occupiedSlots() / stats.allocatedSlots();
        sender.sendMessage(Component.text(String.format(Locale.ROOT,
            "%s: ~%,d bytes (%.2f MiB); %,d chunk metas; slots %,d / %,d (%.2f%%); arrays L1/L2/L3/L4: %,d/%,d/%,d/%,d",
            name, stats.estimatedBytes(), stats.estimatedBytes() / (1024.0 * 1024.0), stats.level1Arrays(),
            stats.occupiedSlots(), stats.allocatedSlots(), occupancy, stats.level1Arrays(), stats.level2Arrays(),
            stats.level3Arrays(), stats.level4Arrays()), NamedTextColor.YELLOW));
    }

    @Subcommand("togglechunkmeta")
    @Description("Toggle showing chunk meta logs")
    @CommandPermission("cmc.debug")
    public void chunkmeta(CommandSender sender) {
        if (!CHUNK_META_LOGGER.getLevel().isMoreSpecificThan(Level.INFO)) {
            Configurator.setLevel("Chunk meta", Level.INFO);
            sender.sendMessage(Component.text("Chunk meta logs disabled.", NamedTextColor.RED));
        } else {
            Configurator.setLevel("Chunk meta", Level.DEBUG);
            sender.sendMessage(Component.text("Chunk meta logs enabled.", NamedTextColor.GREEN));
        }
    }
}
