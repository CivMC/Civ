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
import java.util.Calendar;
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
 * Defines an explicit 1 to 1 relationship between two players
 *
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 */
public final class BSShare {

    private static final BanstickCore CORE = Objects.requireNonNull(BanstickCore.getInstance());
    private static Map<Long, BSShare> allShareID = new HashMap<>();
    private static ConcurrentLinkedQueue<WeakReference<BSShare>> dirtyShares = new ConcurrentLinkedQueue<>();
    private boolean dirty;

    private long sid;
    private Long deferFirstPlayer;
    private BSPlayer firstPlayer;
    private Long deferSecondPlayer;
    private BSPlayer secondPlayer;

    private Long deferFirstSession;
    private BSSession firstSession;
    private Long deferSecondSession;
    private BSSession secondSession;

    private Timestamp createTime;
    private Timestamp pardonTime;

    private BSShare() {
    }

    public long getId() {
        return this.sid;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getPardonTime() {
        return pardonTime;
    }

    /**
     * If a pradon was granted incorrectly, this safely clears it.
     */
    public void clearPardonTime() {
        this.pardonTime = null;
        this.dirty = true;
        dirtyShares.offer(new WeakReference<BSShare>(this));
        firstPlayer.unpardonShare(this);
        secondPlayer.unpardonShare(this);
    }

    /**
     * Individual relationships can be pardoned. This sets the time of that pardon.
     *
     * @param pardonTime The Java Date to pardon on.
     */
    public void setPardonTime(@NotNull Date pardonTime) {
        setPardonTime(new Timestamp(pardonTime.getTime()));
    }

    /**
     * Individual relationships can be pardoned. This sets the time of that pardon.
     *
     * @param pardonTime The SQL Date to pardon on.
     */
    public void setPardonTime(@NotNull Timestamp pardonTime) {
        this.pardonTime = pardonTime;
        this.dirty = true;
        dirtyShares.offer(new WeakReference<BSShare>(this));
        if (isPardoned()) {
            firstPlayer.pardonShare(this);
            secondPlayer.pardonShare(this);
        } else {
            firstPlayer.unpardonShare(this);
            secondPlayer.unpardonShare(this);
        }
    }

    public boolean isPardoned() {
        return this.pardonTime != null && this.pardonTime.compareTo(new Date()) <= 0;
    }

    /**
     * @return the first player of this share.
     */
    public BSPlayer getFirstPlayer() {
        if (firstPlayer == null && deferFirstPlayer != null) {
            firstPlayer = BSPlayer.getById(deferFirstPlayer);
        }
        return firstPlayer;
    }

    /**
     * @return the second player of this share.
     */
    public BSPlayer getSecondPlayer() {
        if (secondPlayer == null && deferSecondPlayer != null) {
            secondPlayer = BSPlayer.getById(deferSecondPlayer);
        }
        return secondPlayer;
    }

    /**
     * @return the first session of this share (generally for first player).
     */
    public BSSession getFirstSession() {
        if (firstSession == null && deferFirstSession != null) {
            firstSession = BSSession.byId(deferFirstSession);
        }
        return firstSession;
    }

    /**
     * @return the second session of this share (generally for second player).
     */
    public BSSession getSecondSession() {
        if (secondSession == null && deferSecondSession != null) {
            secondSession = BSSession.byId(deferSecondSession);
        }
        return secondSession;
    }

    /**
     * Gets a BSShare by database ID.
     *
     * @param sid the Share to retrieve's ID
     * @return the BSShare if already loaded or found in DB, or null if not found.
     */
    public static BSShare byId(long sid) {
        if (allShareID.containsKey(sid)) {
            return allShareID.get(sid);
        }
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement getId = connection.prepareStatement("SELECT * FROM bs_share WHERE sid = ?");) {
            getId.setLong(1, sid);
            try (ResultSet rs = getId.executeQuery();) {
                if (rs.next()) {
                    BSShare internal = internalGetShare(rs);
                    allShareID.put(sid, internal);
                    return internal;
                } else {
                    CORE.getLogger().warn("Failed to retrieve Share by id: {} - not found", sid);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Retrieval of Share by ID failed: " + sid, se);
        }

        return null;
    }

    /**
     * Gets all BSShares for a particular player. Does not matter if player is first or second on the share.
     *
     * @param player The BSPlayer to investigate
     * @return a list of Shares for this player, could be empty if no shares found.
     */
    public static List<BSShare> byPlayer(BSPlayer player) {
        List<BSShare> shares = new ArrayList<>();
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement getId = connection.prepareStatement(
                 "SELECT * FROM bs_share WHERE first_pid = ? OR second_pid = ?");) {
            getId.setLong(1, player.getId());
            getId.setLong(2, player.getId());
            try (ResultSet rs = getId.executeQuery();) {
                while (rs.next()) {
                    if (allShareID.containsKey(rs.getLong(1))) {
                        shares.add(allShareID.get(rs.getLong(1)));
                        continue;
                    }
                    BSShare internal = internalGetShare(rs);
                    allShareID.put(rs.getLong(1), internal);
                    shares.add(internal);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Retrieval of Shares by Player failed: " + player.toString(), se);
        }
        return shares;
    }

    /**
     * Gets all BSShares for a particular session. Gets all shares for all players involved in the session as
     * either first or second session.
     *
     * @param session the BSSession to investigate
     * @return a list of BSShare related to the session. List may be empty if no session are found.
     */
    public static List<BSShare> bySession(BSSession session) {
        List<BSShare> shares = new ArrayList<>();
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement getId = connection.prepareStatement(
                 "SELECT * FROM bs_share WHERE first_sid = ? OR second_sid = ?");) {
            getId.setLong(1, session.getId());
            getId.setLong(2, session.getId());
            try (ResultSet rs = getId.executeQuery();) {
                while (rs.next()) {
                    if (allShareID.containsKey(rs.getLong(1))) {
                        shares.add(allShareID.get(rs.getLong(1)));
                        continue;
                    }
                    BSShare internal = internalGetShare(rs);
                    allShareID.put(rs.getLong(1), internal);
                    shares.add(internal);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Retrieval of Shares by Session failed: " + session.toString(), se);
        }
        return shares;
    }

    private static BSShare internalGetShare(ResultSet rs) throws SQLException {
        BSShare internal = new BSShare();
        internal.sid = rs.getLong(1);
        internal.dirty = false;
        internal.createTime = rs.getTimestamp(2);
        internal.deferFirstPlayer = rs.getLong(3);
        // nS.firstPlayer = BSPlayer.byId(rs.getLong(3));
        internal.deferSecondPlayer = rs.getLong(4);
        // nS.secondPlayer = BSPlayer.byId(rs.getLong(4));
        internal.deferFirstSession = rs.getLong(5);
        // nS.firstSession = BSSession.byId(rs.getLong(5));
        internal.deferSecondSession = rs.getLong(6);
        // nS.secondSession = BSSession.byId(rs.getLong(6));
        if (rs.getBoolean(7)) {
            try {
                internal.pardonTime = rs.getTimestamp(8);
            } catch (SQLException se) {
                internal.pardonTime = null; // data is inconsistent.
                internal.dirty = true;
                dirtyShares.offer(new WeakReference<BSShare>(internal));
            }
        } else {
            internal.pardonTime = null;
        }
        return internal;
    }

    /**
     * Forces a save of all Shares marked dirty.
     */
    public static void saveDirty() {
        int batchSize = 0;
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement save = connection.prepareStatement(
                 "UPDATE bs_share SET pardon = ?, pardon_time = ? WHERE sid = ?");) {
            while (!dirtyShares.isEmpty()) {
                WeakReference<BSShare> rshare = dirtyShares.poll();
                BSShare share = rshare.get();
                if (share != null && share.dirty) {
                    share.dirty = false;
                    share.saveToStatement(save);
                    save.addBatch();
                    batchSize++;
                }
                if (batchSize > 0 && batchSize % 100 == 0) {
                    int[] batchRun = save.executeBatch();
                    if (batchRun.length != batchSize) {
                        CORE.getLogger().error("Some elements of the dirty batch didn't save? {} vs {}", batchSize, batchRun.length);
                    } else {
                        CORE.getLogger().debug("Share batch: {} saves", batchRun.length);
                    }
                    batchSize = 0;
                }
            }
            if (batchSize > 0 && batchSize % 100 > 0) {
                int[] batchRun = save.executeBatch();
                if (batchRun.length != batchSize) {
                    CORE.getLogger().error("Some elements of the dirty batch didn't save? {} vs {}", batchSize, batchRun.length);
                } else {
                    CORE.getLogger().debug("Share batch: {} saves", batchRun.length);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Save of BSShare dirty batch failed!: ", se);
        }
    }

    /**
     * Saves the BSShare; only for internal use. Outside code must use Flush();
     */
    private void save() {
        if (!dirty) {
            return;
        }
        this.dirty = false; // don't let anyone else in!
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement save = connection.prepareStatement(
                 "UPDATE bs_share SET pardon = ?, pardon_time = ? WHERE sid = ?");) {
            saveToStatement(save);
            int effects = save.executeUpdate();
            if (effects == 0) {
                CORE.getLogger().error("Failed to save BSShare or no update? " + this.sid);
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Save of BSShare failed!: ", se);
        }
    }

    private void saveToStatement(PreparedStatement save) throws SQLException {
        if (this.pardonTime == null) {
            save.setBoolean(1, false);
            save.setNull(2, Types.TIMESTAMP);
        } else {
            save.setBoolean(1, true);
            save.setTimestamp(2, this.pardonTime);
        }
        save.setLong(3, this.sid);
    }

    /**
     * Cleanly saves this player if necessary, and removes it from the references lists.
     */
    public void flush() {
        if (dirty) {
            save();
        }
        allShareID.remove(this.sid);
        this.deferFirstPlayer = null;
        this.firstPlayer = null;
        this.deferSecondPlayer = null;
        this.secondPlayer = null;
        this.deferFirstSession = null;
        this.firstSession = null;
        this.deferSecondPlayer = null;
        this.secondSession = null;
    }

    /**
     * Preloads a segment of share data. Offset indicates lowbound exclusive to begin,
     * with limit constraining size of batch.
     *
     * @param offset (not included) low bound on ID of record to load.
     * @param limit  how many to load
     * @return last ID encountered, or -1 is none/no more
     */
    public static long preload(long offset, int limit) {
        long maxId = -1;
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement loadShares = connection.prepareStatement(
                 "SELECT * FROM bs_share WHERE sid > ? ORDER BY sid LIMIT ?");) {
            loadShares.setLong(1, offset);
            loadShares.setInt(2, limit);
            try (ResultSet rs = loadShares.executeQuery()) {
                while (rs.next()) {
                    if (rs.getLong(1) > maxId) {
                        maxId = rs.getLong(1);
                    }
                    if (allShareID.containsKey(rs.getLong(1))) {
                        continue;
                    }

                    BSShare share = internalGetShare(rs);
                    allShareID.put(share.sid, share);
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Failed during Share preload, offset " + offset + " limit " + limit, se);
        }
        return maxId;
    }

    @Override
    public String toString() {
        return PlainTextComponentSerializer.plainText().serialize(getComponentMessage(true));
    }

    /**
     * Print the full Share as a Component, and optionally show IPs
     *
     * @param showIPs true to show the IPs
     * @return a Component with all the data.
     */
    public Component getComponentMessage(boolean showIPs) {
        TextComponent.Builder builder = Component.text();
        if (this.isPardoned()) {
            builder.append(Component.text("[Pardoned] ", NamedTextColor.GREEN));
        }

        builder.append(Component.text("Share by ", NamedTextColor.DARK_PURPLE))
            .append(Component.text(getFirstPlayer().getName(), NamedTextColor.WHITE))
            .append(Component.text(" and ", NamedTextColor.DARK_PURPLE))
            .append(Component.text(getSecondPlayer().getName(), NamedTextColor.WHITE))
            .append(Component.text(" via ", NamedTextColor.DARK_PURPLE))
            .append(getFirstSession().getComponentMessage(showIPs).color(NamedTextColor.WHITE))
            .append(Component.text(" with ", NamedTextColor.DARK_PURPLE))
            .append(getSecondSession().getComponentMessage(showIPs).color(NamedTextColor.WHITE));
        return builder.build();
    }

    /**
     * Create a new Share
     *
     * @param overlap First player's session
     * @param session Second player's session
     * @return a new BSShare linking these sessions
     */
    public static BSShare create(BSSession overlap, BSSession session) {
        try (Connection connection = CORE.getDatabaseHandler().getData().getConnection();
             PreparedStatement newShare = connection.prepareStatement(
                 "INSERT INTO bs_share(create_time, first_pid, second_pid, first_sid, second_sid, pardon, pardon_time) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);) {
            BSShare share = new BSShare();
            share.createTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
            share.deferFirstPlayer = overlap.getPlayer().getId();
            share.firstPlayer = overlap.getPlayer();
            share.deferSecondPlayer = session.getPlayer().getId();
            share.secondPlayer = session.getPlayer();
            share.deferFirstSession = overlap.getId();
            share.firstSession = overlap;
            share.deferSecondSession = session.getId();
            share.secondSession = session;
            share.pardonTime = null;

            newShare.setTimestamp(1, share.createTime);
            newShare.setLong(2, share.firstPlayer.getId());
            newShare.setLong(3, share.secondPlayer.getId());
            newShare.setLong(4, share.firstSession.getId());
            newShare.setLong(5, share.secondSession.getId());
            newShare.setBoolean(6, false);
            newShare.setNull(7, Types.TIMESTAMP);

            int ins = newShare.executeUpdate();
            if (ins < 1) {
                CORE.getLogger().warn("Insert reported no share inserted? {}", share.getId());
            }

            try (ResultSet rs = newShare.getGeneratedKeys()) {
                if (rs.next()) {
                    long sid = rs.getLong(1);
                    share.sid = sid;
                    share.dirty = false;
                    allShareID.put(sid, share);
                    return share;
                } else {
                    CORE.getLogger().error("Failed to get ID from inserted share!? {} - {}", overlap.getId(), session.getId());
                    return null;
                }
            }
        } catch (SQLException se) {
            CORE.getLogger().error("Failed to insert new share for sessions!", se);
        }
        return null;
    }
}
