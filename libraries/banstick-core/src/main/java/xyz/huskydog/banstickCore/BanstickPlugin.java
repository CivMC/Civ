package xyz.huskydog.banstickCore;

import org.slf4j.Logger;
import java.nio.file.Path;

public interface BanstickPlugin {
    Logger getLogger();
    Path getDataDirectory();
}
