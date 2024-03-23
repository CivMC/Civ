package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import java.util.logging.Level;
import java.util.logging.Logger;

@CommandAlias("cmc")
public class ChunkMetaCommand extends BaseCommand {

	private static final Logger CHUNK_META_LOGGER = Logger.getLogger("Chunk meta");

	@Subcommand("togglechunkmeta")
	@Description("Toggle showing chunk meta logs")
	@CommandPermission("cmc.debug")
	public void chunkmeta(CommandSender sender) {
		if (CHUNK_META_LOGGER.getLevel().intValue() < Level.INFO.intValue()) {
			CHUNK_META_LOGGER.setLevel(Level.INFO);
			sender.sendMessage(Component.text("Chunk meta logs disabled.", NamedTextColor.RED));
		} else {
			CHUNK_META_LOGGER.setLevel(Level.FINE);
			sender.sendMessage(Component.text("Chunk meta logs enabled.", NamedTextColor.GREEN));
		}
	}
}
