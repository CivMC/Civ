package xyz.huskydog.banstickCore;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Represents a platform-specific Banstick implementation
 */
public interface BanstickPlugin {
    @NotNull Logger getLogger();
    @NotNull Path getDataDirectory();
    @NotNull Config getConfig();
    @NotNull String getPluginId();

    /**
     * Schedule a task to run asynchronously after a delay, and repeat at a specified period
     * @param task the task to run
     * @param delay the delay in seconds before first execution
     * @param period the period in seconds between subsequent executions
     */
    void scheduleAsyncTask(@NotNull Runnable task, long delay, long period);

    /**
     * Schedule a task to run asynchronously after a delay
     * @param task the task to run
     * @param delay the delay in seconds before execution
     */
    void scheduleAsyncTask(@NotNull Runnable task, long delay);

    /**
     * Kick a player by UUID
     * @param uuid the UUID of the player to kick
     * @return if the kick player was successful
     */
    boolean kickPlayer(@NotNull UUID uuid, @NotNull Component reason);

    /**
     * Broadcast a message to all players with a specific permission
     * @param message the message to broadcast
     * @param permission the permission required to receive the message
     */
    void broadcastMessage(@NotNull Component message, @NotNull String permission);
}
