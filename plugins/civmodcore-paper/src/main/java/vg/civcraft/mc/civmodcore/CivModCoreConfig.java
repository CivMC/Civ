package vg.civcraft.mc.civmodcore;

import javax.annotation.Nonnull;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.config.ConfigParser;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;

public final class CivModCoreConfig extends ConfigParser {

	@Getter
	private DatabaseCredentials databaseCredentials;
	private static final DatabaseCredentials DEFAULT_DATABASE_CREDENTIALS = null;

	@Getter
	private String scoreboardHeader;
	private static final String DEFAULT_SCOREBOARD_HEADER = "  Info  ";

	@Getter
	private int skinCacheThreads;
	private static final int DEFAULT_SKIN_CACHE_THREADS = Runtime.getRuntime().availableProcessors() / 2;

	CivModCoreConfig(@Nonnull final CivModCorePlugin plugin) {
		super(plugin);
		reset();
	}

	@Override
	protected boolean parseInternal(@Nonnull final ConfigurationSection config) {
		this.databaseCredentials = config.getObject("database",
				DatabaseCredentials.class, DEFAULT_DATABASE_CREDENTIALS);
		this.scoreboardHeader = ChatColor.translateAlternateColorCodes('&',
				config.getString("scoreboardHeader", DEFAULT_SCOREBOARD_HEADER));
		this.skinCacheThreads = config.getInt("skin-download-threads", DEFAULT_SKIN_CACHE_THREADS);
		return true;
	}

	@Override
	public void reset() {
		super.reset();
		this.databaseCredentials = DEFAULT_DATABASE_CREDENTIALS;
		this.scoreboardHeader = DEFAULT_SCOREBOARD_HEADER;
		this.skinCacheThreads = DEFAULT_SKIN_CACHE_THREADS;
	}

}
