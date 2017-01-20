package com.programmerdan.minecraft.banstick.handler;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSSession;
import com.programmerdan.minecraft.banstick.data.BSShare;
import com.programmerdan.minecraft.banstick.data.BSVPN;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

/**
 * Ties into the managed datasource processes of the CivMod core plugin.
 * 
 * @author ProgrammerDan <programmerdan@gmail.com>
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
		String dbname = config.getString("datadase", "banstick");
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

	/*
	 * Rough data model:
	 * 
	 * [playerrecord]:
	 *   pid BIGINT
	 *   name VARCHAR(16)
	 *   uuid VARCHAR(36)
	 *   vpn_pardon_time TIMESTAMP
	 *   shared_pardon_time TIMESTAMP
	 * 
	 * [sessionrecords]:
	 *   join_time TIMESTAMP
	 *   leave_time TIMESTAMP
	 *   pid BIGINT
	 *   ip4 VARCHAR(15) -- fixed
	 *   ip4cidr24 VARCHAR(15) -- fixed
	 *   ip4cidr20 VARCHAR(15) -- fixed
	 *   ip4cidr16 VARCHAR(15) -- fixed
	 *   ip6 VARCHAR(39) -- fixed, if available.
	 *   ip6cidr98 VARCHAR(39) -- fixed
	 *  
	 * [banrecords]:
	 *   event_time TIMESTAMP 
	 *   pid BIGINT
	 *   ip4 VARCHAR(15)
	 *   ip4cidr24 VARCHAR(15) -- fixed
	 *   ip4cidr20 VARCHAR(15) -- fixed
	 *   ip4cidr16 VARCHAR(15) -- fixed
	 *   ip6 VARCHAR(39)
	 *   ip6cidr98 VARCHAR(39)
	 *   ban_end TIMESTAMP
	 *   message TEXT
	 *   
	 */

	private void activateDirtySave(ConfigurationSection config) {
		long period = 5*60*1000l;
		long delay = 5*60*1000l;
		if (config != null) {
			period = config.getLong("period", period);
			delay = config.getLong("delay", delay);
		}
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(BanStick.getPlugin(), new Runnable() {
			@Override
			public void run() {
				BSPlayer.saveDirty();
			}
		}, delay, period);

		Bukkit.getScheduler().runTaskTimerAsynchronously(BanStick.getPlugin(), new Runnable() {
			@Override
			public void run() {
				BSBan.saveDirty();
			}
		}, delay + (period / 5), period);
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(BanStick.getPlugin(), new Runnable() {
			@Override
			public void run() {
				BSSession.saveDirty();
			}
		}, delay + ((period * 2) / 5), period);

		Bukkit.getScheduler().runTaskTimerAsynchronously(BanStick.getPlugin(), new Runnable() {
			@Override
			public void run() {
				BSShare.saveDirty();
			}
		}, delay + ((period * 3) / 5), period);

		Bukkit.getScheduler().runTaskTimerAsynchronously(BanStick.getPlugin(), new Runnable() {
			@Override
			public void run() {
				BSVPN.saveDirty();
			}
		}, delay + ((period * 4) / 5), period);

		BanStick.getPlugin().info("Dirty save tasks started.");
	}

	private void activatePreload(ConfigurationSection config) {
		if (config != null && config.getBoolean("enabled")) {
			long period = 5*60*1000l;
			long delay = 5*60*1000l;
			if (config != null) {
				period = config.getLong("period", period);
				delay = config.getLong("delay", delay);
			}
			final int batchsize = config.getInt("batch", 100);
			
			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
					lastId = BSIP.preload(lastId, batchsize);
					if (lastId < 0) this.cancel();
				}
			}.runTaskTimerAsynchronously(BanStick.getPlugin(), delay, period);
			
			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
					lastId = BSVPN.preload(lastId, batchsize);
					if (lastId < 0) this.cancel();
				}
			}.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + (period / 6), period);
			
			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
					lastId = BSBan.preload(lastId, batchsize, false);
					if (lastId < 0) this.cancel();
				}
			}.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + ((period * 2) / 6), period);
			
			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
					lastId = BSPlayer.preload(lastId, batchsize);
					if (lastId < 0) this.cancel();
				}
			}.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + ((period * 3) / 6), period);

			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
					lastId = BSSession.preload(lastId, batchsize);
					if (lastId < 0) this.cancel();
				}
			}.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + ((period * 4) / 6), period);
			
			new BukkitRunnable() {
				private long lastId = 0l;
				@Override
				public void run() {
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
					" pid BIGINT AUTOINCREMENT PRIMARY KEY," +
					" name VARCHAR(16)," +
					" uuid CHAR(36) NOT NULL UNIQUE," +
					" first_add TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" bid REFERENCES bs_ban(bid)," +
					" vpn_pardon_time TIMESTAMP," +
					" shared_pardon_time TIMESTAMP," +
					" INDEX bs_player_name (name)," +
					" INDEX bs_player_vpn_pardons (vpn_pardon_time)," +
					" INDEX bs_player_shared_pardons (shared_pardon_time)," +
					" INDEX bs_player_join (first_add)" +
					");",
					"CREATE TABLE IF NOT EXISTS bs_session (" +
					" sid BIGINT AUTOINCREMENT PRIMARY KEY," +
					" pid BIGINT REFERENCES bs_player(pid)," +
					" join_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" leave_time TIMSTAMP," +
					" iid BIGINT NOT NULL REFERENCES bs_ip(iid)," +
					" INDEX bs_session_pids (pid, join_time, leave_time)" +
					");",
					"CREATE TABLE IF NOT EXISTS bs_ban (" +
					" bid BIGINT AUTOINCREMENT PRIMARY KEY," +
					" ban_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" ip_ban REFERENCES bs_ip(iid)," +
					" vpn_ban REFERENCES bs_vpn(vid)," +
					" share_ban REFERENCES bs_share(sid)," +
					" admin_ban BOOLEAN," +
					" message TEXT," +
					" ban_end TIMESTAMP," +
					" INDEX bs_ban_time (ban_time)," +
					" INDEX bs_ban_ip (ip_ban)" +
					" INDEX bs_ban_vpn (vpn_ban)," +
					" INDEX bs_ban_share (share_ban)," +
					" INDEX bs_ban_end (ban_end)" +
					");",
					"CREATE TABLE IF NOT EXISTS bs_share (" +
					" sid BIGINT AUTOINCREMENT PRIMARY KEY," +
					" create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" first_pid BIGINT NOT NULL REFERENCES bs_player(pid)," +
					" second_pid BIGINT NOT NULL REFERENCES bs_player(pid)," +
					" first_sid BIGINT NOT NULL REFERENCES bs_session(sid)," +
					" second_sid BIGINT NOT NULL REFERENCES bs_session(sid)," +
					" pardon BOOLEAN," +
					" pardon_time TIMESTAMP," +
					" INDEX bs_share (first_pid, second_pid),",
					" INDEX bs_pardon (pardon_time)",
					");",
					"CREATE TABLE IF NOT EXISTS bs_ip (" +
					" iid BIGINT AUTOINCREMENT PRIMARY KEY," +
					" create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" ip4 CHAR(15)," +
					" ip4cidr SMALLINT," +
					" ip6 CHAR(39)," +
					" ip6cidr SMALLINT," +
					" INDEX bs_session_ip4 (ip4, ip4cidr)," +
					" INDEX bs_session_ip6 (ip6, ip6cidr)" +
					");"
				);
		
	}
	
	/**
	 * Add all new migrations here.
	 */
	private void stageUpdates() {
		
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
			bsIP = BSIP.create(netAddress);
		}
		return bsIP;
	}
	
	public BSIP getIP(final InetAddress netAddress) {
		return BSIP.byInetAddress(netAddress);
	}
	
	public List<BSIP> getAllByIP(final InetAddress netAddress) {
		return BSIP.allMatching(netAddress);
	}
	
	// ===== TODO: dirty save schedulers
	
}
