package net.civmc.zorweth.oxygen;

import com.dre.brewery.api.events.IngedientAddEvent;
import com.dre.brewery.api.events.brew.BrewModifyEvent;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class BreweryOxygenListener implements Listener {

    private static final Component NO_OXYGEN_MESSAGE = Component.text("Brewery does not work without oxygen.",
        NamedTextColor.RED);

    private final OxygenManager oxygenManager;

    public BreweryOxygenListener(final OxygenManager oxygenManager) {
        this.oxygenManager = Objects.requireNonNull(oxygenManager);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void on(final IngedientAddEvent event) {
        if (hasOxygen(event.getBlock())) {
            return;
        }
        event.setCancelled(true);
        sendNoOxygenMessage(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void on(final BrewModifyEvent event) {
        final Player player = event.getPlayer();
        if (player == null || this.oxygenManager.hasOxygen(player.getLocation())) {
            return;
        }
        event.setCancelled(true);
        sendNoOxygenMessage(player);
    }

    private boolean hasOxygen(final Block block) {
        return block != null && this.oxygenManager.hasOxygen(block.getLocation());
    }

    private void sendNoOxygenMessage(final Player player) {
        if (player != null) {
            player.sendMessage(NO_OXYGEN_MESSAGE);
        }
    }
}
