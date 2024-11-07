package net.civmc.kitpvp.arena.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.civmc.kitpvp.arena.ArenaManager;
import net.civmc.kitpvp.arena.LoadedArena;
import net.civmc.kitpvp.arena.data.Arena;
import net.civmc.kitpvp.arena.data.ArenaDao;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;

public class ArenaGui {

    private final ArenaDao dao;
    private final ArenaManager manager;

    public ArenaGui(ArenaDao dao, ArenaManager manager) {
        this.dao = dao;
        this.manager = manager;
    }

    public void open(Player player) {
        List<IClickable> arenas = new ArrayList<>();

        List<LoadedArena> loadedArenas = manager.getArenas();
        loadedArenas.sort(Comparator.comparingInt(s -> {
            World world = Bukkit.getWorld(manager.getArenaName(s.arena().name(), s.owner()));
            if (world == null) {
                return -1;
            } else {
                return world.getPlayerCount();
            }
        }));
        for (LoadedArena loadedArena : loadedArenas) {
            Arena arena = loadedArena.arena();
            if (!player.hasPermission("kitpvp.admin")
                && loadedArena.invitedPlayers() != null
                && !loadedArena.invitedPlayers().contains(player.getPlayerProfile())
                && !loadedArena.owner().equals(player.getPlayerProfile())) {
                continue;
            }

            ItemStack item = new ItemStack(arena.icon());
            ItemMeta meta = item.getItemMeta();
            boolean isOwner = loadedArena.owner().getId().equals(player.getUniqueId());
            boolean hasAccess = isOwner || player.hasPermission("kitpvp.admin");
            if (isOwner) {
                meta.setEnchantmentGlintOverride(true);
            }
            meta.itemName(Component.text(arena.displayName(), NamedTextColor.YELLOW));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Created by " + loadedArena.owner().getName(), NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            if (loadedArena.invitedPlayers() != null) {
                lore.add(Component.text("Private arena", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            }
            lore.add(Component.text("Click to teleport", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Shift left click to join as spectator", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            if (hasAccess) {
                lore.add(Component.text("Shift right click to delete", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            }
            World world = Bukkit.getWorld(manager.getArenaName(arena.name(), loadedArena.owner()));
            if (world != null && world.getPlayerCount() > 0) {
                lore.add(Component.text("Currently playing:", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                for (Player worldPlayer : world.getPlayers()) {
                    lore.add(Component.text("- " + worldPlayer.getName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                }
            }
            meta.lore(lore);
            item.setItemMeta(meta);

            arenas.add(new Clickable(item) {
                @Override
                protected void clicked(@NotNull Player clicker) {
                    if (world != null) {
                        if (player.getGameMode() == GameMode.SPECTATOR && !player.hasPermission("kitpvp.admin")) {
                            player.setGameMode(GameMode.SURVIVAL);
                        }
                        player.teleport(arena.spawn().toLocation(world));
                    }
                }

                @Override
                protected void onShiftLeftClick(@NotNull Player clicker) {
                    if (world != null) {
                        player.setGameMode(GameMode.SPECTATOR);
                        player.teleport(arena.spawn().toLocation(world));
                    }
                }

                @Override
                protected void onShiftRightClick(@NotNull Player clicker) {
                    if (!hasAccess) {
                        super.onShiftRightClick(clicker);
                        return;
                    }

                    new ConfirmDeletionGui(manager, loadedArena.owner(), clicker);
                }
            });
        }

        ItemStack createArena = new ItemStack(Material.PAPER);
        ItemMeta createArenaMeta = createArena.getItemMeta();
        createArenaMeta.itemName(Component.text("Create new arena", NamedTextColor.GREEN));
        createArena.setItemMeta(createArenaMeta);

        ItemStack createPrivateArena = new ItemStack(Material.PAPER);
        ItemMeta createPrivateArenaMeta = createPrivateArena.getItemMeta();
        createPrivateArenaMeta.itemName(Component.text("Create new private arena", NamedTextColor.LIGHT_PURPLE));
        createPrivateArena.setItemMeta(createPrivateArenaMeta);

        MultiPageView view = new MultiPageView(player, arenas, "Arenas", true);
        view.setMenuSlot(new ArenaClickable(createArena, true), 0);
        view.setMenuSlot(new ArenaClickable(createPrivateArena, false), 1);

        view.showScreen();
    }

    class ArenaClickable extends Clickable {

        private final boolean isPublic;

        public ArenaClickable(ItemStack item, boolean isPublic) {
            super(item);
            this.isPublic = isPublic;
        }

        @Override
        protected void clicked(@NotNull Player clicker) {
            if (manager.hasArena(clicker)) {
                clicker.sendMessage(Component.text("You already have an arena! Delete it to create a new one.", NamedTextColor.RED));
                return;
            }

            new ArenaCategorySelectionGui(dao, manager, isPublic).open(clicker);
        }
    }

}
