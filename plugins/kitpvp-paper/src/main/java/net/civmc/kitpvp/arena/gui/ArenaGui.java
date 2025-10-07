package net.civmc.kitpvp.arena.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.civmc.kitpvp.arena.ArenaManager;
import net.civmc.kitpvp.arena.LoadedArena;
import net.civmc.kitpvp.arena.data.Arena;
import net.civmc.kitpvp.arena.data.ArenaDao;
import net.civmc.kitpvp.kit.KitCost;
import net.civmc.kitpvp.ranked.RankedQueueManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;

public class ArenaGui {

    private final ArenaDao dao;
    private final RankedQueueManager rankedQueueManager;
    private final double elo;
    private final ArenaManager manager;

    public ArenaGui(ArenaDao dao, RankedQueueManager rankedQueueManager, double elo, ArenaManager manager) {
        this.dao = dao;
        this.rankedQueueManager = rankedQueueManager;
        this.elo = elo;
        this.manager = manager;
    }

    public void open(Player player) {
        List<IClickable> arenas = new ArrayList<>();

        List<LoadedArena> loadedArenas = manager.getArenas();
        loadedArenas.sort(Comparator.comparingInt(s -> {
            World world = Bukkit.getWorld(manager.getArenaName(s));
            if (world == null) {
                return -1;
            } else {
                return world.getPlayerCount();
            }
        }));
        for (LoadedArena loadedArena : loadedArenas) {
            if (loadedArena.ranked()) {
                continue;
            }
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
            World world = Bukkit.getWorld(manager.getArenaName(loadedArena));
            if (world != null && world.getPlayerCount() > 0) {
                lore.add(Component.text("Currently playing:", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                for (Player worldPlayer : world.getPlayers()) {
                    if (worldPlayer.getGameMode() == GameMode.SURVIVAL) {
                        lore.add(Component.text("- " + worldPlayer.getName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                    }
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

        ItemStack queueArena = new ItemStack(Material.BEACON);
        ItemMeta queueArenaMeta = queueArena.getItemMeta();
        TextColor darkishBlue = TextColor.color(55, 159, 163);
        queueArenaMeta.itemName(Component.text("Queue for ranked 1v1", NamedTextColor.GOLD));
        queueArenaMeta.lore(List.of(
            Component.empty().append(Component.text("Your elo: ", darkishBlue).append(Component.text(Math.round(elo), NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false)),
            Component.empty().append(Component.text("Maximum kit cost: ", darkishBlue).append(Component.text(KitCost.MAX_POINTS + " points", NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false)),
            Component.empty().append(Component.text("Or type /ranked to join or leave the queue", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
            Component.empty().append(Component.text("Change your kit by clicking the diamond sword in a /kit", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
        ));
        queueArena.setItemMeta(queueArenaMeta);

//        ItemStack queueUnrankedArena = new ItemStack(Material.BEDROCK);
//        ItemMeta queueUnrankedArenaMeta = queueUnrankedArena.getItemMeta();
//        queueUnrankedArenaMeta.itemName(Component.text("Queue for unranked 1v1", NamedTextColor.GOLD));
//        queueUnrankedArenaMeta.lore(List.of(
//            Component.empty().append(Component.text("Maximum kit cost: ", darkishBlue).append(Component.text(KitCost.MAX_POINTS + " points", NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false)),
//            Component.empty().append(Component.text("Or type /unranked to join or leave the queue", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
//            Component.empty().append(Component.text("Change your kit by clicking the diamond sword in a /kit", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
//        ));
//        queueUnrankedArena.setItemMeta(queueUnrankedArenaMeta);

        ItemStack createArena = new ItemStack(Material.PAPER);
        ItemMeta createArenaMeta = createArena.getItemMeta();
        createArenaMeta.itemName(Component.text("Create new arena", NamedTextColor.GREEN));
        createArena.setItemMeta(createArenaMeta);

        ItemStack createPrivateArena = new ItemStack(Material.PAPER);
        ItemMeta createPrivateArenaMeta = createPrivateArena.getItemMeta();
        createPrivateArenaMeta.itemName(Component.text("Create new private arena", NamedTextColor.LIGHT_PURPLE));
        createPrivateArena.setItemMeta(createPrivateArenaMeta);

        MultiPageView view = new MultiPageView(player, arenas, "Arenas", true);
        view.setMenuSlot(new QueueClickable(queueArena), 0);
//        view.setMenuSlot(new QueueUnrankedClickable(queueUnrankedArena), 1);
        view.setMenuSlot(new DecorationStack(new ItemStack(Material.AIR)), 1);
        view.setMenuSlot(new ArenaClickable(createArena, true), 2);
        view.setMenuSlot(new ArenaClickable(createPrivateArena, false), 3);

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

    class QueueClickable extends Clickable {

        public QueueClickable(ItemStack item) {
            super(item);
        }

        @Override
        protected void clicked(@NotNull Player clicker) {
            if (!rankedQueueManager.isInQueue(clicker)) {
                rankedQueueManager.joinQueue(clicker, false);
                clicker.closeInventory();
            }
        }
    }


    class QueueUnrankedClickable extends Clickable {

        public QueueUnrankedClickable(ItemStack item) {
            super(item);
        }

        @Override
        protected void clicked(@NotNull Player clicker) {
            if (!rankedQueueManager.isInUnrankedQueue(clicker)) {
                rankedQueueManager.joinUnrankedQueue(clicker, false);
                clicker.closeInventory();
            }
        }
    }


}
