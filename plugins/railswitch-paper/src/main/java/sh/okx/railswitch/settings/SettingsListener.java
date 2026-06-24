package sh.okx.railswitch.settings;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import static sh.okx.railswitch.settings.SettingsManager.updateDestScoreboardHud;

public class SettingsListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent pje) {
        updateDestScoreboardHud(pje.getPlayer());
    }
}
