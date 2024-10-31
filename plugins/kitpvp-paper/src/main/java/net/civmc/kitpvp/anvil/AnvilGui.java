package net.civmc.kitpvp.anvil;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import java.util.HashMap;
import java.util.Map;

public class AnvilGui implements Listener {
    private final Map<Player, Component> names = new HashMap<>();
    private final Map<Player, Inventory> anvils = new HashMap<>();
    private final Map<Player, AnvilGuiListener> listeners = new HashMap<>();

    public void open(Player player, Component name, AnvilGuiListener listener) {
        names.put(player, name);
        InventoryView view = player.openAnvil(null, true);
        if (view == null) {
            names.remove(player);
            return;
        }
        anvils.put(player, view.getTopInventory());
        listeners.put(player, listener);

        view.getTopInventory().setItem(0, new ItemStack(Material.WRITABLE_BOOK));
    }

    @EventHandler
    public void on(InventoryOpenEvent event) {
        Component title = names.remove(event.getPlayer());
        if (title != null) {
            event.titleOverride(title);
        }
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Inventory inventory = anvils.remove(event.getPlayer());
        if (inventory != null) {
            inventory.clear();
            listeners.remove(event.getPlayer()).onClose();
        }
    }

    @EventHandler
    public void on(InventoryCloseEvent event) {
        Inventory inventory = anvils.remove(event.getPlayer());
        if (inventory != null) {
            inventory.clear();
            listeners.remove(event.getPlayer()).onClose();
        }
    }

    @EventHandler
    public void on(InventoryClickEvent event) {
        if (anvils.get(event.getWhoClicked()) != event.getInventory()) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() != event.getInventory()) {
            return;
        }

        if (event.getSlot() == 2) {
            String text = ((AnvilView) event.getView()).getRenameText();
            if (text == null) {
                return;
            }

            if (listeners.get(event.getWhoClicked()).onRename(text)) {
                listeners.remove(event.getWhoClicked());
                anvils.remove(event.getWhoClicked()).clear();
                if (event.getWhoClicked().getOpenInventory().getTopInventory() == event.getInventory()) {
                    event.getWhoClicked().closeInventory();
                }
            }
        }
    }
}
