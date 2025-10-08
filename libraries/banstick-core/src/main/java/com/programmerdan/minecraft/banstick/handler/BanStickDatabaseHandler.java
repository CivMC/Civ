package com.programmerdan.minecraft.banstick.handler;

import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import com.programmerdan.minecraft.banstick.data.BSSession;
import com.programmerdan.minecraft.banstick.data.BSShare;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import xyz.huskydog.banstickCore.BanstickCore;
import xyz.huskydog.banstickCore.Config;
import xyz.huskydog.banstickCore.cmc.database.DatabaseCredentials;
import xyz.huskydog.banstickCore.cmc.database.ManagedDatasource;
import xyz.huskydog.banstickCore.cmc.utils.BukkitPlayer;

/**
 * Ties into the managed datasource processes of the CivMod core plugin.
 *
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 */
public class BanStickDatabaseHandler {

    private BanstickCore core;
    private ManagedDatasource data;

    /**
     * Creates the core DAO handler from a config.
     *
     * @param config the config to use
     * @throws RuntimeException if database config is missing or invalid.
     */
    public BanStickDatabaseHandler(Config config) {
        this.core = Objects.requireNonNull(BanstickCore.getInstance());
        CommentedConfigurationNode internal = config.getRawConfig().node("database");
        if (!configureData(internal)) {
            throw new RuntimeException("Failed to configure Database for BanStick!");
        }
    }

    public ManagedDatasource getData() {
        return this.data;
    }

    private boolean configureData(CommentedConfigurationNode config) {
        if (config.empty()) {
            core.getLogger().error("Database configuration missing, cannot continue.");
            return false;
        }

        String host = config.node("host").getString("localhost");
        int port = config.node("port").getInt(3306);
        String dbname = config.node("database").getString("banstick");
        String username = config.node("user").getString();
        String password = config.node("password").getString();
        int poolsize = config.node("poolsize").getInt(5);
        long connectionTimeout = config.node("connection_timeout").getLong(10000L);
        long idleTimeout = config.node("idle_timeout").getLong(600000L);
        long maxLifetime = config.node("max_lifetime").getLong(7200000L);
        try {
            data = ManagedDatasource.construct(core.getPlugin(), new DatabaseCredentials(username, password, host, port, "mysql", dbname,
                poolsize, connectionTimeout, idleTimeout, maxLifetime));
            data.getConnection().close();
        } catch (Exception e) {
            core.getLogger().error("Failed to initialize Database connection", e);
            return false;
        }

        initializeTables();

        long beginTime = System.currentTimeMillis();

        try {
            core.getLogger().info("Update prepared, starting database update.");
            if (!data.updateDatabase()) {
                core.getLogger().info("Update failed, disabling plugin.");
                return false;
            }
        } catch (Exception e) {
            core.getLogger().error("Update failed, disabling plugin. Cause:", e);
            return false;
        }

        core.getLogger().info("Database update took {} seconds", (System.currentTimeMillis() - beginTime) / 1000);

        activatePreload(config.node("preload"));
        // TODO: add way to disable dirty save for paper plugin as only the velocity version is modifying data
        activateDirtySave(config.node("dirtysave"));
        return true;
    }

    private void activateDirtySave(CommentedConfigurationNode config) {
        long period = 5 * 60 * 50L;
        long delay = 5 * 60 * 50L;
        if (config != null) {
            period = config.node("period").getLong(period);
            delay = config.node("delay").getLong(delay);
        }
        core.getLogger().debug("DirtySave Period {} Delay {}", period, delay);

        core.getPlugin().scheduleAsyncTask(() -> {
            core.getLogger().debug("Player dirty save");
            BSPlayer.saveDirty();
        }, delay, period);

        core.getPlugin().scheduleAsyncTask(() -> {
            core.getLogger().debug("Ban dirty save");
            BSBan.saveDirty();
        }, delay + (period / 5), period);

        core.getPlugin().scheduleAsyncTask(() -> {
            core.getLogger().debug("Session dirty save");
            BSSession.saveDirty();
        }, delay + ((period * 2) / 5), period);

        core.getPlugin().scheduleAsyncTask(() -> {
            core.getLogger().debug("Share dirty save");
            BSShare.saveDirty();
        }, delay + ((period * 3) / 5), period);

        core.getPlugin().scheduleAsyncTask(() -> {
            core.getLogger().debug("Proxy dirty save");
            BSIPData.saveDirty();
        }, delay + ((period * 4) / 5), period);

        core.getLogger().info("Dirty save tasks started.");
    }

    private void activatePreload(@NotNull CommentedConfigurationNode config) {
        if (!config.empty() && !config.node("enabled").getBoolean(false)) {
            core.getLogger().debug("Preloading is disabled");
            return;
        }

        long period = 5 * 60 * 50L;
        long delay = 5 * 60 * 50L;
        period = config.node("period").getLong(period);
        delay = config.node("delay").getLong(delay);
        final int batchsize = config.node("batch").getInt(100);

        core.getLogger().debug("Preload Period {} Delay {} batch {}", period, delay, batchsize);

        // TODO: rewrite using core scheduler
        // core.getPlugin().scheduleAsyncTask(new Runnable() {
        //     private long lastId = 0;
        //
        //     @Override
        //     public void run() {
        //         BanStick.getPlugin().debug("IP preload {0}, lim {1}", lastId, batchsize);
        //         lastId = BSIP.preload(lastId, batchsize);
        //         if (lastId < 0) {
        //             // No direct cancel, so just return early
        //             return;
        //         }
        //     }
        // }, delay, period);


        // new BukkitRunnable() {
        //     private long lastId;
        //
        //     @Override
        //     public void run() {
        //         BanStick.getPlugin().debug("IP preload {0}, lim {1}", lastId, batchsize);
        //         lastId = BSIP.preload(lastId, batchsize);
        //         if (lastId < 0) {
        //             this.cancel();
        //         }
        //     }
        // }.runTaskTimerAsynchronously(BanStick.getPlugin(), delay, period);

        // new BukkitRunnable() {
        //     private long lastId;
        //
        //     @Override
        //     public void run() {
        //         BanStick.getPlugin().debug("Proxy preload {0}, lim {1}", lastId, batchsize);
        //         lastId = BSIPData.preload(lastId, batchsize);
        //         if (lastId < 0) {
        //             this.cancel();
        //         }
        //     }
        // }.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + (period / 6), period);
        //
        // new BukkitRunnable() {
        //     private long lastId;
        //
        //     @Override
        //     public void run() {
        //         BanStick.getPlugin().debug("Ban preload {0}, lim {1}", lastId, batchsize);
        //         lastId = BSBan.preload(lastId, batchsize, false);
        //         if (lastId < 0) {
        //             this.cancel();
        //         }
        //     }
        // }.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + ((period * 2) / 6), period);
        //
        // new BukkitRunnable() {
        //     private long lastId;
        //
        //     @Override
        //     public void run() {
        //         BanStick.getPlugin().debug("Player preload {0}, lim {1}", lastId, batchsize);
        //         lastId = BSPlayer.preload(lastId, batchsize);
        //         if (lastId < 0) {
        //             this.cancel();
        //         }
        //     }
        // }.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + ((period * 3) / 6), period);
        //
        // new BukkitRunnable() {
        //     private long lastId;
        //
        //     @Override
        //     public void run() {
        //         BanStick.getPlugin().debug("Session preload {0}, lim {1}", lastId, batchsize);
        //         lastId = BSSession.preload(lastId, batchsize);
        //         if (lastId < 0) {
        //             this.cancel();
        //         }
        //     }
        // }.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + ((period * 4) / 6), period);
        //
        // new BukkitRunnable() {
        //     private long lastId;
        //
        //     @Override
        //     public void run() {
        //         BanStick.getPlugin().debug("Share preload {0}, lim {1}", lastId, batchsize);
        //         lastId = BSShare.preload(lastId, batchsize);
        //         if (lastId < 0) {
        //             this.cancel();
        //         }
        //     }
        // }.runTaskTimerAsynchronously(BanStick.getPlugin(), delay + ((period * 5) / 6), period);
    }

    /**
     * Basic method to set up data model v1.
     */
    private void initializeTables() {
        data.registerMigration(0, false,
            "CREATE TABLE IF NOT EXISTS bs_player (" +
                " pid BIGINT AUTO_INCREMENT PRIMARY KEY," +
                " name VARCHAR(16)," +
                " uuid CHAR(36) NOT NULL UNIQUE," +
                " first_add TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " bid BIGINT, " +
                " ip_pardon_time TIMESTAMP NULL, " +
                " proxy_pardon_time TIMESTAMP NULL," +
                " shared_pardon_time TIMESTAMP NULL," +
                " INDEX bs_player_name (name)," +
                " INDEX bs_player_ip_pardons (ip_pardon_time)," +
                " INDEX bs_player_proxy_pardons (proxy_pardon_time)," +
                " INDEX bs_player_shared_pardons (shared_pardon_time)," +
                " INDEX bs_player_join (first_add)" +
                ");",

            "CREATE TABLE IF NOT EXISTS bs_session (" +
                " sid BIGINT AUTO_INCREMENT PRIMARY KEY," +
                " pid BIGINT," +
                " join_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " leave_time TIMESTAMP NULL," +
                " iid BIGINT NOT NULL," +
                " INDEX bs_session_pids (pid, join_time, leave_time)" +
                ");",

            "CREATE TABLE IF NOT EXISTS bs_ban_log (" +
                " lid BIGINT AUTO_INCREMENT PRIMARY KEY," +
                " pid BIGINT NOT NULL," +
                " bid BIGINT NOT NULL," +
                " action_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " action VARCHAR(10) NOT NULL," +
                " INDEX bs_ban_log_time (pid, action_time DESC)" +
                ");", // TODO: Whenever a ban is given or removed from a player, record.

            "CREATE TABLE IF NOT EXISTS bs_ban (" +
                " bid BIGINT AUTO_INCREMENT PRIMARY KEY," +
                " ban_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " ip_ban BIGINT," +
                " proxy_ban BIGINT," +
                " share_ban BIGINT," +
                " admin_ban BOOLEAN DEFAULT FALSE," +
                " message TEXT," +
                " ban_end TIMESTAMP NULL," +
                " INDEX bs_ban_time (ban_time)," +
                " INDEX bs_ban_ip (ip_ban)," +
                " INDEX bs_ban_proxy (proxy_ban)," +
                " INDEX bs_ban_share (share_ban)," +
                " INDEX bs_ban_end (ban_end)" +
                ");",

            "CREATE TABLE IF NOT EXISTS bs_share (" +
                " sid BIGINT AUTO_INCREMENT PRIMARY KEY," +
                " create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " first_pid BIGINT NOT NULL REFERENCES bs_player(pid)," +
                " second_pid BIGINT NOT NULL REFERENCES bs_player(pid)," +
                " first_sid BIGINT NOT NULL REFERENCES bs_session(sid)," +
                " second_sid BIGINT NOT NULL REFERENCES bs_session(sid)," +
                " pardon BOOLEAN DEFAULT FALSE," +
                " pardon_time TIMESTAMP NULL," +
                " INDEX bs_share (first_pid, second_pid)," +
                " INDEX bs_pardon (pardon_time)" +
                ");",
            "CREATE TABLE IF NOT EXISTS bs_ip (" +
                " iid BIGINT AUTO_INCREMENT PRIMARY KEY," +
                " create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " ip4 CHAR(15)," +
                " ip4cidr SMALLINT," +
                " ip6 CHAR(39)," +
                " ip6cidr SMALLINT," +
                " INDEX bs_session_ip4 (ip4, ip4cidr)," +
                " INDEX bs_session_ip6 (ip6, ip6cidr)" +
                ");",
            "CREATE TABLE IF NOT EXISTS bs_ip_data (" +
                " idid BIGINT AUTO_INCREMENT PRIMARY KEY," +
                " iid BIGINT NOT NULL REFERENCES bs_ip(iid)," +
                " create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " valid BOOLEAN DEFAULT TRUE," +
                " continent TEXT," +
                " country TEXT," +
                " region TEXT," +
                " city TEXT," +
                " postal TEXT," +
                " lat DOUBLE DEFAULT NULL," +
                " lon DOUBLE DEFAULT NULL," +
                " domain TEXT," +
                " provider TEXT," +
                " registered_as TEXT," +
                " connection TEXT," +
                " proxy FLOAT," +
                " source TEXT," +
                " comment TEXT," +
                " INDEX bs_ip_data_iid (iid)," +
                " INDEX bs_ip_data_valid (valid, create_time DESC)," +
                " INDEX bs_ip_data_proxy (proxy)" +
                ");",

            "ALTER TABLE bs_player ADD CONSTRAINT fk_bs_player_ban " +
                " FOREIGN KEY (bid) REFERENCES bs_ban (bid);",
            "ALTER TABLE bs_session ADD CONSTRAINT fk_bs_session_player " +
                " FOREIGN KEY (pid) REFERENCES bs_player (pid);",
            "ALTER TABLE bs_session ADD CONSTRAINT fk_bs_session_iid " +
                " FOREIGN KEY (iid) REFERENCES bs_ip (iid);",

            "ALTER TABLE bs_ban_log ADD CONSTRAINT fk_bs_ban_log_player " +
                " FOREIGN KEY (pid) REFERENCES bs_player (pid);",
            "ALTER TABLE bs_ban_log ADD CONSTRAINT fk_bs_ban_log_ban " +
                " FOREIGN KEY (bid) REFERENCES bs_ban (bid);",

            "ALTER TABLE bs_ban ADD CONSTRAINT fk_bs_ban_ip " +
                " FOREIGN KEY (ip_ban) REFERENCES bs_ip (iid);",
            "ALTER TABLE bs_ban ADD CONSTRAINT fk_bs_ban_ip_data " +
                " FOREIGN KEY (proxy_ban) REFERENCES bs_ip_data (idid);",
            "ALTER TABLE bs_ban ADD CONSTRAINT fk_bs_ban_share " +
                " FOREIGN KEY (share_ban) REFERENCES bs_share (sid);"

        );
        data.registerMigration(1, false, "CREATE TABLE IF NOT EXISTS bs_exclusion ("
            + "eid BIGINT AUTO_INCREMENT PRIMARY KEY,"
            + "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "first_pid BIGINT NOT NULL REFERENCES bs_player(pid),"
            + "second_pid BIGINT NOT NULL REFERENCES bs_player(pid),"
            + " INDEX bs_exclusion_pid (first_pid, second_pid)"
            + ");");
        data.registerMigration(2, false, "CREATE TABLE IF NOT EXISTS bs_banned_registrars ("
            + "rid BIGINT AUTO_INCREMENT PRIMARY KEY,"
            + "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "registered_as text"
            + ");");

    }

    /**
     * Handles shutdown and proper postprocess of dirty data.
     */
    public void doShutdown() {

        core.getLogger().info("Player dirty save");
        BSPlayer.saveDirty();

        core.getLogger().info("Ban dirty save");
        BSBan.saveDirty();

        core.getLogger().info("Session dirty save");
        BSSession.saveDirty();

        core.getLogger().info("Share dirty save");
        BSShare.saveDirty();

        core.getLogger().info("Proxy dirty save");
        BSIPData.saveDirty();

        core.getLogger().info("Ban Log save");
        core.getLogHandler().disable();
    }

    // ============ QUERIES =============

    /**
     * For a given Bukkit player, get or create a BSPlayer for them.
     *
     * @param player the player
     * @return the BSPlayer created or retrieved. Null on failure only.
     */
    public BSPlayer getOrCreatePlayer(final BukkitPlayer player) {
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

    /**
     * For an Inet Address, get or create an IP for it.
     *
     * @param netAddress the InetAddress
     * @return the BSIP created or retrieved. Null on failure only.
     */
    public BSIP getOrCreateIP(final InetAddress netAddress) {
        BSIP bsIP = getIP(netAddress);
        if (bsIP == null) {
            core.getLogger().debug("Creating IP address: {}", netAddress);
            bsIP = BSIP.create(netAddress);
        } else {
            core.getLogger().info("Registering future retrieval of IPData for {}", bsIP);
            // TODO: handle this in velocity
            // BanStick.getPlugin().getIPDataHandler().offer(bsIP);
        }
        return bsIP;
    }

    public BSIP getIP(final InetAddress netAddress) {
        return BSIP.byInetAddress(netAddress);
    }

    public List<BSIP> getAllByIP(final InetAddress netAddress) {
        return BSIP.allMatching(netAddress);
    }
}
