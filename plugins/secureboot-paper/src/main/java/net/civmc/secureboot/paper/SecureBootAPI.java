package net.civmc.secureboot.paper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public class SecureBootAPI {
    public static Logger logger = Logger.getLogger("SecureBoot");

    private static final ArrayList<String> RequiredPlugins = new ArrayList<>();
    private static final ArrayList<String> ReadyPlugins = new ArrayList<>();

    /**
     * Tells SecureBoot to ensure that the specified plugin is enabled AND marked ready
     *
     * @param pluginName
     * @return if plugin was marked as required to start the server
     */
    public static boolean markDependent(@Nullable String pluginName) {
        if (pluginName == null) return false;

        RequiredPlugins.add(pluginName);
        logger.info( "Marked dependent: " + pluginName);
        return true;
    }

    /**
     * Notify SecureBoot a plugin is marked as ready
     *
     * @param pluginName
     * @return
     */
    public static boolean markPluginAsReady(@Nullable String pluginName) {
        if (pluginName == null) return false;

        ReadyPlugins.add(pluginName);
        logger.info( "Marked ready: " + pluginName);
        return true;
    }

    public enum Violation {
        NotEnabled,
        NotReady
    }

    /**
     * Gets a list of every plugin that either failed to mark itself as ready, or is not enabled in Bukkit
     * @return HashMap of Plugin name and the reason it is considered a violator
     */
    public static HashMap<String, Violation> getSecureBootViolations() {
        var pm = Bukkit.getPluginManager();
        var violations = new HashMap<String, Violation>();

        for (String pluginName : RequiredPlugins) {
            if (!pm.isPluginEnabled(pluginName)) {
                violations.put(pluginName, Violation.NotEnabled);
            } else if (!ReadyPlugins.contains(pluginName)) {
                violations.put(pluginName, Violation.NotReady);
            }
        }

        return violations;
    }
}
