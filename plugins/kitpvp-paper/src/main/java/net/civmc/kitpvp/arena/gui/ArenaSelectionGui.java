package net.civmc.kitpvp.arena.gui;

import java.util.List;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.arena.ArenaManager;
import net.civmc.kitpvp.arena.data.Arena;
import net.civmc.kitpvp.arena.data.ArenaDao;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class ArenaSelectionGui {

    private final JavaPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
    private final ArenaDao arenaDao;
    private final ArenaManager manager;

    public ArenaSelectionGui(ArenaDao arenaDao, ArenaManager manager) {
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
        if (arenas.size() > 54) {
            throw new IllegalArgumentException("too many arenas");
        } else if (arenas.isEmpty()) {
            player.sendMessage(Component.text("No available arenas", NamedTextColor.RED));
            return;
        }
        ClickableInventory inventory = new ClickableInventory(Math.ceilDiv(arenas.size(), 9) * 9, "Arena types");
        int slot = 0;
        for (Arena arena : arenaDao.getArenas()) {
            ItemStack item = new ItemStack(arena.icon());
            ItemMeta meta = item.getItemMeta();
            meta.itemName(Component.text(arena.name(), NamedTextColor.WHITE));
            item.setItemMeta(meta);
            inventory.setSlot(new Clickable(item) {
                @Override
                protected void clicked(@NotNull Player clicker) {
                    clicker.closeInventory();
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> manager.createArena(clicker, arena));
                }
            }, slot++);
        }
        inventory.showInventory(player, false);
    }
}
