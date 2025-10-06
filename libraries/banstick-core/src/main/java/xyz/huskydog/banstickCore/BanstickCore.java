package xyz.huskydog.banstickCore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BanstickCore {
    private static @Nullable BanstickCore INSTANCE;

    private final @NotNull BanstickPlugin plugin;
    private  final @NotNull Logger logger;
    private final @NotNull Config config;


    public BanstickCore(@NotNull BanstickPlugin plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger();
        this.config = plugin.getConfig();

        INSTANCE = this;
    }

    public @NotNull Logger getLogger() {
        return logger;
    }

    public static @Nullable BanstickCore getInstance() {
        return INSTANCE;
    }
}
