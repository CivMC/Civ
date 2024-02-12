package vg.civcraft.mc.civmodcore;

import javax.annotation.Nonnull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.config.ConfigParser;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;

import java.util.Objects;

public final class CivModCoreConfig extends ConfigParser {

	private DatabaseCredentials databaseCredentials;
	private static final DatabaseCredentials DEFAULT_DATABASE_CREDENTIALS = null;

	private String scoreboardHeader;
	private static final String DEFAULT_SCOREBOARD_HEADER = "  Info  ";

	private int skinCacheThreads;
	private static final int DEFAULT_SKIN_CACHE_THREADS = Runtime.getRuntime().availableProcessors() / 2;

	private boolean chunkLoadingStatistics;
	private static final boolean DEFAULT_CHUNK_LOADING_STATISTICS = true;

	private int chunkLoadingThreads;
	private static final int DEFAULT_CHUNK_LOADING_THREADS = 1;

	CivModCoreConfig(@Nonnull final CivModCorePlugin plugin) {
		super(plugin);
		Objects.requireNonNull(plugin);
		reset();
	}

	@Override
	protected boolean parseInternal(@Nonnull final ConfigurationSection config) {
		this.databaseCredentials = config.getObject("database",
				DatabaseCredentials.class, DEFAULT_DATABASE_CREDENTIALS);
		this.scoreboardHeader = ChatColor.translateAlternateColorCodes('&',
				config.getString("scoreboardHeader", DEFAULT_SCOREBOARD_HEADER));
		this.skinCacheThreads = config.getInt("skin-download-threads", DEFAULT_SKIN_CACHE_THREADS);
		this.chunkLoadingStatistics = config.getBoolean("chunk-loading-statistics", DEFAULT_CHUNK_LOADING_STATISTICS);
		this.chunkLoadingThreads = config.getInt("chunk-loading-threads", DEFAULT_CHUNK_LOADING_THREADS);
		return true;
	}

	@Override
	public void reset() {
		super.reset();
		this.databaseCredentials = DEFAULT_DATABASE_CREDENTIALS;
		this.scoreboardHeader = DEFAULT_SCOREBOARD_HEADER;
		this.skinCacheThreads = DEFAULT_SKIN_CACHE_THREADS;
		this.chunkLoadingStatistics = DEFAULT_CHUNK_LOADING_STATISTICS;
		this.chunkLoadingThreads = DEFAULT_CHUNK_LOADING_THREADS;
	}

	public DatabaseCredentials getDatabaseCredentials() {
		return databaseCredentials;
	}

	public String getScoreboardHeader() {
		return scoreboardHeader;
	}

	public int getSkinCacheThreads() {
		return skinCacheThreads;
	}

	public boolean getChunkLoadingStatistics() {
		return this.chunkLoadingStatistics;
	}

	public int getChunkLoadingThreads() {
		return this.chunkLoadingThreads;
	}
}
