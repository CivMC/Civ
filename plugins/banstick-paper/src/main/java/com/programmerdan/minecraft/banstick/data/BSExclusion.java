package com.programmerdan.minecraft.banstick.data;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;

/**
 * Represents an exclusion of association between two players.
 *
 * Pardoning shares will only pardon specific already existing shares, but an exclusion can be used to functionality
 * pardon all existing and all in the future created shares between players.
 *
 * Exclusions can be applied even if a share does not exist yet and will be taken into account for transitive
 * associations. Functionally players and shares form a graph in which players are nodes and shares are edges.
 * Transitive retrievals of association groups get the biggest connected subgraph of the graph containing all
 * players/shares. Exclusions allow specifying edges, which will be ignored during this transitive alt lookup and make
 * them non-existant for the purpose of this graph. This allows subdividing player alt groups into subsets, which will
 * persist even across ip changes.
 *
 */
public class BSExclusion {

    private static Map<Long, BSExclusion> allExclussionsID = new HashMap<>();

    private long eid;

    private long deferFirstPlayer;
    private BSPlayer firstPlayer;
    private long deferSecondPlayer;
    private BSPlayer secondPlayer;

    private Timestamp createTime;

    /**
     * Creation only possible over the static methods
     */
    private BSExclusion() {
    }

    /**
     * @return Unique id of this exclusion, which is also its primary key in the database
     */
    public long getId() {
        return this.eid;
    }

    /**
     * @return When this exclusion was created
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @return First player part of this exclusion
     */
    public BSPlayer getFirstPlayer() {
        if (firstPlayer == null) {
            firstPlayer = BSPlayer.byId(deferFirstPlayer);
        }
        return firstPlayer;
    }

    /**
     * @return BanStick internal id of the first player part of this exclusion
     */
    public long getFirstPlayerID() {
        return deferFirstPlayer;
    }

    /**
     * @return BanStick internal id of the second player part of this exclusion
     */
    public long getSecondPlayerID() {
        return deferSecondPlayer;
    }

    /**
     * @return Second player part of this exclusion
     */
    public BSPlayer getSecondPlayer() {
        if (secondPlayer == null) {
            secondPlayer = BSPlayer.byId(deferSecondPlayer);
        }
        return secondPlayer;
    }

    /**
     * Deletes this exclusion completly from both the cache and the database
     */
    public void delete() {
        allExclussionsID.remove(this.eid);
        try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
                PreparedStatement ps = connection.prepareStatement("DELETE FROM bs_exclusion WHERE eid = ?");) {
            ps.setLong(1, eid);
            ps.execute();
        } catch (SQLException se) {
            BanStick.getPlugin().severe("Removal of Exclusion failed: " + eid, se);
        }
        getFirstPlayer().removeExclusion(this);
        getSecondPlayer().removeExclusion(this);
    }

    /**
     * Retrieves an exclusion based on its id. If the exclusion is known in the cache it'll be loaded from there,
     * otherwise it'll attempted to be loaded from the database
     *
     * @param eid
     *            ID of the exclusion to load
     * @return BSExclusion with the given ID or null if no such BSExclusion was found
     */
    public static BSExclusion byId(long eid) {
        if (allExclussionsID.containsKey(eid)) {
            return allExclussionsID.get(eid);
        }
        try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
                PreparedStatement getId = connection.prepareStatement("SELECT * FROM bs_exclusion WHERE eid = ?");) {
            getId.setLong(1, eid);
            try (ResultSet rs = getId.executeQuery();) {
                if (rs.next()) {
                    BSExclusion nS = internalGetExclusion(rs);
                    allExclussionsID.put(eid, nS);
                    return nS;
                } else {
                    BanStick.getPlugin().warning("Failed to retrieve exclusion by id: " + eid + " - not found");
                }
            }
        } catch (SQLException se) {
            BanStick.getPlugin().severe("Retrieval of Exclusion by ID failed: " + eid, se);
        }

        return null;
    }

    /**
     * Retrieves all exclusions for a given player from the database. If some exclusions were already cached, the cached
     * object will be priorized. The returned map will use the id of the other player in the exclusion as key, but no
     * guarantee can be made regarding whether the player the exclusions were requested for is the first or second
     * player within the exclusion itself.
     *
     * @param player
     *            Player to retrieve exclusions for
     * @return Map using PID of the other player in the exclusion as key and the exclusion itself as value
     */
    static Map<Long, BSExclusion> byPlayer(BSPlayer player) {
        Map<Long, BSExclusion> exclusions = new HashMap<>();
        try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
                PreparedStatement getId = connection
                        .prepareStatement("SELECT * FROM bs_exclusion WHERE first_pid = ? OR second_pid = ?");) {
            getId.setLong(1, player.getId());
            getId.setLong(2, player.getId());
            try (ResultSet rs = getId.executeQuery();) {
                while (rs.next()) {
                    if (allExclussionsID.containsKey(rs.getLong(1))) {
                        BSExclusion existingExcl = allExclussionsID.get(rs.getLong(1));
                        long otherID = existingExcl.deferFirstPlayer == player.getId() ? existingExcl.deferSecondPlayer
                                : existingExcl.deferFirstPlayer;
                        exclusions.put(otherID, existingExcl);
                        continue;
                    }
                    BSExclusion loadedExcl = internalGetExclusion(rs);
                    long otherID = loadedExcl.deferFirstPlayer == player.getId() ? loadedExcl.deferSecondPlayer
                            : loadedExcl.deferFirstPlayer;
                    allExclussionsID.put(rs.getLong(1), loadedExcl);
                    exclusions.put(otherID, loadedExcl);
                }
            }
        } catch (SQLException se) {
            BanStick.getPlugin().severe("Retrieval of Exclusions by Player failed: " + player.toString(), se);
        }
        return exclusions;
    }

    /**
     * Internal method to retrieve parts of a BSExclusion from a ResultSet and fill them into a new instance
     *
     * @param rs
     *            ResultSet to retrieve data from
     * @return Constructed BSExclusion
     * @throws SQLException
     */
    private static BSExclusion internalGetExclusion(ResultSet rs) throws SQLException {
        BSExclusion excl = new BSExclusion();
        excl.eid = rs.getLong(1);
        excl.createTime = rs.getTimestamp(2);
        excl.deferFirstPlayer = rs.getLong(3);
        excl.deferSecondPlayer = rs.getLong(4);
        return excl;
    }

    /**
     * Preloads exclusions from the database by their index
     *
     * @param offset
     *            Offset of the section to load
     * @param limit
     *            How many exclusions to load
     * @return Highest id that was successfully loaded
     */
    public static long preload(long offset, int limit) {
        long maxId = -1;
        try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
                PreparedStatement loadExclusions = connection
                        .prepareStatement("SELECT * FROM bs_exclusion WHERE eid > ? ORDER BY eid LIMIT ?");) {
            loadExclusions.setLong(1, offset);
            loadExclusions.setInt(2, limit);
            try (ResultSet rs = loadExclusions.executeQuery()) {
                while (rs.next()) {
                    if (rs.getLong(1) > maxId)
                        maxId = rs.getLong(1);
                    if (allExclussionsID.containsKey(rs.getLong(1))) {
                        continue;
                    }

                    BSExclusion excl = internalGetExclusion(rs);
                    allExclussionsID.put(excl.eid, excl);
                }
            }
        } catch (SQLException se) {
            BanStick.getPlugin().severe("Failed during Exclusion preload, offset " + offset + " limit " + limit, se);
        }
        return maxId;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(ChatColor.DARK_PURPLE).append("Exclusion between ").append(ChatColor.WHITE)
                .append(getFirstPlayer().getName()).append(ChatColor.DARK_PURPLE).append(" and ")
                .append(ChatColor.WHITE).append(getSecondPlayer().getName());
        return sb.toString();
    }

    public String toFullString() {
        StringBuffer sb = new StringBuffer();
        sb.append(ChatColor.DARK_PURPLE).append("Exclusion between ").append(ChatColor.WHITE)
                .append(getFirstPlayer().getName()).append(ChatColor.DARK_PURPLE).append(" and ")
                .append(ChatColor.WHITE).append(getSecondPlayer().getName()).append(ChatColor.DARK_PURPLE);
        return sb.toString();
    }

    public static BSExclusion create(BSPlayer first, BSPlayer second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Can not create exclusion based on player null");
        }
        try (Connection connection = BanStickDatabaseHandler.getinstanceData().getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO bs_exclusion(create_time, first_pid, second_pid) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);) {
            BSExclusion exclusion = new BSExclusion();
            exclusion.createTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
            exclusion.deferFirstPlayer = first.getId();
            exclusion.firstPlayer = first;
            exclusion.deferSecondPlayer = second.getId();
            exclusion.secondPlayer = second;

            ps.setTimestamp(1, exclusion.createTime);
            ps.setLong(2, exclusion.firstPlayer.getId());
            ps.setLong(3, exclusion.secondPlayer.getId());
            int ins = ps.executeUpdate();
            if (ins < 1) {
                BanStick.getPlugin().warning("Insert reported no exclusion inserted? " + exclusion.getId());
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long eid = rs.getLong(1);
                    exclusion.eid = eid;
                    allExclussionsID.put(eid, exclusion);
                    return exclusion;
                } else {
                    BanStick.getPlugin().severe(
                            "Failed to get ID from inserted exclusion!? " + first.getId() + " - " + second.getId());
                    return null;
                }
            }
        } catch (SQLException se) {
            BanStick.getPlugin().severe("Failed to insert new exclusion for sessions!", se);
        }
        return null;
    }

}
