package net.civmc.kitpvp.snapshot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final InventorySnapshotManager snapshotManager;

    public DeathListener(InventorySnapshotManager snapshotManager) {
        this.snapshotManager = snapshotManager;
    }

    @EventHandler
    public void on(PlayerDeathEvent event) {
        die(event.getPlayer());
        event.getDrops().clear();
    }

    public void die(Player player) {
        Player killer = player.getKiller();
        if (killer == null) {
            return;
        }
        snapshotManager.putSnapshot(killer.getUniqueId(), new InventorySnapshot(killer.getInventory().getContents(), false, player.getPlayerProfile(), killer.getHealth()));
        snapshotManager.putSnapshot(player.getUniqueId(), new InventorySnapshot(player.getInventory().getContents(), true, killer.getPlayerProfile(), 0));

        player.getInventory();
        Bukkit.broadcast(
            Component.empty().append(createComponent(player).color(NamedTextColor.GOLD))
                .append(Component.text(" was killed by ", NamedTextColor.YELLOW)
                    .append(createComponent(killer).color(NamedTextColor.GOLD))));
    }

    private Component createComponent(Player player) {
        return Component.text(player.getName())
            .hoverEvent(Component.text("Click to view inventory"))
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/viewinventorysnapshot " + player.getName()));
    }
}
