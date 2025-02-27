package net.civmc.secureboot.paper;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SecureBootPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    // need event to run as late as possible to catch any lingering issues
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerLoad(ServerLoadEvent event) {
        var violators = SecureBootAPI.getSecureBootViolations();
        var logger = getLogger();

        if (violators.isEmpty()) {
            logger.info("No violations found");
            return;
        }

        // TODO: add alerting when this is triggered

        for (var plugin : violators.keySet() ) {
            logger.warning(plugin + ": " + violators.get(plugin));
        }
        Bukkit.shutdown();
    }
}
