package net.civmc.kitpvp.arena.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.TreeMap;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.arena.ArenaManager;
import net.civmc.kitpvp.arena.data.Arena;
import net.civmc.kitpvp.arena.data.ArenaDao;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class ArenaCategorySelectionGui {

    private final JavaPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
    private final ArenaDao arenaDao;
    private final ArenaManager manager;

    public ArenaCategorySelectionGui(ArenaDao arenaDao, ArenaManager manager) {
        this.arenaDao = arenaDao;
        this.manager = manager;
    }

    public void open(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Arena> arenas = arenaDao.getArenas();
            Bukkit.getScheduler().runTask(plugin, () -> open(player, arenas));
        });
    }

    private void open(Player player, List<Arena> arenas) {
        if (arenas.isEmpty()) {
            player.sendMessage(Component.text("No available arenas", NamedTextColor.RED));
            return;
        }

        List<Arena> uncategorised = new ArrayList<>();
        SequencedMap<String, List<Arena>> arenasByCategory = new TreeMap<>();
        for (Arena arena : arenas) {
            if (arena.category() == null) {
                uncategorised.add(arena);
            } else {
                arenasByCategory.computeIfAbsent(arena.category(), k -> new ArrayList<>()).add(arena);
            }
        }

        int cats = arenasByCategory.size() + (uncategorised.isEmpty() ? 0 : 1);
        ClickableInventory inventory = new ClickableInventory(Math.ceilDiv(cats, 9) * 9, "Arena categories");

        int slot = 0;
        for (Map.Entry<String, List<Arena>> entry : arenasByCategory.entrySet()) {
            inventory.setSlot(toClickable(entry.getKey(), NamedTextColor.WHITE, entry.getValue()), slot++);
        }
        if (!uncategorised.isEmpty()) {
            inventory.setSlot(toClickable("Uncategorised", NamedTextColor.GRAY, uncategorised), slot);
        }
        inventory.showInventory(player, false);
    }

    private Clickable toClickable(String category, NamedTextColor colour, List<Arena> arenas) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text(category, colour));
        item.setItemMeta(meta);
        return new Clickable(item) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                ArenaSelectionGui gui = new ArenaSelectionGui(arenaDao, ArenaCategorySelectionGui.this, manager);
                Bukkit.getScheduler().runTask(plugin, () -> gui.open(clicker, arenas));
            }
        };
    }
}
