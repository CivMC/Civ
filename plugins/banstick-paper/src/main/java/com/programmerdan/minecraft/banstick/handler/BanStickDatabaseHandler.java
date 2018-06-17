package com.programmerdan.minecraft.banstick.handler;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.data.BSLog;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSSession;
import com.programmerdan.minecraft.banstick.data.BSShare;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

/**
 * Ties into the managed datasource processes of the CivMod core plugin.
 *
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class BanStickDatabaseHandler {

	private ManagedDatasource data;

	public ManagedDatasource getData() {
		return this.data;
	}

	private static BanStickDatabaseHandler instance;

	public static BanStickDatabaseHandler getInstance() {
		return BanStickDatabaseHandler.instance;
	}

	public static ManagedDatasource getinstanceData() {
		return BanStickDatabaseHandler.instance.data;
	}

	public BanStickDatabaseHandler(FileConfiguration config) {
		if (!configureData(config.getConfigurationSection("database"))) {
			throw new RuntimeException("Failed to configure Database for BanStick!");
		}
		BanStickDatabaseHandler.instance = this;
	}

	private boolean configureData(ConfigurationSection config) {
		String host = config.getString("host", "localhost");
		int port = config.getInt("port", 3306);
		String dbname = config.getString("database", "banstick");
		String username = config.getString("user");
		String password = config.getString("password");
		int poolsize = config.getInt("poolsize", 5);
		long connectionTimeout = config.getLong("connection_timeout", 10000l);
		long idleTimeout = config.getLong("idle_timeout", 600000l);
		long maxLifetime = config.getLong("max_lifetime", 7200000l);
		try {
			data = new ManagedDatasource(BanStick.getPlugin(), username, password, host, port, dbname,
					poolsize, connectionTimeout, idleTimeout, maxLifetime);
			data.getConnection().close();
		} catch (Exception se) {
			BanStick.getPlugin().info("Failed to initialize Database connection");
			se.printStackTrace();
			return false;
		}

		initializeTables();
		stageUpdates();

		long begin_time = System.currentTimeMillis();

		try {
			BanStick.getPlugin().info("Update prepared, starting database update.");
			if (!data.updateDatabase()) {
				BanStick.getPlugin().info( "Update failed, disabling plugin.");
				return false;
			}
		} catch (Exception e) {
			BanStick.getPlugin().severe("Update failed, disabling plugin. Cause:", e);
			return false;
		}

		BanStick.getPlugin().info(String.format("Database update took %d seconds", (System.currentTimeMillis() - begin_time) / 1000));

		activatePreload(config.getConfigurationSection("preload"));
		activateDirtySave(config.getConfigurationSection("dirtysave"));
		return true;
	}

	private void activateDirtySave(ConfigurationSection config) {
		long period = 5*60*50l;
		long delay = 5*60*50l;
		if (config != null) {
			period = config.getLong("period", period);
			delay = config.getLong("delay", delay);
		}
		BanStick.getPlugin().debug("DirtySave Period {0} Delay {1}", period, delay);

		Bukkit.getScheduler().runTaskTimerAsynchronously(BanStick.getPlugin(), new Runnable() {
			@Override
			public void run() {
				BanStick.getPlugin().debug("Player dirty save");
				BSPlayer.saveDirty();
			}
		}, delay, period);

		Bukkit.getScheduler().runTaskTimerAsynchronously(BanStick.getPlugin(), new Runnable() {
			@Override
			public void run() {
				BanStick.getPlugin().debug("Ban dirty save");
				BSBan.saveDirty();
			}
		}, delay + (period / 5), period);

		Bukkit.getScheduler().runTaskTimerAsynchronously(BanStick.getPlugin(), new Runnable() {
			@Override
			public void run() {
				BanStick.getPlugin().debug("Session dirty save");
				BSSession.saveDirty();
			}
		}, delay + ((period * 2) / 5), period);

		Bukkit.getScheduler().runTaskTimerAsynchronously(BanStick.getPlugin(), new Runnable() {
			@Override
			public void run() {
				BanStick.getPlugin().debug("Share dirty save");
				BSShare.saveDirty();
			}
		}, delay + ((period * 3) / 5), period);

		Bukkit.getScheduler().runTaskTimerAsynchronously(BanStick.getPlugin(), new Runnable() {
			@Override
			public void run() {
				BanStick.getPlugin().debug("Proxy dirty save");
				BSIPData.saveDirty();
			}
		}, delay + ((period * 4) / 5), period);

		BanStick.getPlugin().info("Dirty save tasks started.");
	}

	private void activatePreload(ConfigurationSection config) {
		if (config != null && config.getBoolean("enabled")) {
			long period = 5*60*50l;
			long delay = 5*60*50l;
			if (config != null) {
				period = config.getLong("period", period);
				delay = config.getLong("delay", delay);
			}
			final int batchsize = config.getInt("batch", 100);

			BanStick.getPlugin().debug("Preload Period {0} Delay {1} batch {2}", period, delay, batchsize);

			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
					BanStick.getPlugin().debug("IP preload {0}, lim {1}", lastId, batchsize);
					lastId = BSIP.preload(lastId, batchsize);
					if (lastId < 0) this.cancel();
				}
			}.runTaskTimerAsynchronously(BanStick.getPlugin(), delay, period);

			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
					BanStick.getPlugin().debug("Proxy preload {0}, lim {1}", lastId, batchsize);
					lastId = BSIPData.preload(lastId, batchsize);
					if (lastId < 0) this.cancel();
				}
			}.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + (period / 6), period);

			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
					BanStick.getPlugin().debug("Ban preload {0}, lim {1}", lastId, batchsize);
					lastId = BSBan.preload(lastId, batchsize, false);
					if (lastId < 0) this.cancel();
				}
			}.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + ((period * 2) / 6), period);

			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
					BanStick.getPlugin().debug("Player preload {0}, lim {1}", lastId, batchsize);
					lastId = BSPlayer.preload(lastId, batchsize);
					if (lastId < 0) this.cancel();
				}
			}.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + ((period * 3) / 6), period);

			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
					BanStick.getPlugin().debug("Session preload {0}, lim {1}", lastId, batchsize);
					lastId = BSSession.preload(lastId, batchsize);
					if (lastId < 0) this.cancel();
				}
			}.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + ((period * 4) / 6), period);

			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
					BanStick.getPlugin().debug("Share preload {0}, lim {1}", lastId, batchsize);
					lastId = BSShare.preload(lastId, batchsize);
					if (lastId < 0) this.cancel();
				}
			}.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + ((period * 5) / 6), period);
		} else {
			BanStick.getPlugin().info("Preloading is disabled. Expect more lag on joins, lookups, and bans.");
		}

	}

	/**
	 * Basic method to set up data model v1.
	 */
	private void initializeTables() {
		data.registerMigration(0,  false,
					"CREATE TABLE IF NOT EXISTS bs_player (" +
					" pid BIGINT AUTO_INCREMENT PRIMARY KEY," +
					" name VARCHAR(16)," +
					" uuid CHAR(36) NOT NULL UNIQUE," +
					" first_add TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" bid BIGINT REFERENCES bs_ban(bid)," +
					" ip_pardon_time TIMESTAMP NULL, " +
					" proxy_pardon_time TIMESTAMP NULL," +
					" shared_pardon_time TIMESTAMP NULL," +
					" INDEX bs_player_name (name)," +
					" INDEX bs_player_ip_pardons (ip_pardon_time)," +
					" INDEX bs_player_proxy_pardons (proxy_pardon_time)," +
					" INDEX bs_player_shared_pardons (shared_pardon_time)," +
					" INDEX bs_player_join (first_add)" +
					");",
					"CREATE TABLE IF NOT EXISTS bs_session (" +
					" sid BIGINT AUTO_INCREMENT PRIMARY KEY," +
					" pid BIGINT REFERENCES bs_player(pid)," +
					" join_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" leave_time TIMESTAMP NULL," +
					" iid BIGINT NOT NULL REFERENCES bs_ip(iid)," +
					" INDEX bs_session_pids (pid, join_time, leave_time)" +
					");",
					"CREATE TABLE IF NOT EXISTS bs_ban_log (" +
					" lid BIGINT AUTO_INCREMENT PRIMARY KEY," +
					" pid BIGINT NOT NULL REFERENCES bs_player(pid)," +
					" bid BIGINT NOT NULL REFERENCES bs_ban(bid)," +
					" action_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" action VARCHAR(10) NOT NULL," +
					" INDEX bs_ban_log_time (pid, action_time DESC)" +
					");", // TODO: Whenever a ban is given or removed from a player, record.
					"CREATE TABLE IF NOT EXISTS bs_ban (" +
					" bid BIGINT AUTO_INCREMENT PRIMARY KEY," +
					" ban_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" ip_ban BIGINT REFERENCES bs_ip(iid)," +
					" proxy_ban BIGINT REFERENCES bs_ip_data(idid)," +
					" share_ban BIGINT REFERENCES bs_share(sid)," +
					" admin_ban BOOLEAN DEFAULT FALSE," +
					" message TEXT," +
					" ban_end TIMESTAMP NULL," +
					" INDEX bs_ban_time (ban_time)," +
					" INDEX bs_ban_ip (ip_ban)," +
					" INDEX bs_ban_proxy (proxy_ban)," +
					" INDEX bs_ban_share (share_ban)," +
					" INDEX bs_ban_end (ban_end)" +
					");",
					"CREATE TABLE IF NOT EXISTS bs_share (" +
					" sid BIGINT AUTO_INCREMENT PRIMARY KEY," +
					" create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" first_pid BIGINT NOT NULL REFERENCES bs_player(pid)," +
					" second_pid BIGINT NOT NULL REFERENCES bs_player(pid)," +
					" first_sid BIGINT NOT NULL REFERENCES bs_session(sid)," +
					" second_sid BIGINT NOT NULL REFERENCES bs_session(sid)," +
					" pardon BOOLEAN DEFAULT FALSE," +
					" pardon_time TIMESTAMP NULL," +
					" INDEX bs_share (first_pid, second_pid)," +
					" INDEX bs_pardon (pardon_time)" +
					");",
					"CREATE TABLE IF NOT EXISTS bs_ip (" +
					" iid BIGINT AUTO_INCREMENT PRIMARY KEY," +
					" create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" ip4 CHAR(15)," +
					" ip4cidr SMALLINT," +
					" ip6 CHAR(39)," +
					" ip6cidr SMALLINT," +
					" INDEX bs_session_ip4 (ip4, ip4cidr)," +
					" INDEX bs_session_ip6 (ip6, ip6cidr)" +
					");",
					"CREATE TABLE IF NOT EXISTS bs_ip_data (" +
					" idid BIGINT AUTO_INCREMENT PRIMARY KEY," +
					" iid BIGINT NOT NULL REFERENCES bs_ip(iid)," +
					" create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" valid BOOLEAN DEFAULT TRUE," +
					" continent TEXT," +
					" country TEXT," +
					" region TEXT," +
					" city TEXT," +
					" postal TEXT," +
					" lat DOUBLE DEFAULT NULL," +
					" lon DOUBLE DEFAULT NULL," +
					" domain TEXT," +
					" provider TEXT," +
					" registered_as TEXT," +
					" connection TEXT," +
					" proxy FLOAT," +
					" source TEXT," +
					" comment TEXT," +
					" INDEX bs_ip_data_iid (iid)," +
					" INDEX bs_ip_data_valid (valid, create_time DESC)," +
					" INDEX bs_ip_data_proxy (proxy)" +
					");"
				);
		data.registerMigration(1,  false, "CREATE TABLE IF NOT EXISTS bs_exclusion ("
		        + "eid BIGINT AUTO_INCREMENT PRIMARY KEY,"
	            + "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "first_pid BIGINT NOT NULL REFERENCES bs_player(pid),"
                + "second_pid BIGINT NOT NULL REFERENCES bs_player(pid),"
                + " INDEX bs_exclusion_pid (first_pid, second_pid)"
		        + ");");

	}

	/**
	 * Add all new migrations here.
	 */
	private void stageUpdates() {

	}

	public void doShutdown() {

		BanStick.getPlugin().info("Player dirty save");
		BSPlayer.saveDirty();

		BanStick.getPlugin().info("Ban dirty save");
		BSBan.saveDirty();

		BanStick.getPlugin().info("Session dirty save");
		BSSession.saveDirty();

		BanStick.getPlugin().info("Share dirty save");
		BSShare.saveDirty();

		BanStick.getPlugin().info("Proxy dirty save");
		BSIPData.saveDirty();

		BanStick.getPlugin().info("Ban Log save");
		BSLog log = BanStick.getPlugin().getLogHandler();
		if (log != null) log.disable();
	}

	// ============ QUERIES =============

	public BSPlayer getOrCreatePlayer(final Player player) {
		// TODO: use exception
		BSPlayer bsPlayer = getPlayer(player.getUniqueId());
		if (bsPlayer == null) {
			bsPlayer = BSPlayer.create(player);
		}

		return bsPlayer;
	}

	public BSPlayer getPlayer(final UUID uuid) {
		return BSPlayer.byUUID(uuid); // TODO: exception
	}

	public BSIP getOrCreateIP(final InetAddress netAddress) {
		BSIP bsIP = getIP(netAddress);
		if (bsIP == null) {
			BanStick.getPlugin().debug("Creating IP address: {0}", netAddress);
			bsIP = BSIP.create(netAddress);
		} else {
			BanStick.getPlugin().info("Registering future retrieval of IPData for {0}", bsIP.toString());
			BanStick.getPlugin().getIPDataHandler().offer(bsIP);
		}
		return bsIP;
	}

	public BSIP getIP(final InetAddress netAddress) {
		return BSIP.byInetAddress(netAddress);
	}

	public List<BSIP> getAllByIP(final InetAddress netAddress) {
		return BSIP.allMatching(netAddress);
	}
}
