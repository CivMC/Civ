package vg.civcraft.mc.civmodcore.events;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Useful when your plugin has isolated sections with listeners, like a SimpleAdminHack or a dependency glue.
 */
public final class PooledListeners {
    private final List<Listener> listener = new ArrayList<>();

    public void registerListener(
        final @NotNull Plugin ownerPlugin,
        final @NotNull Listener listener
    ) {
        Bukkit.getPluginManager().registerEvents(listener, ownerPlugin);
        this.listener.add(listener);
    }

    public void clearListeners() {
        this.listener.forEach(HandlerList::unregisterAll);
        this.listener.clear();
    }
}
