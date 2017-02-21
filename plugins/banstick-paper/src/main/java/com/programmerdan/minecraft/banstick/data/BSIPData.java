package com.programmerdan.minecraft.banstick.data;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;

import inet.ipaddr.IPAddress;

public class BSIPData {
	private static Map<Long, BSIPData> allIPDataID = new HashMap<Long, BSIPData>();
	private static ConcurrentLinkedQueue<WeakReference<BSIPData>> dirtyIPData = new ConcurrentLinkedQueue<WeakReference<BSIPData>>();
	private boolean dirty;
	
	private BSIPData() {}
	/*
	 * bs_ip_data
	 * 
	 * 					" idid BIGINT AUT_INCREMENT PRIMARY KEY," +
					" iid BIGINT NOT NULL REFERENCE bs_ip(iid)," +
					" create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" valid BOOLEAN," +
					" continent TEXT," + 
					" country TEXT," +
					" region TEXT," +
					" city TEXT," +
					" postal TEXT," +
					" domain TEXT," +
					" provider TEXT," +
					" registered_as TEXT," +
					" connection TEXT," +
					" proxy FLOAT," +
					" source TEXT," +
					" comment TEXT," + 
	 */
	
	private long idid;
	private BSIP iid;
	private Timestamp createTime;
	private boolean valid; // mutable
	private String continent;
	private String country;
	private String region;
	private String postal;
	private String domain;
	private String registeredAs;
	private String connection;
	private float proxy; // mutable
	private String source; // mutable
	private String comment; // mutable
	
	public long getId() {
		return this.idid;
	}
	
	public void invalidate() {
		if (this.valid) {
			this.valid = false;
			dirtyIPData.offer(new WeakReference<BSIPData>(this));
		}
	}
	
	public boolean isValid() {
		return this.valid;
	}
	
	public Date getCreateTime() {
		return this.createTime;
	}
	
	public BSIP getIP() {
		return this.iid;
	}
	
	public String getContinent() {
		return this.continent;
	}
	
	public String getCountry() {
		return this.country;
	}
	
	public String getRegion() {
		return this.region;
	}
	
	public String getPostal() {
		return this.postal;
	}
	
	public String getDomain() {
		return this.domain;
	}
	
	public String getRegisteredAs() {
		return this.registeredAs;
	}
	
	public String getConnection() {
		return this.connection;
	}
	
	public float getProxy() {
		return this.proxy;
	}
	
	public void setProxy(float proxy) {
		this.proxy = proxy;
		dirtyIPData.offer(new WeakReference<BSIPData>(this));
	}
	
	public String getSource() {
		return this.source;
	}
	
	public void setSource(String source) {
		this.source = source;
		dirtyIPData.offer(new WeakReference<BSIPData>(this));
		
	}
	
	public String getComment() {
		return this.comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
		dirtyIPData.offer(new WeakReference<BSIPData>(this));
	}

	public static BSIPData byId(long idid) {
		if (allIPDataID.containsKey(idid)) {
			return allIPDataID.get(idid);
		}
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getId = connection.prepareStatement("SELECT * FROM bs_ip_data WHERE idid = ?");) {
			getId.setLong(1, idid);
			try (ResultSet rs = getId.executeQuery();) {
				if (rs.next()) {
					BSIPData data = extractData(rs);
					allIPDataID.put(idid,  data);
					return data;
				} else {
					BanStick.getPlugin().warning("Failed to retrieve IP Data by id: " + idid + " - not found");
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Retrieval of IP Data by ID failed: " + idid, se);
		}
		return null;
	}
	
	private static BSIPData extractData(ResultSet rs) throws SQLException {
		BSIPData data = new BSIPData();
		data.idid = rs.getLong(1);
		data.iid = BSIP.byId(rs.getLong(2));
		data.createTime = rs.getTimestamp(3);
		data.valid = rs.getBoolean(4);
		data.continent = rs.getString(5);
		data.country = rs.getString(6);
		data.region = rs.getString(7);
		data.postal = rs.getString(8);
		data.domain = rs.getString(9);
		data.registeredAs = rs.getString(10);
		data.connection = rs.getString(11);
		data.proxy = rs.getFloat(12);
		data.source = rs.getString(13);
		data.comment = rs.getString(14);
		data.dirty = false;
		return data;
	}

	/**
	 * Returns only the latest valid BSIPData records by exact match with the IP
	 * @param ip
	 * @return
	 */
	public static BSIPData byExactIP(BSIP ip) {
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement getId = connection.prepareStatement("SELECT * FROM bs_ip_data WHERE iid = ? and valid = true ORDER BY create_time DESC LIMIT 1");) {
			getId.setLong(1, ip.getId());
			try (ResultSet rs = getId.executeQuery();) {
				if (rs.next()) {
					if (allIPDataID.containsKey(rs.getLong(1))) {
						return allIPDataID.get(rs.getLong(1));
					}
					BSIPData data = extractData(rs);
					allIPDataID.put(data.idid,  data);
					return data;
				} else {
					BanStick.getPlugin().warning("Failed to retrieve IP Data by exact IP: {0} - not found", ip);
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Retrieval of IP Data by exact IP failed: " + ip, se);
		}
		return null;
	}
	
	/**
	 * Returns the smallest (most bits) BSIPData that contains the given IP/CIDR.
	 * 
	 * @param ip
	 * @return
	 */
	public static BSIPData byContainsIP(BSIP ip) {
		IPAddress address = ip.getIPAddress();
		List<BSIP> knownContains = BSIP.allMatching(address, address.getNetworkPrefixLength());
		
		for (BSIP maybe : knownContains) {
			BSIPData data = byExactIP(maybe);
			if (data != null) {
				return data;
			}
		}
		BanStick.getPlugin().warning("No IPData records contain IP {0}", ip);
		return null;
	}
	
	/**
	 * Returns all BSIPData that relate to or contain the given IP/CIDR.
	 * 
	 * @param ip
	 * @return a list; empty if nothing found.
	 */
	public static List<BSIPData> allByIP(BSIP ip) {
		List<BSIPData> returns = new ArrayList<BSIPData>();
		IPAddress address = ip.getIPAddress();
		List<BSIP> knownContains = BSIP.allMatching(address, address.getNetworkPrefixLength());
		
		for (BSIP maybe : knownContains) {
			BSIPData data = byExactIP(maybe);
			if (data != null) {
				returns.add(data);
			}
		}
		if (returns.isEmpty()) {
			BanStick.getPlugin().warning("No IPData records contain IP {0}", ip);
		}
		return returns;
	}
	
	/**
	 * This does NOT check for prior existance. Must be managed elsewhere.
	 * 
	 * @param ip
	 * @param continent
	 * @param country
	 * @param region
	 * @param postal
	 * @param domain
	 * @param registeredAs
	 * @param _connection
	 * @param proxy
	 * @param source
	 * @param comment
	 * @return
	 */
	public static BSIPData create(BSIP ip, String continent, String country, String region, String postal, String domain, String registeredAs, String _connection, float proxy, String source, String comment) {
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection()) {
			BSIPData newData = new BSIPData();
			newData.dirty = false;
			newData.continent = continent;
			newData.iid = ip;
			newData.country = country;
			newData.region = region;
			newData.postal = postal;
			newData.domain = domain;
			newData.registeredAs = registeredAs;
			newData.connection = _connection;
			newData.proxy = proxy;
			newData.source = source;
			newData.comment = comment;
			
			try (PreparedStatement insertData = connection.prepareStatement("INSERT INTO bs_ip_data(iid, continent, country, region, postal, domain, registered_as, connection, proxy, source, comment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
				insertData.setLong(1, newData.iid.getId());
				if (newData.continent == null) {
					insertData.setNull(2, Types.VARCHAR);
				} else {
					insertData.setString(2, newData.continent);
				}
				if (newData.country == null) {
					insertData.setNull(3, Types.VARCHAR);
				} else {
					insertData.setString(3, newData.country);
				}
				if (newData.region == null) {
					insertData.setNull(4, Types.VARCHAR);
				} else {
					insertData.setString(4, newData.region);
				}
				if (newData.postal == null) {
					insertData.setNull(5, Types.VARCHAR);
				} else {
					insertData.setString(5, newData.postal);
				}
				if (newData.domain == null) {
					insertData.setNull(6, Types.VARCHAR);
				} else {
					insertData.setString(6, newData.domain);
				}
				if (newData.registeredAs == null) {
					insertData.setNull(7, Types.VARCHAR);
				} else {
					insertData.setString(7, newData.registeredAs);
				}
				if (newData.connection == null) {
					insertData.setNull(8, Types.VARCHAR);
				} else {
					insertData.setString(8, newData.connection);
				}
				insertData.setFloat(9, newData.proxy);
				if (newData.source == null) {
					insertData.setNull(10, Types.VARCHAR);
				} else {
					insertData.setString(10, newData.source);
				}
				if (newData.comment == null) {
					insertData.setNull(11, Types.VARCHAR);
				} else {
					insertData.setString(11, newData.comment);
				}
				insertData.execute();
				try (ResultSet rs = insertData.getGeneratedKeys()) {
					if (rs.next()) { 
						newData.idid = rs.getLong(1);
					} else {
						BanStick.getPlugin().severe("No IDID returned on IP Data insert?!");
						return null; // no bid? error.
					}
				}
			}
			
			allIPDataID.put(newData.idid, newData);
			return newData;
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to create a new ip data record: ", se);
		}
		return null;
	}
	
	/**
	 * Saves the BSBan; only for internal use. Outside code must use Flush();
	 */
	private void save() {
		if (!dirty) return;
		this.dirty = false; // don't let anyone else in!
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement save = connection.prepareStatement("UPDATE bs_ip_data SET valid = ?, proxy = ?, source = ?, comment = ? WHERE idid = ?");) {
			saveToStatement(save);
			int effects = save.executeUpdate();
			if (effects == 0) {
				BanStick.getPlugin().severe("Failed to save BSIPData or no update? " + this.idid);
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Save of BSIPData failed!: ", se);
		}
	}
	
	private void saveToStatement(PreparedStatement save) throws SQLException {
		save.setBoolean(1, this.valid);
		save.setFloat(2, this.proxy);
		if (this.source == null) {
			save.setNull(3, Types.VARCHAR);
		} else {
			save.setString(3, this.source);
		}
		if (this.comment == null) {
			save.setNull(4,  Types.VARCHAR);
		} else {
			save.setString(4,  this.comment);
		}
		save.setLong(5,  this.idid);
	}
	
	/**
	 * Cleanly saves this player if necessary, and removes it from the references lists.
	 */
	public void flush() {
		if (dirty) {
			save();
		}
		allIPDataID.remove(this.idid);
		this.iid = null;
	}

	/**
	 * Pulls from the dirty queue and commits updates to the backing DB in batches. Allows for high volume changes w/o swamping the DB with churn.
	 * 
	 * Note that inserts are direct, to ensure IDs and relationship consistency is upheld
	 */
	public static void saveDirty() {
		int batchSize = 0;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement save = connection.prepareStatement("UPDATE bs_ip_data SET valid = ?, proxy = ?, source = ?, comment = ? WHERE idid = ?");) {
			while (!dirtyIPData.isEmpty()) {
				WeakReference<BSIPData> rdata = dirtyIPData.poll();
				BSIPData data = rdata.get();
				if (data != null && data.dirty) {
					data.dirty = false;
					data.saveToStatement(save);
					save.addBatch();
					batchSize ++;
				}
				if (batchSize % 100 == 0) {
					int[] batchRun = save.executeBatch();
					if (batchRun.length != batchSize) {
						BanStick.getPlugin().severe("Some elements of the dirty batch didn't save? " + batchSize + " vs " + batchRun.length);
					} else {
						BanStick.getPlugin().debug("IP Data batch: {0} saves", batchRun.length);
					}
					batchSize = 0;
				}
			}
			if (batchSize % 100 > 0) {
				int[] batchRun = save.executeBatch();
				if (batchRun.length != batchSize) {
					BanStick.getPlugin().severe("Some elements of the dirty batch didn't save? " + batchSize + " vs " + batchRun.length);
				} else {
					BanStick.getPlugin().debug("IP Data batch: {0} saves", batchRun.length);
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Save of BSIPData dirty batch failed!: ", se);
		}
	}

	/**
	 * Triggers a segmented preload of valid BSIPData segments. Items marked invalid are not loaded.
	 * Fills the cache with entries.
	 * 
	 * @param offset Starting point
	 * @param limit How many to load
	 * @return The largest ID encountered during this load, or -1 if nothing loaded.
	 */
	public static long preload(long offset, int limit) {
		long maxId = -1;
		try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
				PreparedStatement loadData = connection.prepareStatement(
						"SELECT * FROM bs_ip_data WHERE valid = true AND idid > ? ORDER BY idid LIMIT ?");) {
			loadData.setLong(1, offset);
			loadData.setInt(2, limit);
			try (ResultSet rs = loadData.executeQuery()) {
				while (rs.next()) {
					BSIPData data = extractData(rs);
					if (!allIPDataID.containsKey(data.idid)) {
						allIPDataID.put(data.idid, data);
					}
					if (data.idid > maxId) maxId = data.idid;
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed during IPData preload, offset " + offset + " limit " + limit, se);
		}
		return maxId;

	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(iid.toString()).append(" - ");
		if (!valid) {
			sb.append("[Invalid] ");
		}
		
		if (continent != null) {
			sb.append(continent).append(" ");
		}
		if (country != null) {
			sb.append(country).append(" ");
		}
		if (region != null) {
			sb.append(region).append(" ");
		}
		if (postal != null) {
			sb.append(postal).append(" ");
		}
		if (connection != null) {
			sb.append(connection).append(" ");
		}
		if (domain != null) {
			sb.append("(").append(domain).append(") ");
		}
		if (registeredAs != null) {
			sb.append(registeredAs).append(" ");
		}
		if (source != null) {
			sb.append("from ").append(source).append(" ");
		}
		if (comment != null) {
			sb.append(":").append(comment).append(" ");
		}
		sb.append("[pli: ").append(proxy).append("]");
		
		return sb.toString();
	}

}
