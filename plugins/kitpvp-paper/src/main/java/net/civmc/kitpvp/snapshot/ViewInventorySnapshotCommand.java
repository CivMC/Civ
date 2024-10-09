package net.civmc.kitpvp.snapshot;

import net.civmc.kitpvp.KitPvpPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import java.text.DecimalFormat;

public class ViewInventorySnapshotCommand implements CommandExecutor {

    private final InventorySnapshotManager manager;

    public ViewInventorySnapshotCommand(InventorySnapshotManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        OfflinePlayer viewPlayer = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (viewPlayer == null) {
            player.sendMessage(Component.text("Unknown player", NamedTextColor.RED));
            return false;
        }
        InventorySnapshot snapshot = manager.getSnapshot(viewPlayer.getUniqueId());
        if (snapshot == null) {
            player.sendMessage(Component.text("Could not find an inventory for that player", NamedTextColor.RED));
            return false;
        }

        ClickableInventory ci = new ClickableInventory(54, viewPlayer.getName());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(viewPlayer);
        if (snapshot.victim()) {
            headMeta.itemName(Component.text("Died to " + snapshot.otherPlayerProfile().getName(), NamedTextColor.RED));
        } else {
            headMeta.itemName(Component.text("Won against " + snapshot.otherPlayerProfile().getName(), NamedTextColor.GREEN));
        }
        head.setItemMeta(headMeta);

        if (!snapshot.victim()) {
            ItemStack health = new ItemStack(Material.REDSTONE_TORCH);
            ItemMeta healthMeta = health.getItemMeta();
            healthMeta.itemName(Component.text("Won at " + new DecimalFormat("#.#").format(snapshot.health() / 2) + " hearts", NamedTextColor.RED));
            health.setItemMeta(healthMeta);
            ci.setItem(health, 8);
        }

        ci.setSlot(new Clickable(head) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                    clicker.performCommand(label + " " + snapshot.otherPlayerProfile().getName());
                });
            }
        }, 0);

        for (int i = 9; i < 36; i++) {
            ci.setItem(getItem(snapshot, i, "None", Material.GRAY_STAINED_GLASS_PANE), 9 + i);
        }
        for (int i = 0; i < 9; i++) {
            ci.setItem(getItem(snapshot, i, "None", Material.YELLOW_STAINED_GLASS_PANE), 45 + i);
        }

        ci.setItem(getItem(snapshot, 39, "Helmet", Material.ORANGE_STAINED_GLASS_PANE), 10);
        ci.setItem(getItem(snapshot, 38, "Chestplate", Material.ORANGE_STAINED_GLASS_PANE), 11);
        ci.setItem(getItem(snapshot, 37, "Leggings", Material.ORANGE_STAINED_GLASS_PANE), 12);
        ci.setItem(getItem(snapshot, 36, "Boots", Material.ORANGE_STAINED_GLASS_PANE), 13);
        ci.setItem(getItem(snapshot, 40, "Offhand", Material.ORANGE_STAINED_GLASS_PANE), 15);

        ci.showInventory(player, false);
        return true;
    }

    private ItemStack getItem(InventorySnapshot snapshot, int slot, String defaultName, Material defaultMaterial) {
        ItemStack item = snapshot.items()[slot];
        if (item == null || item.isEmpty()) {
            item = new ItemStack(defaultMaterial);
            ItemMeta meta = item.getItemMeta();
            meta.itemName(Component.text(defaultName, NamedTextColor.GRAY));
            item.setItemMeta(meta);
        }
        return item;
    }
}
