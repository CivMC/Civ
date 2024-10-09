package net.civmc.kitpvp;

import com.dre.brewery.api.BreweryApi;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DrunkDeathListener implements Listener {
    @EventHandler
    public void on(PlayerDeathEvent event) {
        BreweryApi.setPlayerDrunk(event.getPlayer(), 0, 10);
    }
}
