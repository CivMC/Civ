package xyz.huskydog.banstickCore;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import java.nio.file.Path;

public interface BanstickPlugin {
    @NotNull Logger getLogger();
    @NotNull Path getDataDirectory();
    @NotNull Config getConfig();
    @NotNull String getPluginId();
}
