package com.programmerdan.minecraft.banstick.data;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4Address;
import inet.ipaddr.ipv6.IPv6Address;

public class BSIP {
	private static Map<IPAddress, BSIP> allIPNA = new HashMap<IPAddress, BSIP>();
	private static Map<Long, BSIP> allIPId = new HashMap<Long, BSIP>();
	
	private long iid;
	private Timestamp createTime;

	private IPv4Address basev4;
	private IPv6Address basev6;
	
	private BSIP() {}
	
	public long getId() {
		return iid;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	
	public IPv4Address getIPv4Address() {
		return basev4;
	}
	
	public IPv6Address getIPv6Address() {
		return basev6;
	}
	
	/**
	 * Basically, this method finds all records for which this exact address is a member, based on subnet.
	 * 
	 * @param netAddress
	 * @return
	 */
	public static List<BSIP> allMatching(InetAddress netAddress) {
		IPAddress lookup = IPAddress.from(netAddress);
		return allMatching(lookup);
	}
	
	/**
	 * @see #allMatching(InetAddress)
	 * @param lookup
	 * @return
	 */
	public static List<BSIP> allMatching(IPAddress lookup) {
		List<BSIP> matches = new ArrayList<BSIP>();
		if (lookup.isIPv4()) {
			fillFromCIDRs(lookup, 32, matches);
		} else if (lookup.isIPv6()) {
			fillFromCIDRs(lookup, 128, matches);
		}
		return matches;
	}
	
	/**
	 * @see #allMatching(InetAddress)
	 * 
	 * Expands CIDR restrictions for subnet matching.
	 * 
	 * @param lookup
	 * @return
	 */
	public static List<BSIP> allMatching(IPAddress lookup, int CIDR) {
		List<BSIP> matches = new ArrayList<BSIP>();
		if (lookup.isIPv4()) {
			fillFromCIDRs(lookup, CIDR, matches);
		} else if (lookup.isIPv6()) {
			fillFromCIDRs(lookup, CIDR, matches);
		}
		return matches;
	}	
	/**
	 * Starting at max CIDR passed in and moving towards lowest, check for host matches.
	 * These can then be used to check for bans on those matching host networks.
	 * 
	 * Shorthand: Looks to find host networks for the specified IP/subnet that have IP table entries
	 * to use for ban-lookups.
	 * 
	 * I recommend this be non-synchronous!
	 * 
	 * @param lookup
	 * @param maxCIDR
	 * @param matches
	 */
	private static void fillFromCIDRs(IPAddress lookup, int maxCIDR, List<BSIP> matches) {
		for (int cCIDR = maxCIDR; cCIDR > 0; cCIDR--) {
			BSIP prospect = byCIDR(lookup.toSubnet(cCIDR).getLowest().toString(), cCIDR);
			if (prospect != null) {
				matches.add(prospect);
			}
		}
	}
	
	
	/**
	 * Exact lookup of netAddress by CIDR (InetAddress version)
	 * 
	 * @param netAddress
	 * @param CIDR
	 * @return
	 */
	public static BSIP byCIDR(InetAddress netAddress, int CIDR) {
		return BSIP.byCIDR(netAddress.getHostAddress(), CIDR);
	}
	
	/**
	 * Exact lookup of netAddress by CIDR
	 * @param netAddress
	 * @param CIDR
	 * @return
	 */
	public static BSIP byCIDR(String netAddress, int CIDR) {
		IPAddressString ips = new IPAddressString(netAddress + "/" + CIDR);
		IPAddress lookup = ips.getAddress();
		if (allIPNA.containsKey(lookup)) {
			return allIPNA.get(lookup);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();) {
			PreparedStatement getIP = null;
			if (lookup.isIPv4()) {
				getIP = connection.prepareStatement("SELECT * FROM bs_ip WHERE ip4 = ? and ip4cidr = ?");
			} else if (lookup.isIPv6()) {
				getIP = connection.prepareStatement("SELECT * FROM bs_ip WHERE ip6 = ? and ip6cidr = ?");
			} else { 
				BanStick.getPlugin().severe("Unknown Inet address type: " + netAddress.toString());
				return null;
			}
			getIP.setString(1, lookup.toSubnet(CIDR).getLowest().toString());
			getIP.setInt(2, CIDR);
			
			BSIP bsip = null;
			try (ResultSet rs = getIP.executeQuery();) {
				bsip = internalGetResult(rs);
			}
			getIP.close();
			return bsip;
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Unable to retrieve BPID: " + netAddress + "/" + CIDR, se);
		}
		return null;
	}
	
	/**
	 * Get IP by exact Inet Address IP. Either matches or fails.
	 * 
	 * @param netAddress
	 * @return
	 */
	public static BSIP byInetAddress(InetAddress netAddress) {
		IPAddress lookup = IPAddress.from(netAddress);
		return byIPAddress(lookup);
	}
	
	public static BSIP byIPAddress(IPAddress lookup) {
		if (allIPNA.containsKey(lookup)) {
			return allIPNA.get(lookup);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();) {
			PreparedStatement getIP = null;
			if (lookup.isIPv4()) {
				getIP = connection.prepareStatement("SELECT * FROM bs_ip WHERE ip4 = ? and ip4cidr = 32");
				getIP.setString(1, lookup.toString());
			} else if (lookup.isIPv6()) {
				getIP = connection.prepareStatement("SELECT * FROM bs_ip WHERE ip6 = ? and ip6cidr = 128");
				getIP.setString(1,  lookup.toString());
			} else { 
				BanStick.getPlugin().severe("Unknown Inet address type: " + lookup.toString());
				return null;
			}
			BSIP bsip = null;
			try (ResultSet rs = getIP.executeQuery();) {
				bsip = internalGetResult(rs);
			}
			getIP.close();
			return bsip;
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to execute query to get IP: " + lookup.toString(), se);
		}
		
		return null;
	}

	/**
	 * Get IP by exact ID. Either matches or fails.
	 * 
	 * @param iid
	 * @return
	 */
	public static BSIP byId(long iid) {
		if (allIPId.containsKey(iid)) {
			return allIPId.get(iid);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getIP = connection.prepareStatement("SELECT * FROM bs_ip WHERE iid = ?");) {
			getIP.setLong(1, iid);
			try (ResultSet rs = getIP.executeQuery();) {
				return internalGetResult(rs);
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Unable to retrieve BPID: " + iid, se);
		}
		return null; // TODO: exception
	}
	
	private static BSIP internalGetResult(ResultSet rs) throws SQLException {
		if (rs.next()) {
			// found
			BSIP bsip = new BSIP();
			bsip.iid = rs.getLong(1);
			bsip.createTime = rs.getTimestamp(2);
			String ipv4 = rs.getString(3);
			int ipv4cidr = rs.getInt(4);
			if (rs.wasNull()) ipv4cidr = 32;
			
			String ipv6 = rs.getString(5);
			int ipv6cidr = rs.getInt(6);
			if (rs.wasNull()) ipv6cidr = 128;
			
			if (ipv4 != null) { // ipv4 specific entry. Typical for player-entries.
				IPAddressString ips = new IPAddressString(ipv4 + "/" + ipv4cidr);
				if (ips.isIPv4()) {
					bsip.basev4 = ips.getAddress().toIPv4();
					BSIP.allIPNA.put(bsip.basev4, bsip);
				} else {
					BanStick.getPlugin().warning("Conversion of ipv4 address to ipv4 failed??: " + bsip.iid + " - " + ipv4);
					return null; // TODO: exception
				}
			} else if (ipv6 != null) { //ipv6 specific entry.
				IPAddressString ips = new IPAddressString(ipv6 + "/" + ipv6cidr);
				if (ips.isIPv6()) {
					bsip.basev6 = ips.getAddress().toIPv6();
					BSIP.allIPNA.put(bsip.basev6, bsip);
				} else {
					BanStick.getPlugin().warning("Conversion of ipv6 address to ipv6 failed??: " + bsip.iid + " - " + ipv6);
					return null; // TODO: exception
				}
			} else {
				BanStick.getPlugin().warning("Empty ip entry?!: " + bsip.iid);
				return null; // TODO: exception
			}
			
			BSIP.allIPId.put(bsip.iid, bsip);
			
			return bsip;
		} else {
			BanStick.getPlugin().warning("Failed to find IP"); 
			return null; // TODO: exception 
		}
	}

	public static BSIP create(InetAddress netAddress) {
		IPAddress lookup = IPAddress.from(netAddress);
		return create(lookup);
	}
	
	public static BSIP create(IPAddress lookup) {
		if (allIPNA.containsKey(lookup)) {
			return allIPNA.get(lookup);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"INSERT INTO bs_ip(ip4, ip4cidr, ip6, ip6cidr, createTime) VALUES (?, ?, ?, ?, ?);",
						Statement.RETURN_GENERATED_KEYS);) {
			BSIP newIP = new BSIP();
			if (lookup.isIPv4()) {
				statement.setNull(3, Types.CHAR);
				statement.setNull(4, Types.SMALLINT);
				statement.setString(1, lookup.toString());
				statement.setInt(2, 32);
				newIP.basev4 = lookup.toIPv4();
			} else {
				statement.setNull(1, Types.CHAR);
				statement.setNull(2, Types.SMALLINT);
				statement.setString(3, lookup.toString());
				statement.setInt(4, 128);
				newIP.basev6 = lookup.toIPv6();
			}
			newIP.createTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
			statement.setTimestamp(5, newIP.createTime);
			
			int ins = statement.executeUpdate();
			if (ins < 1) {
				BanStick.getPlugin().warning("Insert reported nothing inserted? " + lookup.toString());
			}
			
			try (ResultSet rs = statement.getGeneratedKeys()) {
				if (rs.next()) {
					long iid = rs.getLong(1);
					newIP.iid = iid;
					BSIP.allIPId.put(iid, newIP);
					BSIP.allIPNA.put((newIP.basev4 == null ? newIP.basev6 : newIP.basev4), newIP);
					return newIP;
				} else {
					BanStick.getPlugin().severe("Failed to get ID from inserted record!? " + lookup.toString());
					return null;
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to create IP from " + lookup.toString(), se);
		}
		return null;
	}
	
	public static BSIP create(InetAddress netAddress, int CIDR) {
		IPAddress lookup = IPAddress.from(netAddress).toSubnet(CIDR).getLowest();
		return create(lookup, CIDR);
	}
	
	public static BSIP create(IPAddress lookup, int CIDR) {
		if (allIPNA.containsKey(lookup)) {
			return allIPNA.get(lookup);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"INSERT INTO bs_ip(ip4, ip4cidr, ip6, ip6cidr, create_time) VALUES (?, ?, ?, ?, ?);",
						Statement.RETURN_GENERATED_KEYS);) {
			BSIP newIP = new BSIP();
			if (lookup.isIPv4()) {
				statement.setNull(3, Types.CHAR);
				statement.setNull(4, Types.SMALLINT);
				statement.setString(1, lookup.toString());
				statement.setInt(2, CIDR);
				newIP.basev4 = lookup.toIPv4();
			} else {
				statement.setNull(1, Types.CHAR);
				statement.setNull(2, Types.SMALLINT);
				statement.setString(3, lookup.toString());
				statement.setInt(4, CIDR);
				newIP.basev6 = lookup.toIPv6();
			}
			newIP.createTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
			statement.setTimestamp(5, newIP.createTime);
			
			int ins = statement.executeUpdate();
			if (ins < 1) {
				BanStick.getPlugin().warning("Insert reported nothing inserted? " + lookup.toString() + "/" + CIDR);
			}
			
			try (ResultSet rs = statement.getGeneratedKeys()) {
				if (rs.next()) {
					long iid = rs.getLong(1);
					newIP.iid = iid;
					BSIP.allIPId.put(iid, newIP);
					BSIP.allIPNA.put((newIP.basev4 == null ? newIP.basev6 : newIP.basev4), newIP);
					return newIP;
				} else {
					BanStick.getPlugin().severe("Failed to get ID from inserted record!? " + lookup.toString() + "/" + CIDR);
					return null;
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to create IP from " + lookup.toString() + "/" + CIDR, se);
		}
		return null;
	}
	
	public static long preload(long offset, int limit) {
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement loadIPs = connection.prepareStatement("SELECT * FROM bs_ip ORDER BY bid OFFSET ? LIMIT ?");) {
			loadIPs.setLong(1, offset);
			loadIPs.setInt(2, limit);
			try (ResultSet rs = loadIPs.executeQuery()) {
				long maxId = -1;
				while (rs.next()) {
					BSIP bsip = new BSIP();
					bsip.iid = rs.getLong(1);
					bsip.createTime = rs.getTimestamp(2);
					String ipv4 = rs.getString(3);
					int ipv4cidr = rs.getInt(4);
					if (rs.wasNull()) ipv4cidr = 32;
					
					String ipv6 = rs.getString(5);
					int ipv6cidr = rs.getInt(6);
					if (rs.wasNull()) ipv6cidr = 128;
					
					if (ipv4 != null) { // ipv4 specific entry. Typical for player-entries.
						IPAddressString ips = new IPAddressString(ipv4 + "/" + ipv4cidr);
						if (ips.isIPv4()) {
							bsip.basev4 = ips.getAddress().toIPv4();
							if (!BSIP.allIPNA.containsKey(bsip.basev4)) {
								BSIP.allIPNA.put(bsip.basev4, bsip);
							}
						} else {
							BanStick.getPlugin().warning("Conversion of ipv4 address to ipv4 failed??: " + bsip.iid + " - " + ipv4);
							continue;
							// TODO: exception
						}
					} else if (ipv6 != null) { //ipv6 specific entry.
						IPAddressString ips = new IPAddressString(ipv6 + "/" + ipv6cidr);
						if (ips.isIPv6()) {
							bsip.basev6 = ips.getAddress().toIPv6();
							if (!BSIP.allIPNA.containsKey(bsip.basev6)) {
								BSIP.allIPNA.put(bsip.basev6, bsip);
							}
						} else {
							BanStick.getPlugin().warning("Conversion of ipv6 address to ipv6 failed??: " + bsip.iid + " - " + ipv6);
							continue;
							// TODO: exception
						}
					} else {
						BanStick.getPlugin().warning("Empty ip entry?!: " + bsip.iid);
						continue;
						// TODO: exception
					}
					
					if (BSIP.allIPId.containsKey(bsip.iid)) {
						BSIP.allIPId.put(bsip.iid, bsip);
					}
					if (bsip.iid > maxId) maxId = bsip.iid;
				}
				return maxId;
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed during IP preload, offset " + offset + " limit " + limit, se);
		}
		return -1;
	}
}
