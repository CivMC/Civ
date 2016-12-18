package com.programmerdan.minecraft.banstick.handler;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.programmerdan.minecraft.banstick.BanStick;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

/**
 * Ties into the managed datasource processes of the CivMod core plugin.
 * 
 * @author ProgrammerDan <programmerdan@gmail.com>
 *
 */
public class BanStickDatabaseHandler {

	/*
	 * Rough data model:
	 * 
	 * [playerrecord]:
	 *   pid BIGINTEGER
	 *   name VARCHAR(16)
	 *   uuid VARCHAR(36)
	 *   vpn_pardon_time TIMESTAMP
	 *   shared_pardon_time TIMESTAMP
	 * 
	 * [sessionrecords]:
	 *   join_time TIMESTAMP
	 *   leave_time TIMESTAMP
	 *   pid BIGINTEGER
	 *   ip4 VARCHAR(15) -- fixed
	 *   ip4cidr24 VARCHAR(15) -- fixed
	 *   ip4cidr20 VARCHAR(15) -- fixed
	 *   ip4cidr16 VARCHAR(15) -- fixed
	 *   ip6 VARCHAR(39) -- fixed, if available.
	 *   ip6cidr98 VARCHAR(39) -- fixed
	 *  
	 * [banrecords]:
	 *   event_time TIMESTAMP 
	 *   pid BIGINTEGER
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
	
	private ManagedDatasource data;
	
	public BanStickDatabaseHandler(FileConfiguration config) {
		if (!configureData(config.getConfigurationSection("database"))) {
			throw new RuntimeException("Failed to configure Database for BanStick!");
		}
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
		return true;

	}
	
	
}
