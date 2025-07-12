package com.untamedears.itemexchange.glues.citadel;

import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.events.BlockInventoryRequestEvent;
import com.untamedears.itemexchange.glues.namelayer.PermissionsGlue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.utilities.DependencyGlue;
import vg.civcraft.mc.namelayer.group.Group;

public final class CitadelGlue extends DependencyGlue {

    public CitadelGlue(final @NotNull ItemExchangePlugin plugin) {
        super(plugin, "Citadel");
    }

    private final Listener listener = new Listener() {
        @EventHandler(ignoreCancelled = true)
        public void denyCreationIfNotGotPerms(final BlockInventoryRequestEvent event) {
            if (event.getPurpose() != BlockInventoryRequestEvent.Purpose.ACCESS) {
                return;
            }
            final Player requester = event.getRequester();
            if (requester == null) {
                return;
            }
            final Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(event.getBlock());
            if (reinforcement == null) {
                return;
            }
            final Group group = reinforcement.getGroup();
            if (PermissionsGlue.CHESTS.testPermission(group, requester)) {
                return;
            }
            event.setCancelled(true);
        }
    };

    @Override
    protected void onDependencyEnabled() {
        Bukkit.getPluginManager().registerEvents(this.listener, this.plugin);
    }

    @Override
    protected void onDependencyDisabled() {
        HandlerList.unregisterAll(this.listener);
    }

}
