package com.programmerdan.minecraft.banstick.importer;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.handler.ImportWorker;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.NameAPI;

/**
 * Worker for IpCheck plugin's import.
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class IpCheckImportWorker extends ImportWorker {

	private ManagedDatasource internalDatabase;
	
	public IpCheckImportWorker(ConfigurationSection config) {
		super(config);
	}

	@Override
	public boolean internalSetup(ConfigurationSection config) {
		if (config == null) {
			return false;
		}
		ConfigurationSection internal = config.getConfigurationSection("database");
		if (internal != null) {
			return provisionDatabase(internal);
		} else {
			return false;
		}
	}
	
	private boolean provisionDatabase(ConfigurationSection config) {
		String host = config.getString("host", "localhost");
		int port = config.getInt("port", 3306);
		String dbname = config.getString("database", "ipcheck");
		String username = config.getString("user");
		String password = config.getString("password");
		int poolsize = config.getInt("poolsize", 3);
		long connectionTimeout = config.getLong("connection_timeout", 10000L);
		long idleTimeout = config.getLong("idle_timeout", 600000L);
		long maxLifetime = config.getLong("max_lifetime", 7200000L);
		try {
			internalDatabase = new ManagedDatasource(BanStick.getPlugin(), username, password, host, port, dbname,
					poolsize, connectionTimeout, idleTimeout, maxLifetime);
			internalDatabase.getConnection().close();
		} catch (Exception se) {
			BanStick.getPlugin().info("Failed to initialize Database connection");
			return false;
		}
		
		return true;
	}

	@Override
	public void doImport() {
	
		// plan will be, create IP records; establish bans if ban is indicated.
		// So, first, ipcheck_ip -- create any IP records not already known. If known, do nothing.
		//		If banned, make a ban record for that IP
		// Second, ipcheck_user -- create any Users not already known. If known and not banned, 
		//		but marked banned, ban w/ message.
		//   If exempted, rejoined exempted, or protected, provide pardons as appropriate
		// Third, ipcheck_log -- create sessions for users + ips for the times given.
		try {
			doImportIPs();
			Thread.sleep(0);
			doImportPlayers();
			Thread.sleep(0);
			doImportSessions();
			Thread.sleep(0);
		} catch (Exception e) {
			BanStick.getPlugin().severe("Failed during IP-Check import, potentially incomplete import: ", e);
		}
	}

	@Override
	public String name() {
		return "ipcheck";
	}
	
	private void doImportIPs() {
		try (Connection connection = internalDatabase.getConnection();
				PreparedStatement getIPs = connection.prepareStatement(
						"SELECT * FROM ipcheck_ip ORDER BY `timestamp` LIMIT ?, ?");) {
			/*
			 * CREATE TABLE `ipcheck_ip` (
	  `ip` varchar(15) NOT NULL,
	  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	  `banned` bit(1) NOT NULL DEFAULT b'0',
	  `exempted` bit(1) NOT NULL DEFAULT b'0',
	  `rejoinexempt` bit(1) NOT NULL DEFAULT b'0',
	  PRIMARY KEY (`ip`)
			 */
			long offset = 0;
			int limit = 100;
			boolean cont = true;
			while (cont) {
				getIPs.clearParameters();
				getIPs.setLong(1, offset);
				getIPs.setInt(2, limit);
				try (ResultSet rs = getIPs.executeQuery();) {
					int thisCycle = 0;
					while (rs.next()) {
						// only ip and ban state is leveraged here.
						String ip = rs.getString(1);
						//Timestamp firstUse = rs.getTimestamp(2);
						boolean isBanned = rs.getBoolean(3);
						//boolean isJoinExempt = rs.getBoolean(4);
						//boolean isWarnExempt = rs.getBoolean(5);
						
						try {
							IPAddressString address = new IPAddressString(ip);
							IPAddress exactAddress = address.toAddress();
							
							BSIP exactIP = BSIP.byIPAddress(exactAddress);
							if (exactIP == null) {
								exactIP = BSIP.create(exactAddress);
							}
							if (isBanned) {
								BSBan.create(exactIP, "Banned for Multiaccounting", null, true);
							}
						} catch (Exception e) {
							BanStick.getPlugin().warning("Found invalid IP {0} in IP-Check database", ip);
						}
						thisCycle++;
					}
					if (thisCycle == 0) { // all done!
						cont = false;
					} else {
						offset += limit;
					}
				}
				Thread.sleep(0L);
			}
		} catch (SQLException | InterruptedException se) {
			BanStick.getPlugin().severe("Failed to import IPs from IP-Check, potentially incomplete import: ", se);
		}
	}
	
	private void doImportPlayers() {
		try (Connection connection = internalDatabase.getConnection();
				PreparedStatement getIPs = connection.prepareStatement(
						"SELECT * FROM ipcheck_user ORDER BY `timestamp` LIMIT ?, ?");) {
			/*
			 *  CREATE TABLE `ipcheck_user` (
	  `username` varchar(255) NOT NULL,
	  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	  `banmessage` varchar(255) DEFAULT NULL,
	  `banned` bit(1) NOT NULL DEFAULT b'0',
	  `exempted` bit(1) NOT NULL DEFAULT b'0',
	  `rejoinexempt` bit(1) NOT NULL DEFAULT b'0',
	  `protected` bit(1) NOT NULL DEFAULT b'0',
	  PRIMARY KEY (`username`)
			 */
			long offset = 0;
			int limit = 100;
			boolean cont = true;
			while (cont) {
				getIPs.clearParameters();	
				getIPs.setLong(1, offset);
				getIPs.setInt(2, limit);
				try (ResultSet rs = getIPs.executeQuery();) {
					int thisCycle = 0;
					while (rs.next()) {
						// ignore fields not used in import.
						String username = rs.getString(1);
						//Timestamp firstJoin = rs.getTimestamp(2);
						String banMessage = rs.getString(3);
						//boolean isWarnExempt = rs.getBoolean(6);
						
						UUID uuid = null;
						try {
							uuid = NameAPI.getUUID(username);
						} catch (NoClassDefFoundError ncde) { }
						
						if (uuid == null) {
							Player bukkitPlayer = Bukkit.getPlayerExact(username);
							if (bukkitPlayer != null) {
								uuid = bukkitPlayer.getUniqueId();
							}
						}
						if (uuid == null) {
							BanStick.getPlugin().warning(
									"Unable to find UUID for player {0} while importing IPCheck", username);
							continue;
						}
						BSPlayer player = BSPlayer.byUUID(uuid);
						
						if (player == null) {
							player = BSPlayer.create(uuid, username);
						}
						
						if (player == null) {
							BanStick.getPlugin().warning(
									"Failure making player with UUID {0} and name {1}", uuid, username);
						}
						
						boolean isBanned = rs.getBoolean(4);
						if (isBanned) {
							BSBan ban = null;
							if (banMessage != null && banMessage.length() > 0) {
								ban = BSBan.create(banMessage, null, true);								
							} else {
								ban = BSBan.create("Banned for Multiaccounting", null, true);
							}
							
							player.setBan(ban);
						}
						
						boolean isJoinExempt = rs.getBoolean(5);
						if (isJoinExempt) {
							player.setSharedPardonTime(new Date());
						}
						boolean isProtected = rs.getBoolean(7);
						if (isProtected) {
							player.setIPPardonTime(new Date());
							player.setSharedPardonTime(new Date());
						}

						thisCycle++;
					}
					if (thisCycle == 0) { // all done!
						cont = false;
					} else {
						offset += limit;
					}
				}
				Thread.sleep(0L);
			}
		} catch (SQLException | InterruptedException se) {
			BanStick.getPlugin().severe("Failed to import Players from IP-Check, potentially incomplete import: ", se);
		}		
	}
	
	private void doImportSessions() {
		try (Connection connection = internalDatabase.getConnection();
				PreparedStatement getIPs = connection.prepareStatement(
						"SELECT * FROM ipcheck_log ORDER BY `timestamp` LIMIT ?, ?");) {
			/*
			 * CREATE TABLE `ipcheck_log` (
	  `ip` varchar(15) NOT NULL,
	  `username` varchar(255) NOT NULL,
	  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	  PRIMARY KEY (`ip`,`username`)
			 */		
			long offset = 0;
			int limit = 100;
			boolean cont = true;
			while (cont) {
				getIPs.clearParameters();
				getIPs.setLong(1, offset);
				getIPs.setInt(2, limit);
				try (ResultSet rs = getIPs.executeQuery();) {
					int thisCycle = 0;
					while (rs.next()) {
						String ip = rs.getString(1);
						String username = rs.getString(2);
						Timestamp sessionStart = rs.getTimestamp(3);
						
						try {
							UUID uuid = null;
							try {
								uuid = NameAPI.getUUID(username);
							} catch (NoClassDefFoundError ncde) { }
							
							if (uuid == null) {
								Player bukkitPlayer = Bukkit.getPlayerExact(username);
								if (bukkitPlayer != null) {
									uuid = bukkitPlayer.getUniqueId();
								}
							}
							if (uuid == null) {
								BanStick.getPlugin().warning(
										"Unable to find UUID for player {0} while importing IPCheck", username);
								continue;
							}
							BSPlayer player = BSPlayer.byUUID(uuid);
							IPAddressString address = new IPAddressString(ip);
							IPAddress exactAddress = address.toAddress();
							BSIP exactIP = BSIP.byIPAddress(exactAddress);
							if (player == null || exactIP == null) {
								BanStick.getPlugin().warning(
										"Failed to find Player {0} or IP {1} from import, unable to import session time {2}",
										username, ip, sessionStart);
							} else {
								player.startSession(exactIP, sessionStart);
								player.endSession(new Date(sessionStart.getTime() + 1000L)); // default of 1 second.
							}
						} catch (Exception e) {
							BanStick.getPlugin().warning("Found invalid data username {0} ip {1} in IP-Check database", 
									username, ip);
						}
						thisCycle++;
					}
					if (thisCycle == 0) { // all done!
						cont = false;
					} else {
						offset += limit;
					}
				}
				Thread.sleep(0L);
			}
		} catch (SQLException | InterruptedException se) {
			BanStick.getPlugin().severe("Failed to import IPs from IP-Check, potentially incomplete import: ", se);
		}
	}

}
