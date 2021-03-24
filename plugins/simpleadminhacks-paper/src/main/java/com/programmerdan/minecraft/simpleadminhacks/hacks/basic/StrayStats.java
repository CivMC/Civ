package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.command.AikarCommand;
import vg.civcraft.mc.namelayer.NameLayerPlugin;

public final class StrayStats extends BasicHack {

	public StrayStats(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
	}

	public static BasicHackConfig generate(final SimpleAdminHacks plugin, final ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

	// ------------------------------------------------------------
	// Bootstrap
	// ------------------------------------------------------------

	private final StatsCommand statsCommand = new StatsCommand();

	@Override
	public void onEnable() {
		super.onEnable();
		BanStick.getPlugin();
		NameLayerPlugin.getInstance();
		this.plugin.getCommands().registerCommand(this.statsCommand);
	}

	@Override
	public void onDisable() {
		this.plugin.getCommands().deregisterCommand(this.statsCommand);
		super.onDisable();
	}

	// ------------------------------------------------------------
	// Statistics Commands
	// ------------------------------------------------------------

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	private class StatsCommand extends AikarCommand {
		@CommandAlias("compile_player_join_statistics")
		@CommandPermission("simpleadmin.stats")
		@SuppressWarnings("unchecked")
		public void compileStatistics(final CommandSender sender) throws IOException, IllegalAccessException {
			sender.sendMessage(ChatColor.GOLD + "Starting to compile player join statistics!");
			BSPlayer.preload(0, Integer.MAX_VALUE); // Just load everything
			final var cache = (Map<UUID, BSPlayer>) FieldUtils.readDeclaredStaticField(BSPlayer.class, "allPlayersUUID", true);
			final var builder = new StringBuilder();
			cache.values()
					.parallelStream()
					.sorted(Comparator.comparing(BSPlayer::getFirstAdd))
					.map(entry -> entry.getName() + "," + DATE_FORMAT.format(entry.getFirstAdd()))
					.forEachOrdered(entry -> builder.append(entry).append("\n"));
			try (final var test = new FileWriter(new File(plugin().getDataFolder(), "playerJoinStats.csv"))) {
				test.write(builder.toString());
			}
			sender.sendMessage(ChatColor.GREEN + "Finished compiling player join statistics!");
		}
	}

}
