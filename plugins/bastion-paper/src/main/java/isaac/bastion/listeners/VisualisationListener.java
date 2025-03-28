package isaac.bastion.listeners;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.utils.BastionVisualiserUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.Iterator;
import java.util.Set;

public class VisualisationListener implements Listener {

    private BastionVisualiserUtils visualiserUtils;
    public VisualisationListener(BastionVisualiserUtils utils) {
        this.visualiserUtils = utils;
        Bastion.getSettingManager().getShowBastionFieldsSetting().registerListener(
            (player, setting, oldValue, newValue) -> {
                Player realPlayer = Bukkit.getPlayer(player);
                if (newValue) {
                    Bastion.getVisualiserUtils().getBastionsNearbyPlayer(realPlayer).forEach(bastion -> {
                        visualiserUtils.showFieldForBastionToPlayer(bastion, realPlayer);
                    });
                    visualiserUtils.startFieldTask(realPlayer);
                    return;
                }
                visualiserUtils.stopFieldTask(realPlayer);
                Bastion.getVisualiserUtils().getBastionsNearbyPlayer(realPlayer).forEach(bastion -> {
                    visualiserUtils.hideFieldForBastionToPlayer(bastion, realPlayer);
                });
        });
    }

    @EventHandler
    public void onPlayerChunkLoad(PlayerChunkLoadEvent event) {
        Set<BastionBlock> nearby = visualiserUtils.getBastionsNearbyPlayer(event.getPlayer());
        for (int x = 0; x <= 15; x++) {
            for (int z = 0; z <= 15; z++) {
                Block current = event.getChunk().getBlock(x, 0, z);
                nearby.addAll(Bastion.getBastionStorage().forLocation(current.getLocation()));
            }
        }
        if (Bastion.getSettingManager().showBastionFields(event.getPlayer().getUniqueId())) {
            nearby.forEach(bastion -> {
                visualiserUtils.showFieldForBastionToPlayer(bastion, event.getPlayer());
            });
        }
        visualiserUtils.getBastionsNearPlayerMap().put(event.getPlayer(), nearby);
    }

    @EventHandler
    public void onPlayerChunkUnload(PlayerChunkUnloadEvent event) {
        Set<BastionBlock> nearby = visualiserUtils.getBastionsNearbyPlayer(event.getPlayer());
        Iterator<BastionBlock> iterator = nearby.iterator();
        while (iterator.hasNext()) {
            BastionBlock bastion = iterator.next();
            if (bastion.getLocation().getChunk().equals(event.getChunk())) {
                iterator.remove();
                visualiserUtils.hideFieldForBastionToPlayer(bastion, event.getPlayer());
            }
        }
    }
}
