package xyz.huskydog.banstickCore;

import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSLog;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Unified Banstick backend for all platforms
 */
public class BanstickCore {

    private static @Nullable BanstickCore INSTANCE;

    private final @NotNull BanstickPlugin plugin;
    private final @NotNull BanStickDatabaseHandler databaseHandler;
    private final @NotNull BSLog logHandler;
    // private final @NotNull BSRegistrars bannedRegistrars;

    /**
     * Initialize Banstick backend
     *
     * @param plugin Banstick plugin instance
     * @throws RuntimeException if core components cannot be initialized
     */
    public BanstickCore(@NotNull BanstickPlugin plugin) {
        INSTANCE = this;
        this.plugin = plugin;

        this.databaseHandler = new BanStickDatabaseHandler(plugin.getConfig());
        this.logHandler = new BSLog(plugin.getConfig());
        // this.bannedRegistrars = new BSRegistrars();
        // TODO: replace with core scheduler call inside logHandler
        // this.logHandler.runTaskTimerAsynchronously(this, this.logHandler.getDelay(), this.logHandler.getPeriod());
    }

    // /**
    //  * Kick a player by UUID and notify moderators
    //  *
    //  * @param uuid player id
    //  * @param ban  ban information
    //  * @return if the kick player was successful
    //  */
    // public boolean kickPlayer(@NotNull UUID uuid) {
    //     plugin.kickPlayer(uuid, Component.text(ban.getMessage()));
    //     plugin.broadcastMessage(Component.text().append(Component.text("Banning " + uuid + " due to "))
    //         .append(ban.getComponentMessage()).build(), "banstick.ips");
    //     return true;
    // }

    public @NotNull BSLog getLogHandler() {
        return logHandler;
    }

    public @NotNull BanStickDatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }

    public @NotNull Logger getLogger() {
        return plugin.getLogger();
    }

    public @NotNull BanstickPlugin getPlugin() {
        return plugin;
    }

    public static @Nullable BanstickCore getInstance() {
        return INSTANCE;
    }
}
