package sh.okx.railswitch.switches;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import sh.okx.railswitch.RailSwitchPlugin;

/**
 * Handles configuration interactions with detector rails using the configured tool.
 */
public final class SwitchConfiguratorListener implements Listener {

    private final RailSwitchPlugin plugin;
    private final SwitchConfigurationSessionManager sessionManager;

    public SwitchConfiguratorListener(RailSwitchPlugin plugin, SwitchConfigurationSessionManager sessionManager) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConfigureRail(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        ItemStack item = event.getItem();
        if (!isConfigurationTool(item)) {
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.DETECTOR_RAIL) {
            return;
        }
        Player player = event.getPlayer();
        if (sessionManager == null) {
            return;
        }
        if (sessionManager.isEditing(player)) {
            player.sendMessage(ChatColor.YELLOW + "You are already editing a rail. Finish or cancel via chat before starting another.");
            return;
        }
        sessionManager.beginSession(player, clicked);
    }

    private boolean isConfigurationTool(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        Material configured = plugin.getSwitchConfiguration().getToolMaterial();
        return stack.getType() == configured;
    }
}
