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
	 * Gets either the v4 or v6 address.
	 * 
	 * @return either v4 or v6 address
	 */
	public IPAddress getIPAddress() {
		return basev4 != null ? basev4 : basev6;
	}
	
	public boolean isIPv4() {
		return basev4 != null;
	}
	
	public boolean isIPv6() {
		return basev6 != null;
	}
	
	/**
	 * This method finds all records for which this exact address is a member, based on subnet.
	 * 
	 * @param netAddress IP address to match
	 * @return All the subnet records that match it
	 */
	public static List<BSIP> allMatching(InetAddress netAddress) {
		IPAddress lookup = IPAddress.from(netAddress);
		return allMatching(lookup);
	}
	
	/**
	 * SLOW!!!
	 * 
	 * This method finds all records, CIDR and exact, that are members of this subnet.
	 * 
	 * @param lookup The base IP address to use
	 * @param CIDR The largest subnet to consider.
	 * @return All the IP records contained in the given subnet
	 */
	public static List<BSIP> allContained(IPAddress lookup, int CIDR) {
		List<BSIP> returns = new ArrayList<BSIP>();
		IPAddress newBase = lookup.toSubnet(CIDR);
		StringBuilder sb = new StringBuilder("SELECT * FROM bs_ip WHERE ");
		if (newBase.isIPv4()) {
			sb.append("ip4 LIKE \"").append(newBase.toSQLWildcardString()).append("\"");
		} else {
			newBase.getNetworkSection(CIDR).getStartsWithSQLClause(sb, "ip6");
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getIP = connection.prepareStatement(sb.toString());
				ResultSet rs = getIP.executeQuery();) {
			while (rs.next()) {
				BSIP bsip = new BSIP();
				bsip.iid = rs.getLong(1);
				if (allIPId.containsKey(bsip.iid)) {
					returns.add(allIPId.get(bsip.iid));
					continue;
				}
				
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
				
				if (!BSIP.allIPId.containsKey(bsip.iid)) {
					BSIP.allIPId.put(bsip.iid, bsip);
				}
				returns.add(bsip);
			}

		} catch (SQLException e) {
			BanStick.getPlugin().severe("Failed during search for IPs contained by " + newBase.toCanonicalString(), e);
		}
		return returns;
	}
	
	/**
	 * @see #allMatching(InetAddress)
	 * @param lookup The IP address to look up
	 * @return All matching subnet records
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
	 * Expands CIDR restrictions for subnet matching -- find all networks containing this network
	 * 
	 * @see #allMatching(InetAddress)
	 * 
	 * @param lookup The ipaddress as root of the subnet
	 * @param CIDR The CIDR to begin as smallest subnet
	 * @return The subnet records containing this subnet
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
	 * Starting at max CIDR passed in and moving towards largest, check for host matches.
	 * These can then be used to check for bans on those matching host networks.
	 * 
	 * Shorthand: Looks to find host networks for the specified IP/subnet that have IP table entries
	 * to use for ban-lookups.
	 * 
	 * I recommend this be non-synchronous!
	 * 
	 * @param lookup The IPAddress to lookup from (root, smallest subnet base IP)
	 * @param maxCIDR The smallest CIDR to consider
	 * @param matches The List to fill with foud matches
	 */
	private static void fillFromCIDRs(IPAddress lookup, int maxCIDR, List<BSIP> matches) {
		for (int cCIDR = maxCIDR; cCIDR > 0; cCIDR--) {
			BSIP prospect = byCIDR(lookup.toSubnet(cCIDR).getLower().toString(), cCIDR);
			if (prospect != null) {
				matches.add(prospect);
			}
		}
	}
	
	
	/**
	 * Exact lookup of netAddress by CIDR (InetAddress version)
	 * 
	 * @param netAddress The IP Address to lookup from (root, smallest subnet base IP)
	 * @param CIDR The CIDR to match
	 * @return The IPAddress/CIDR match, if found.
	 */
	public static BSIP byCIDR(InetAddress netAddress, int CIDR) {
		return BSIP.byCIDR(netAddress.getHostAddress(), CIDR);
	}
	
	/**
	 * Exact lookup of netAddress by CIDR
	 * @param netAddress The IPAddress as a string to match
	 * @param CIDR The subnet CIDR to match
	 * @return The exact matching IP record or null if not found.
	 */
	public static BSIP byCIDR(String netAddress, int CIDR) {
		IPAddressString ips = new IPAddressString(netAddress + "/" + CIDR);
		//BanStick.getPlugin().debug("Check for CIDR IP: {0}", ips.toString());
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
			getIP.setString(1, lookup.toSubnet(CIDR).getLower().toString());
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
	 * @param netAddress Exact IPAddress to match
	 * @return The matching IP or null
	 */
	public static BSIP byInetAddress(InetAddress netAddress) {
		IPAddress lookup = IPAddress.from(netAddress);
		return byIPAddress(lookup);
	}
	
	public static BSIP byIPAddress(IPAddress lookup) {
		//BanStick.getPlugin().debug("Check for IP: {0}", lookup.toString());
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
	 * @param iid The IP database ID
	 * @return The exact IP data record or null if not found
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
			if (BSIP.allIPId.containsKey(bsip.iid)) {
				return BSIP.allIPId.get(bsip.iid);
			}
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
			//BanStick.getPlugin().warning("Failed to find IP"); 
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
						"INSERT INTO bs_ip(ip4, ip4cidr, ip6, ip6cidr, create_time) VALUES (?, ?, ?, ?, ?);",
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
					if (BanStick.getPlugin().getIPDataHandler() != null) {
						BanStick.getPlugin().getIPDataHandler().offer(newIP);
					}
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
		IPAddress lookup = IPAddress.from(netAddress).toSubnet(CIDR).getLower();
		return create(lookup, CIDR);
	}
	
	public static BSIP create(IPAddress lookup, int CIDR) {
		// TODO: reconsider the CIDR handling in the caching and saving and such.
		//   might not be necessary to separate out the CIDR like I am here.
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
					if (BanStick.getPlugin().getIPDataHandler() != null && (newIP.basev4 == null ? CIDR == 128 : CIDR == 32)) {
						BanStick.getPlugin().getIPDataHandler().offer(newIP);
					}
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
		long maxId = -1;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement loadIPs = connection.prepareStatement("SELECT * FROM bs_ip WHERE iid > ? ORDER BY iid LIMIT ?");) {
			loadIPs.setLong(1, offset);
			loadIPs.setInt(2, limit);
			try (ResultSet rs = loadIPs.executeQuery()) {
				while (rs.next()) {
					BSIP bsip = new BSIP();
					bsip.iid = rs.getLong(1);
					if (bsip.iid > maxId) maxId = bsip.iid;
					if (BSIP.allIPId.containsKey(bsip.iid)) { // already cached.
						continue;
					}
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
					
					if (!BSIP.allIPId.containsKey(bsip.iid)) {
						BSIP.allIPId.put(bsip.iid, bsip);
					}
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed during IP preload, offset " + offset + " limit " + limit, se);
		}
		return maxId;
	}
	
	@Override
	public String toString() {
		if (basev4 != null) {
			return basev4.toCanonicalString();
		} else {
			return basev6.toCanonicalString();
		}
	}
	
	public String toFullString(boolean showIPs) {
		return showIPs ? toString() : String.valueOf(this.iid);
	}
}
