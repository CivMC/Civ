package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

@CommandAlias("cmc")
public class ChunkMetaCommand extends BaseCommand {

	private static boolean ENABLE_CHUNK_META_LOGS = false;

	public static boolean chunkMetaLogsEnabled() {
		return ENABLE_CHUNK_META_LOGS;
	}

	@Subcommand("chunkmeta")
	@Description("Toggle showing chunk meta logs")
	@CommandPermission("cmc.debug")
	public void chunkmeta(CommandSender sender) {
		if (ENABLE_CHUNK_META_LOGS) {
			ENABLE_CHUNK_META_LOGS = false;
			sender.sendMessage(Component.text("Chunk meta logs disabled.", NamedTextColor.RED));
		} else {
			ENABLE_CHUNK_META_LOGS = true;
			sender.sendMessage(Component.text("Chunk meta logs enabled.", NamedTextColor.GREEN));
		}
	}
}
