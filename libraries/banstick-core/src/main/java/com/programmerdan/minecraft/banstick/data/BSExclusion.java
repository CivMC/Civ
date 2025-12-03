package com.programmerdan.minecraft.banstick.data;

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
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import xyz.huskydog.banstickCore.BanstickCore;

/**
 * Represents an exclusion of association between two players.
 *
 * <p>Pardoning shares will only pardon specific already existing shares, but an
 * exclusion can be used to functionality pardon all existing and all in the
 * future created shares between players.
 *
 * <p>Exclusions can be applied even if a share does not exist yet and will be
 * taken into account for transitive associations. Functionally players and
 * shares form a graph in which players are nodes and shares are edges.
 * Transitive retrievals of association groups get the biggest connected
 * subgraph of the graph containing all players/shares. Exclusions allow
 * specifying edges, which will be ignored during this transitive alt lookup and
 * make them non-existant for the purpose of this graph. This allows subdividing
 * player alt groups into subsets, which will persist even across ip changes.
 *
 * @author Maxopoly
 */
public final class BSExclusion {

    private static final BanstickCore CORE = Objects.requireNonNull(BanstickCore.getInstance());
    private static final Map<Long, BSExclusion> allExclusionsID = new HashMap<>();

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
     * @return Unique id of this exclusion, which is also its primary key in the
     * database
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
            firstPlayer = BSPlayer.getById(deferFirstPlayer);
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
            secondPlayer = BSPlayer.getById(deferSecondPlayer);
        }
        return secondPlayer;
    }

    /**
     * Deletes this exclusion completly from both the cache and the database
     */
    public void delete() {
        allExclusionsID.remove(this.eid);
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM bs_exclusion WHERE eid = ?");) {
            ps.setLong(1, eid);
            ps.execute();
        } catch (SQLException se) {
            CORE.getLogger().error("Removal of Exclusion failed: " + eid, se);
        }
        getFirstPlayer().removeExclusion(this);
        getSecondPlayer().removeExclusion(this);
    }

    /**
     * Retrieves an exclusion based on its id. If the exclusion is known in the
     * cache it'll be loaded from there, otherwise it'll attempted to be loaded from
     * the database
     *
     * @param eid ID of the exclusion to load
     * @return BSExclusion with the given ID or null if no such BSExclusion was
     * found
     */
    public static BSExclusion byId(long eid) {
        if (allExclusionsID.containsKey(eid)) {
            return allExclusionsID.get(eid);
        }
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement getId = connection.prepareStatement("SELECT * FROM bs_exclusion WHERE eid = ?");) {
            getId.setLong(1, eid);
            try (ResultSet rs = getId.executeQuery();) {
                if (rs.next()) {
                    BSExclusion excl = internalGetExclusion(rs);
                    allExclusionsID.put(eid, excl);
                    return excl;
                } else {
                    CORE.getLogger().warn("Failed to retrieve exclusion by id: {} - not found", eid);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Retrieval of Exclusion by ID failed: " + eid, se);
        }

        return null;
    }

    /**
     * Retrieves all exclusions for a given player from the database. If some
     * exclusions were already cached, the cached object will be priorized. The
     * returned map will use the id of the other player in the exclusion as key, but
     * no guarantee can be made regarding whether the player the exclusions were
     * requested for is the first or second player within the exclusion itself.
     *
     * @param player Player to retrieve exclusions for
     * @return Map using PID of the other player in the exclusion as key and the
     * exclusion itself as value
     */
    static Map<Long, BSExclusion> byPlayer(BSPlayer player) {
        Map<Long, BSExclusion> exclusions = new HashMap<>();
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement getId = connection
                 .prepareStatement("SELECT * FROM bs_exclusion WHERE first_pid = ? OR second_pid = ?");) {
            getId.setLong(1, player.getId());
            getId.setLong(2, player.getId());
            try (ResultSet rs = getId.executeQuery();) {
                while (rs.next()) {
                    if (allExclusionsID.containsKey(rs.getLong(1))) {
                        BSExclusion existingExcl = allExclusionsID.get(rs.getLong(1));
                        long otherID = existingExcl.deferFirstPlayer == player.getId() ? existingExcl.deferSecondPlayer
                            : existingExcl.deferFirstPlayer;
                        exclusions.put(otherID, existingExcl);
                        continue;
                    }
                    BSExclusion loadedExcl = internalGetExclusion(rs);
                    long otherID = loadedExcl.deferFirstPlayer == player.getId() ? loadedExcl.deferSecondPlayer
                        : loadedExcl.deferFirstPlayer;
                    allExclusionsID.put(rs.getLong(1), loadedExcl);
                    exclusions.put(otherID, loadedExcl);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Retrieval of Exclusions by Player failed: " + player.toString(), se);
        }
        return exclusions;
    }

    /**
     * Internal method to retrieve parts of a BSExclusion from a ResultSet and fill
     * them into a new instance
     *
     * @param rs ResultSet to retrieve data from
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
     * @param offset Offset of the section to load
     * @param limit  How many exclusions to load
     * @return Highest id that was successfully loaded
     */
    public static long preload(long offset, int limit) {
        long maxId = -1;
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement loadExclusions = connection
                 .prepareStatement("SELECT * FROM bs_exclusion WHERE eid > ? ORDER BY eid LIMIT ?");) {
            loadExclusions.setLong(1, offset);
            loadExclusions.setInt(2, limit);
            try (ResultSet rs = loadExclusions.executeQuery()) {
                while (rs.next()) {
                    if (rs.getLong(1) > maxId) {
                        maxId = rs.getLong(1);
                    }
                    if (allExclusionsID.containsKey(rs.getLong(1))) {
                        continue;
                    }

                    BSExclusion excl = internalGetExclusion(rs);
                    allExclusionsID.put(excl.eid, excl);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Failed during Exclusion preload, offset " + offset + " limit " + limit, se);
        }
        return maxId;
    }

    @Override
    public String toString() {
        return PlainTextComponentSerializer.plainText().serialize(getComponentMessage());
    }

    public Component getComponentMessage() {
        return MiniMessage.miniMessage().deserialize("<dark_purple>Exclusion between <white>" + getFirstPlayer().getName() + " <dark_purple>AND <white>" + getSecondPlayer().getName());
    }

    /**
     * Create a new exclusion between two players.
     *
     * @param first  The first player as a BSPlayer
     * @param second The second player as a BSPlayer
     * @return a new BSExclusion or null if failure.
     * @throws IllegalArgumentException if either of the player params is null.
     */
    public static BSExclusion create(BSPlayer first, BSPlayer second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Can not create exclusion based on player null");
        }
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
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
                CORE.getLogger().warn("Insert reported no exclusion inserted? {}", exclusion.getId());
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long eid = rs.getLong(1);
                    exclusion.eid = eid;
                    allExclusionsID.put(eid, exclusion);
                    return exclusion;
                } else {
                    CORE.getLogger().error("Failed to get ID from inserted exclusion!? {} - {}", first.getId(), second.getId());
                    return null;
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Failed to insert new exclusion for sessions!", se);
        }
        return null;
    }

}
