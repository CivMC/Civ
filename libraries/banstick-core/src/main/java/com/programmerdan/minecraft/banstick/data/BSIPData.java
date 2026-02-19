package com.programmerdan.minecraft.banstick.data;

import inet.ipaddr.IPAddress;
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
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import xyz.huskydog.banstickCore.BanstickCore;

/**
 * Static storage and management of Proxy data as BSIPData.
 *
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 */
public final class BSIPData {

    private static final BanstickCore CORE = Objects.requireNonNull(BanstickCore.getInstance());
    private static Map<Long, BSIPData> allIPDataID = new HashMap<>();
    private static ConcurrentLinkedQueue<WeakReference<BSIPData>> dirtyIPData = new ConcurrentLinkedQueue<>();
    private boolean dirty;

    private long idid;
    private Long deferIid;
    private BSIP iid;
    private Timestamp createTime;
    private boolean valid; // mutable
    private String continent;
    private String country;
    private String region;
    private String city;
    private String postal;
    private Double lat;
    private Double lon;
    private String domain;
    private String provider;
    private String registeredAs;
    private String connection;
    private float proxy; // mutable
    private String source; // mutable
    private String comment; // mutable

    private BSIPData() {
    }

    public long getId() {
        return this.idid;
    }

    /**
     * Marks this proxy data as invalid, triggers that the backing data should be
     * updated.
     */
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

    /**
     * @return the BSIP associated with this Proxy (usually a CIDR address).
     */
    public BSIP getIP() {
        if (this.iid == null && this.deferIid != null) {
            this.iid = BSIP.byId(this.deferIid);
        }
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

    public String getCity() {
        return this.city;
    }

    public String getPostal() {
        return this.postal;
    }

    public Double getLat() {
        return this.lat;
    }

    public Double getLon() {
        return this.lon;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getProvider() {
        return this.provider;
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

    /**
     * Updates the proxy "level", based on threshold could trigger bans.
     * Triggers a dirty flag update.
     *
     * @param proxy the new proxy value.
     */
    public void setProxy(float proxy) {
        this.proxy = proxy;
        dirtyIPData.offer(new WeakReference<BSIPData>(this));
    }

    public String getSource() {
        return this.source;
    }

    /**
     * Sets the string-value "source" of this proxy. Can be updated using
     * this method, triggers a dirty flag update.
     *
     * @param source The new source.
     */
    public void setSource(String source) {
        this.source = source;
        dirtyIPData.offer(new WeakReference<BSIPData>(this));

    }

    public String getComment() {
        return this.comment;
    }

    /**
     * Sets the string-value "comment" for this proxy. Can be updated using
     * this method, triggers a dirty flag update.
     *
     * @param comment The new comment
     */
    public void setComment(String comment) {
        this.comment = comment;
        dirtyIPData.offer(new WeakReference<BSIPData>(this));
    }

    /**
     * Retrieve a specific Proxy by its identifier.
     *
     * @param idid the ID of the Proxy to retrieve
     * @return the BSIPData object with that ID if found, otherwise null.
     */
    public static BSIPData byId(long idid) {
        if (allIPDataID.containsKey(idid)) {
            return allIPDataID.get(idid);
        }
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement getId = connection.prepareStatement(
                 "SELECT * FROM bs_ip_data WHERE idid = ?");) {
            getId.setLong(1, idid);
            try (ResultSet rs = getId.executeQuery();) {
                if (rs.next()) {
                    BSIPData data = extractData(rs);
                    allIPDataID.put(idid, data);
                    return data;
                } else {
                    CORE.getLogger().warn("Failed to retrieve IP Data by id: {} - not found", idid);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Retrieval of IP Data by ID failed: " + idid, se);
        }
        return null;
    }

    /**
     * Internal tool to create a BSIPData object from a ResultSet
     *
     * @param rs the ResultSet to unpack
     * @return a BSIPData from that ResultSet
     * @throws SQLException if something goes wrong
     */
    private static BSIPData extractData(ResultSet rs) throws SQLException {
        BSIPData data = new BSIPData();
        data.idid = rs.getLong(1);
        data.deferIid = rs.getLong(2);
        // data.iid = BSIP.byId(rs.getLong(2));
        data.createTime = rs.getTimestamp(3);
        data.valid = rs.getBoolean(4);
        data.continent = rs.getString(5);
        data.country = rs.getString(6);
        data.region = rs.getString(7);
        data.city = rs.getString(8);
        data.postal = rs.getString(9);
        data.lat = rs.getDouble(10);
        if (rs.wasNull()) {
            data.lat = null;
        }
        data.lon = rs.getDouble(11);
        if (rs.wasNull()) {
            data.lon = null;
        }
        data.domain = rs.getString(12);
        data.provider = rs.getString(13);
        data.registeredAs = rs.getString(14);
        data.connection = rs.getString(15);
        data.proxy = rs.getFloat(16);
        data.source = rs.getString(17);
        data.comment = rs.getString(18);
        data.dirty = false;
        return data;
    }

    /**
     * As described in method header; finds the other non-same IPData records that
     * geolocate to the same country/region/city pairing.
     *
     * @param source The IPData to use to seed the search
     * @return A list of IPDatas in the same region, or nothing if none found.
     */
    public static List<BSIPData> bySameCity(BSIPData source) {
        List<BSIPData> found = new ArrayList<>();
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement getSame = connection.prepareStatement(
                 "SELECT * FROM bs_ip_data WHERE country = ? and region = ? and city = ? and idid != ? and valid = true ORDER BY create_time");) {
            getSame.setString(1, source.getCountry());
            getSame.setString(2, source.getRegion());
            getSame.setString(3, source.getCity());
            getSame.setLong(4, source.getId());
            try (ResultSet rs = getSame.executeQuery();) {
                while (rs.next()) {
                    if (allIPDataID.containsKey(rs.getLong(1))) {
                        found.add(allIPDataID.get(rs.getLong(1)));
                    }
                    BSIPData data = extractData(rs);
                    allIPDataID.put(data.idid, data);
                    found.add(data);
                }
                if (found.isEmpty()) {
                    CORE.getLogger().debug("Found no other IP Data in same city as {}", source);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Retrieval of same-city IP Data by IP Data failed: " + source, se);
        }
        return found;
    }

    /**
     * Returns only the latest valid BSIPData records by exact match with the IP
     *
     * @param ip The BSIP record to use for IPData retrieval
     * @return the matching BSIPData or null if no match
     */
    public static BSIPData byExactIP(@NotNull BSIP ip) {
        if (ip == null) {
            CORE.getLogger().warn("Weird failure, byExactIP with null IP");
            return null;
        }
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement getId = connection.prepareStatement(
                 "SELECT * FROM bs_ip_data WHERE iid = ? and valid = true ORDER BY create_time DESC LIMIT 1");) {
            getId.setLong(1, ip.getId());
            try (ResultSet rs = getId.executeQuery();) {
                if (rs.next()) {
                    if (allIPDataID.containsKey(rs.getLong(1))) {
                        return allIPDataID.get(rs.getLong(1));
                    }
                    BSIPData data = extractData(rs);
                    allIPDataID.put(data.idid, data);
                    return data;
                } else {
                    CORE.getLogger().warn("Failed to retrieve IP Data by exact IP: {} - not found", ip);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Retrieval of IP Data by exact IP failed: " + ip, se);
        }
        return null;
    }

    /**
     * @return the average proxy value for BSIPData entries having the same registeredAs.
     */
    public double getAverageForRegistrar() {
        if (this.registeredAs == null) {
            return 0;
        }
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement getSame = connection.prepareStatement(
                 "SELECT avg(proxy) FROM bs_ip_data WHERE registered_as = ?");) {
            getSame.setString(1, this.registeredAs);
            try (ResultSet rs = getSame.executeQuery();) {
                rs.next();
                return rs.getDouble(1); // returns 0 for no matches
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Failed to load average proxy score for registrar "
                + this.registeredAs, se);
            return 0;
        }
    }

    /**
     * Finds the first subnet that fully contains the given BSIP's IP/CIDR, if any.
     *
     * @param ip The BSIP record to base the lookup on
     * @return The BSIPData record that contains the IP/CIDR, or null if none found.
     */
    public static BSIPData byContainsIP(BSIP ip) {
        try {
            if (ip == null) {
                CORE.getLogger().warn("Weird failure, byContainsIP with null IP");
                return null;
            }
            IPAddress address = ip.getIPAddress();
            if (address == null) {
                CORE.getLogger().warn("Weird failure, no ip _in_ {}", ip);
                return null;
            }
            Integer cidr = address.getNetworkPrefixLength();
            if (cidr == null) {
                cidr = address.isIPv4() ? 32 : 128;
            }
            List<BSIP> knownContains = BSIP.allMatching(address, cidr);

            for (BSIP maybe : knownContains) {
                BSIPData data = byExactIP(maybe);
                if (data != null) {
                    return data;
                }
            }

            CORE.getLogger().warn("No IPData records contain IP {}", ip);
        } catch (Exception e) {
            CORE.getLogger().warn("Failure during IPData retrieval", e);
        }
        return null;
    }

    /**
     * Returns all BSIPData that relate to or contain the given IP/CIDR.
     *
     * @param ip The BSIP to use for matching.
     * @return a list; empty if nothing found.
     */
    public static List<BSIPData> allByIP(BSIP ip) {
        List<BSIPData> returns = new ArrayList<>();
        try {
            if (ip == null) {
                CORE.getLogger().warn("Weird failure, allByIP with null IP");
                return returns;
            }
            IPAddress address = ip.getIPAddress();
            if (address == null) {
                CORE.getLogger().warn("Weird failure, no ip _in_ {}", ip);
                return returns;
            }
            Integer cidr = address.getNetworkPrefixLength();
            if (cidr == null) {
                cidr = address.isIPv4() ? 32 : 128;
            }
            List<BSIP> knownContains = BSIP.allMatching(address, cidr);

            for (BSIP maybe : knownContains) {
                BSIPData data = byExactIP(maybe);
                if (data != null) {
                    returns.add(data);
                }
            }

            if (returns.isEmpty()) {
                CORE.getLogger().warn("No IPData records contain IP {}", ip);
            }
        } catch (Exception e) {
            CORE.getLogger().warn("Failure during IPData retrieval", e);
        }
        return returns;
    }

    /**
     * This does NOT check for prior existence. Must be managed elsewhere.
     *
     * @param ip               The BSIP record to bind to. Must not be null.
     * @param continent        The continent where this IP is registered to
     * @param country          The country where this IP is registered to
     * @param region           The region where this IP is registered to
     * @param city             The city where this IP is registered to
     * @param postal           The postal / zip code where this IP is registered to
     * @param lat              The latitude
     * @param lon              The longitude
     * @param domain           The domain that maps to this IP / range
     * @param provider         The provider for this IP / range
     * @param registeredAs     Any registration information for this IP / range
     * @param connectionSource The connection source for this IP / range
     * @param proxy            The likelihood this is a proxy -- higher number for more likelihood.
     *                         Assumes 0 - no, 3 - likely, 4 - definite
     * @param source           The source of this record (Cloud9? Tor? IP-API?)
     * @param comment          Any comments recorded against the BSIPData.
     * @return The new BSIPData if successful
     */
    public static BSIPData create(BSIP ip, String continent, String country, String region,
                                  String city, String postal, Double lat, Double lon, String domain, String provider,
                                  String registeredAs, String connectionSource, float proxy, String source, String comment) {
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection()) {
            BSIPData newData = new BSIPData();
            newData.valid = true;
            newData.dirty = false;
            newData.continent = continent;
            newData.deferIid = ip.getId();
            newData.iid = ip;
            newData.country = country;
            newData.region = region;
            newData.city = city;
            newData.postal = postal;
            newData.lat = lat;
            newData.lon = lon;
            newData.domain = domain;
            newData.provider = provider;
            newData.registeredAs = registeredAs;
            newData.connection = connectionSource;
            newData.proxy = proxy;
            newData.source = source;
            newData.comment = comment;

            try (PreparedStatement insertData = connection.prepareStatement(
                "INSERT INTO bs_ip_data(iid, continent, country, region, city, postal, lat, lon, domain, provider, registered_as, connection, proxy, source, comment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS)) {
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
                if (newData.region == null) {
                    insertData.setNull(5, Types.VARCHAR);
                } else {
                    insertData.setString(5, newData.city);
                }
                if (newData.postal == null) {
                    insertData.setNull(6, Types.VARCHAR);
                } else {
                    insertData.setString(6, newData.postal);
                }
                if (newData.lat == null) {
                    insertData.setNull(7, Types.DOUBLE);
                } else {
                    insertData.setDouble(7, newData.lat);
                }
                if (newData.lon == null) {
                    insertData.setNull(8, Types.DOUBLE);
                } else {
                    insertData.setDouble(8, newData.lon);
                }
                if (newData.domain == null) {
                    insertData.setNull(9, Types.VARCHAR);
                } else {
                    insertData.setString(9, newData.domain);
                }
                if (newData.domain == null) {
                    insertData.setNull(10, Types.VARCHAR);
                } else {
                    insertData.setString(10, newData.provider);
                }
                if (newData.registeredAs == null) {
                    insertData.setNull(11, Types.VARCHAR);
                } else {
                    insertData.setString(11, newData.registeredAs);
                }
                if (newData.connection == null) {
                    insertData.setNull(12, Types.VARCHAR);
                } else {
                    insertData.setString(12, newData.connection);
                }
                insertData.setFloat(13, newData.proxy);
                if (newData.source == null) {
                    insertData.setNull(14, Types.VARCHAR);
                } else {
                    insertData.setString(14, newData.source);
                }
                if (newData.comment == null) {
                    insertData.setNull(15, Types.VARCHAR);
                } else {
                    insertData.setString(15, newData.comment);
                }
                insertData.execute();
                try (ResultSet rs = insertData.getGeneratedKeys()) {
                    if (rs.next()) {
                        newData.idid = rs.getLong(1);
                    } else {
                        CORE.getLogger().error("No IDID returned on IP Data insert?!");
                        return null; // no bid? error.
                    }
                }
            }

            allIPDataID.put(newData.idid, newData);
            return newData;
        } catch (SQLException se) {
            CORE.getLogger().error("Failed to create a new ip data record: ", se);
        }
        return null;
    }

    /**
     * Saves the BSBan; only for internal use. Outside code must use Flush();
     */
    private void save() {
        if (!dirty) {
            return;
        }
        this.dirty = false; // don't let anyone else in!
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement save = connection.prepareStatement(
                 "UPDATE bs_ip_data SET valid = ?, proxy = ?, source = ?, comment = ? WHERE idid = ?");) {
            saveToStatement(save);
            int effects = save.executeUpdate();
            if (effects == 0) {
                CORE.getLogger().error("Failed to save BSIPData or no update? {}", this.idid);
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Save of BSIPData failed!: ", se);
        }
    }

    /**
     * Internal helper to map this BSIPData to a PreparedStatement for upsert
     *
     * @param save the PreparedStatement to fill
     * @throws SQLException if something goes wrong.
     */
    private void saveToStatement(PreparedStatement save) throws SQLException {
        save.setBoolean(1, this.valid);
        save.setFloat(2, this.proxy);
        if (this.source == null) {
            save.setNull(3, Types.VARCHAR);
        } else {
            save.setString(3, this.source);
        }
        if (this.comment == null) {
            save.setNull(4, Types.VARCHAR);
        } else {
            save.setString(4, this.comment);
        }
        save.setLong(5, this.idid);
    }

    /**
     * Cleanly saves this IPData if necessary, and removes it from the references lists.
     */
    public void flush() {
        if (dirty) {
            save();
        }
        allIPDataID.remove(this.idid);
        this.deferIid = null;
        this.iid = null;
    }

    /**
     * Pulls from the dirty queue and commits updates to the backing DB in batches.
     * Allows for high volume changes w/o swamping the DB with churn.
     *
     * <p>Note that inserts are direct, to ensure IDs and relationship consistency is upheld
     */
    public static void saveDirty() {
        int batchSize = 0;
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement save = connection.prepareStatement(
                 "UPDATE bs_ip_data SET valid = ?, proxy = ?, source = ?, comment = ? WHERE idid = ?");) {
            while (!dirtyIPData.isEmpty()) {
                WeakReference<BSIPData> rdata = dirtyIPData.poll();
                BSIPData data = rdata.get();
                if (data != null && data.dirty) {
                    data.dirty = false;
                    data.saveToStatement(save);
                    save.addBatch();
                    batchSize++;
                }
                if (batchSize > 0 && batchSize % 100 == 0) {
                    int[] batchRun = save.executeBatch();
                    if (batchRun.length != batchSize) {
                        CORE.getLogger().error("Some elements of the dirty batch didn't save? {} vs {}", batchSize, batchRun.length);
                    } else {
                        CORE.getLogger().debug("IP Data batch: {} saves", batchRun.length);
                    }
                    batchSize = 0;
                }
            }
            if (batchSize > 0 && batchSize % 100 > 0) {
                int[] batchRun = save.executeBatch();
                if (batchRun.length != batchSize) {
                    CORE.getLogger().error("Some elements of the dirty batch didn't save? {} vs {}", batchSize, batchRun.length);
                } else {
                    CORE.getLogger().debug("IP Data batch: {} saves", batchRun.length);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Save of BSIPData dirty batch failed!: ", se);
        }
    }

    /**
     * Triggers a segmented preload of valid BSIPData segments. Items marked invalid are not loaded.
     * Fills the cache with entries.
     *
     * @param offset Starting point
     * @param limit  How many to load
     * @return The largest ID encountered during this load, or -1 if nothing loaded.
     */
    public static long preload(long offset, int limit) {
        long maxId = -1;
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
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
                    if (data.idid > maxId) {
                        maxId = data.idid;
                    }
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Failed during IPData preload, offset {} limit {}", offset, limit, se);
        }
        return maxId;

    }

    @Override
    public String toString() {
        return PlainTextComponentSerializer.plainText().serialize(getComponentMessage());
    }

    public Component getComponentMessage() {
        TextComponent.Builder sb = Component.text();

        sb.append(Component.text(getIP().toString(), NamedTextColor.WHITE))
            .append(Component.text(" - "));

        if (!valid) {
            sb.append(Component.text("[Invalid] ", NamedTextColor.RED))
                .append(Component.text("", NamedTextColor.WHITE));
        }
        if (continent != null || country != null || region != null || city != null
            || postal != null || lat != null || lon != null) {
            sb.append(Component.text("Location: ", NamedTextColor.AQUA))
                .append(Component.text("", NamedTextColor.WHITE));
        }
        if (continent != null) {
            sb.append(Component.text(continent + ", ", NamedTextColor.WHITE));
        }
        if (country != null) {
            sb.append(Component.text(country + ", ", NamedTextColor.WHITE));
        }
        if (region != null) {
            sb.append(Component.text(region + ", ", NamedTextColor.WHITE));
        }
        if (city != null) {
            sb.append(Component.text(city + ", ", NamedTextColor.WHITE));
        }
        if (postal != null) {
            sb.append(Component.text(postal + ", ", NamedTextColor.WHITE));
        }
        if (lat != null || lon != null) {
            sb.append(Component.text("[", NamedTextColor.GRAY))
                .append(Component.text(String.valueOf(lat), NamedTextColor.WHITE))
                .append(Component.text(",", NamedTextColor.GRAY))
                .append(Component.text(String.valueOf(lon), NamedTextColor.WHITE))
                .append(Component.text("] ", NamedTextColor.GRAY))
                .append(Component.text("", NamedTextColor.WHITE));
        }
        if (connection != null) {
            sb.append(Component.text("Connection: ", NamedTextColor.GRAY))
                .append(Component.text(connection + " ", NamedTextColor.WHITE));
        }
        if (domain != null) {
            sb.append(Component.text("(" + domain + ") ", NamedTextColor.WHITE));
        }
        if (provider != null) {
            sb.append(Component.text("Provider: ", NamedTextColor.GRAY))
                .append(Component.text(provider + " ", NamedTextColor.WHITE));
        }
        if (registeredAs != null) {
            sb.append(Component.text("Reg. As: ", NamedTextColor.GRAY))
                .append(Component.text(registeredAs + " ", NamedTextColor.WHITE));
        }
        if (source != null) {
            sb.append(Component.text("from ", NamedTextColor.DARK_PURPLE))
                .append(Component.text(source + " ", NamedTextColor.WHITE));
        }
        if (comment != null) {
            sb.append(Component.text("Comments:", NamedTextColor.GRAY))
                .append(Component.text(comment + " ", NamedTextColor.WHITE));
        }
        sb.append(Component.text("[pli: ", NamedTextColor.DARK_AQUA));

        NamedTextColor proxyColor;
        if (proxy < 1.0) {
            proxyColor = NamedTextColor.GREEN;
        } else if (proxy < 2.0) {
            proxyColor = NamedTextColor.GOLD;
        } else if (proxy < 3.0) {
            proxyColor = NamedTextColor.YELLOW;
        } else {
            proxyColor = NamedTextColor.RED;
        }
        sb.append(Component.text(String.valueOf(proxy), proxyColor))
            .append(Component.text("]", NamedTextColor.DARK_AQUA));
        return sb.build();
    }

    // /**
    //  * Generates a larger string of full details for this proxy
    //  *
    //  * @param showIPs indicate if IPs should be shown
    //  * @return the String
    //  */
    // public String toFullString(boolean showIPs) {
    //     if (showIPs) {
    //         return toString();
    //     }
    //     StringBuilder sb = new StringBuilder();
    //     sb.append(ChatColor.WHITE).append(getIP().toFullString(showIPs)).append(" - ");
    //     if (!valid) {
    //         sb.append(ChatColor.RED).append("[Invalid] ").append(ChatColor.WHITE);
    //     }
    //     sb.append("Proxy #").append(this.idid).append(" ");
    //     if (source != null) {
    //         sb.append(ChatColor.DARK_PURPLE).append("from ").append(ChatColor.WHITE).append(source).append(" ");
    //     }
    //     if (comment != null) {
    //         sb.append(ChatColor.GRAY).append("Comments:").append(ChatColor.WHITE).append(comment).append(" ");
    //     }
    //     sb.append(ChatColor.DARK_AQUA).append("[pli: ");
    //     if (proxy < 1.0) {
    //         sb.append(ChatColor.GREEN);
    //     } else if (proxy < 2.0) {
    //         sb.append(ChatColor.GOLD);
    //     } else if (proxy < 3.0) {
    //         sb.append(ChatColor.YELLOW);
    //     } else {
    //         sb.append(ChatColor.RED);
    //     }
    //     sb.append(proxy).append(ChatColor.DARK_AQUA).append("]").append(ChatColor.RESET);
    //
    //     return sb.toString();
    // }

}
