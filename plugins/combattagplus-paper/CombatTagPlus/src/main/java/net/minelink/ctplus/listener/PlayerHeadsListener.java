package net.minelink.ctplus.listener;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.compat.api.NpcIdentity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.shininet.bukkit.playerheads.events.PlayerDropHeadEvent;

public final class PlayerHeadsListener implements Listener {

    private final CombatTagPlus plugin;

    public PlayerHeadsListener(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void setOwner(PlayerDropHeadEvent event) {
        Player player = event.getEntity();

        // Do nothing if player is not a NPC
        if (!plugin.getNpcPlayerHelper().isNpc(player)) return;

        // Get NPC identity
        NpcIdentity identity = plugin.getNpcPlayerHelper().getIdentity(player);
        if (identity == null) return;

        ItemStack item = event.getDrop();
        item.setType(Material.PLAYER_HEAD);

        // Set skull owner to actual player's name
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(identity.getName());

        // Save meta
        item.setItemMeta(meta);
    }

}
