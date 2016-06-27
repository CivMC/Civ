package net.minelink.ctplus.listener;

import com.google.common.base.Preconditions;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.event.CombatLogEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static com.google.common.base.Preconditions.*;

public class InstakillListener implements Listener {
    private final CombatTagPlus plugin;

    public InstakillListener(CombatTagPlus plugin) {
        this.plugin = checkNotNull(plugin, "Null plugin");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatLog(CombatLogEvent event) {
        if (!plugin.getSettings().instantlyKill()) return; // We aren't configured to instakill

        // Kill the player
        if (event.getReason() == CombatLogEvent.Reason.TAGGED) {
            event.getPlayer().setHealth(0);
        }
    }
}
