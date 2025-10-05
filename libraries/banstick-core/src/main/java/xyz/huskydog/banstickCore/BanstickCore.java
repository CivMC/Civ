package xyz.huskydog.banstickCore;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class BanstickCore {
    private final @NotNull Config config;

    private final @NotNull BanstickPlugin plugin;
    private  final @NotNull Logger logger;

    public BanstickCore(@NotNull BanstickPlugin plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger();

        config = new Config(plugin);
    }

    public @NotNull Config getConfig() {
        return config;
    }
}
