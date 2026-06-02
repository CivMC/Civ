package net.civmc.zorweth;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class GravityListener implements Listener {

    private final double gravity;

    public GravityListener(double gravity) {
        this.gravity = gravity;
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getAttribute(Attribute.GRAVITY).setBaseValue(gravity);
    }
}
