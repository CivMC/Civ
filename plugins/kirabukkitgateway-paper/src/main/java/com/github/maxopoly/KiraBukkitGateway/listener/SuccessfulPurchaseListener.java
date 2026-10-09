package com.github.maxopoly.KiraBukkitGateway.listener;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.untamedears.itemexchange.events.SuccessfulPurchaseEvent;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.namelayer.group.Group;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Location;
import org.bukkit.Bukkit;

public class SuccessfulPurchaseListener implements Listener {

    @EventHandler
    public void sendSuccessfulPurchase(SuccessfulPurchaseEvent e) {
        if (e.getTrade() == null)
            return;
        Location location = e.getTrade().getInventory().getLocation();

        Reinforcement reinforcement = Citadel.getInstance()
            .getReinforcementManager()
            .getReinforcement(location);

        if (reinforcement == null)
            return;
        Group group = reinforcement.getGroup();
        if (group == null) return;

        KiraBukkitGatewayPlugin.getInstance().getRabbit().sendSuccessfulPurchase(group.getName(),
            e.getPurchaser(), location, e.getTrade().getInput().getName(),
            e.getPaymentItems(), e.getPurchasedItems());
    }
}
