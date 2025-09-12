package vg.civcraft.mc.citadel.listener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.model.WorldBorderBuffers;

public class WorldBorderListener implements Listener {

    private Map<UUID, WorldBorderBuffers> buffers;
    private Set<UUID> warned = new HashSet<>();

    public WorldBorderListener() {
        this.buffers = Citadel.getInstance().getConfigManager().getWorldBorderBuffers();
    }

    @EventHandler
    public void onReinCreation(ReinforcementCreationEvent event) {
        World world = event.getReinforcement().getLocation().getWorld();
        if (!buffers.containsKey(world.getUID())) {
            return;
        }
        WorldBorderBuffers buffer = buffers.get(world.getUID());
        Location reinforcementLocation = event.getReinforcement().getLocation();
        if (!buffer.checkIfOutside(reinforcementLocation.getX(), reinforcementLocation.getZ())) {
            return;
        }

        if (buffer.decay()) {
            if (warned.add(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(Component.text("Reinforcing this close to the border will cause your reinforcements to instantly start decaying").color(NamedTextColor.RED));
            }
        } else {
            event.getPlayer().sendMessage(Component.text("You cannot reinforce this close to the border!").color(NamedTextColor.RED));
            event.setCancelled(true);
        }
    }
}
