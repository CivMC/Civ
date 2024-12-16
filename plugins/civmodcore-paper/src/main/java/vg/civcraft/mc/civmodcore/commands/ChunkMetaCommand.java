package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.command.CommandSender;

@CommandAlias("cmc")
public class ChunkMetaCommand extends BaseCommand {

    private static final Logger CHUNK_META_LOGGER = LogManager.getLogger("Chunk meta");

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
