package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.programmerdan.minecraft.banstick.handler.BanHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.collections4.ComparatorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.utilities.MoreCollectionUtils;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class CmdShowAllPearls extends PearlCommand {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN = 2_000;
    private static final Set<UUID> TOGGLES = new HashSet<>();

    public CmdShowAllPearls(final ExilePearlApi pearlApi) {
        super(pearlApi);
        this.aliases.add("showall");
        setHelpShort("Lists all pearled players.");

        this.senderMustBePlayer = true;
    }

    @Override
    public void perform() {
        final Player sender = player();
        if (onCoolDown(sender)) return;
        generateOpenPearlsMenu(sender);
    }

    private boolean onCoolDown(Player sender) {
        final long now = System.currentTimeMillis();
        final long previousUseTime = COOLDOWNS.compute(sender.getUniqueId(),
            (uuid, value) -> value == null ? 0L : value); // This is better than getOrDefault()
        if (previousUseTime > (now - COOLDOWN)) {
            sender.sendMessage(ChatColor.RED + "You can't do that yet.");
            return true;
        }
        COOLDOWNS.put(sender.getUniqueId(), now);
        return false;
    }

    private void generateOpenPearlsMenu(Player sender) {
        final boolean bannedPearlToggle = TOGGLES.contains(sender.getUniqueId());
        final Location senderLocation = sender.getLocation();
        final double pearlExclusionRadius = this.plugin.getPearlConfig().getRulePearlRadius() * 1.2;
        final boolean isBanStickEnabled = this.plugin.isBanStickEnabled();

        final List<Supplier<IClickable>> contentSuppliers = this.plugin.getPearls().stream()
            // Sort pearls from newest to oldest
            .sorted(ComparatorUtils.reversedComparator(Comparator.comparing(ExilePearl::getPearledOn)))
            .filter((pearl) -> {
                if (!bannedPearlToggle) {
                    final boolean isPlayerBanned = isBanStickEnabled
                        && BanHandler.isPlayerBanned(pearl.getPlayerId());

                    return !isPlayerBanned;
                } else return true;
            })
            .<Supplier<IClickable>>map((pearl) -> () -> {
                final Location pearlLocation = pearl.getLocation();
                final boolean isPlayerBanned = isBanStickEnabled
                    && BanHandler.isPlayerBanned(pearl.getPlayerId());

                final int distanceToPearl = WorldUtils.blockDistance(senderLocation, pearlLocation, true);
                // distance is -1 if the pearl is in a different world
                final boolean showLocation = distanceToPearl != -1 && distanceToPearl <= pearlExclusionRadius;

                CompletableFuture<ItemStack> itemReadyFuture = new CompletableFuture<>();
                final ItemStack item = isPlayerBanned
                    ? new ItemStack(Material.ARMOR_STAND)
                    : CivModCorePlugin.getInstance().getSkinCache().getHeadItem(
                    pearl.getPlayerId(),
                    () -> new ItemStack(Material.ENDER_PEARL),
                    itemReadyFuture::complete);

                Consumer<ItemStack> itemMetaMod = itemToMod ->
                    ItemUtils.handleItemMeta(itemToMod, (ItemMeta meta) -> {
                        // Pearled player's name
                        meta.displayName(
                            ChatUtils.nonItalic()
                                .color(NamedTextColor.AQUA)
                                .content(pearl.getPlayerName())
                                .append(
                                    isPlayerBanned
                                        ? Component.text(" <banned>", NamedTextColor.RED)
                                        : Component.empty()
                                )
                                .build()
                        );

                        meta.lore(List.of(
                            // Pearl type
                            ChatUtils.nonItalic()
                                .color(NamedTextColor.GREEN)
                                .content(pearl.getItemName())
                                .build(),
                            // Pearled player's name and hash
                            ChatUtils.nonItalic()
                                .color(NamedTextColor.GOLD)
                                .content("Player: ")
                                .append(
                                    Component.text(pearl.getPlayerName(), NamedTextColor.GRAY),
                                    Component.space(),
                                    Component.text(Integer.toString(pearl.getPearlId(), 36).toUpperCase(), NamedTextColor.DARK_GRAY)
                                )
                                .build(),
                            // Pearled Date
                            ChatUtils.nonItalic()
                                .color(NamedTextColor.GOLD)
                                .content("Pearled: ")
                                .append(Component.text(DATE_FORMAT.format(pearl.getPearledOn()), NamedTextColor.GRAY))
                                .build(),
                            // Killer's name
                            ChatUtils.nonItalic()
                                .color(NamedTextColor.GOLD)
                                .content("Killed by: ")
                                .append(Component.text(pearl.getKillerName(), NamedTextColor.GRAY))
                                .build()
                        ));

                        if (showLocation) {
                            MetaUtils.addComponentLore(
                                meta,
                                // Pearl location
                                ChatUtils.nonItalic()
                                    .color(NamedTextColor.GOLD)
                                    .content("Location: ")
                                    .append(
                                        Component.text(pearlLocation.getWorld().getName(), NamedTextColor.WHITE),
                                        Component.space(),
                                        Component.text(pearlLocation.getBlockX(), NamedTextColor.RED),
                                        Component.space(),
                                        Component.text(pearlLocation.getBlockY(), NamedTextColor.GREEN),
                                        Component.space(),
                                        Component.text(pearlLocation.getBlockZ(), NamedTextColor.BLUE)
                                    )
                                    .build(),
                                // Waypoint
                                Component.space(),
                                ChatUtils.nonItalic()
                                    .color(NamedTextColor.GREEN)
                                    .content("Click to receive a waypoint")
                                    .build()
                            );
                        }
                        return true;
                    });

                itemMetaMod.accept(item);

                return new Clickable(item) {
                    @Override
                    protected void clicked(final Player clicker) {
                        if (showLocation) {
                            final var location = pearl.getLocation();
                            if (!Objects.equals(location.getWorld(), clicker.getWorld())) {
                                clicker.sendMessage(ChatColor.RED + "That pearl is in a different world!");
                                return;
                            }
                            clicker.sendMessage('['
                                + "name:" + pearl.getPlayerName() + "'s pearl,"
                                + "x:" + location.getBlockX() + ','
                                + "y:" + location.getBlockY() + ','
                                + "z:" + location.getBlockZ()
                                + ']');
                        }
                    }

                    @Override
                    public void addedToInventory(ClickableInventory inv, int slot) {
                        itemReadyFuture.thenAccept(item -> {
                            itemMetaMod.accept(item);
                            this.item = item;
                            inv.setItem(item, slot);
                        });
                        super.addedToInventory(inv, slot);
                    }
                };
            })
            .collect(Collectors.toCollection(ArrayList::new));

        if (contentSuppliers.isEmpty()) {
            final var item = new ItemStack(Material.BARRIER);
            ItemUtils.setComponentDisplayName(
                item,
                ChatUtils.nonItalic()
                    .color(NamedTextColor.RED)
                    .content("There are currently no pearls.")
                    .build()
            );
            contentSuppliers.add(() -> new DecorationStack(item));
        }

        List<IClickable> lazyContents = MoreCollectionUtils.asLazyList(contentSuppliers);
        final var pageView = new MultiPageView(sender, lazyContents, "All Pearls", true);

        pageView.setMenuSlot(constructBannedPearlsToggleClick(bannedPearlToggle), 4);
        pageView.showScreen();
    }

    private IClickable constructBannedPearlsToggleClick(final boolean bannedPearlToggle) {
        final var item = new ItemStack(Material.BARRIER);
        ItemUtils.handleItemMeta(item, (final ItemMeta meta) -> {
            meta.displayName(
                ChatUtils.nonItalic()
                    .color(NamedTextColor.GOLD)
                    .content("Toggle banned pearls")
                    .build()
            );
            meta.lore(List.of(
                ChatUtils.nonItalic()
                    .color(NamedTextColor.AQUA)
                    .content("Currently turned " + (bannedPearlToggle ? "on" : "off"))
                    .build()
            ));
            return true;
        });
        return new Clickable(item) {
            @Override
            public void clicked(final Player clicker) {
                if (onCoolDown(clicker)) return;
                if (!bannedPearlToggle) {
                    TOGGLES.add(clicker.getUniqueId());
                } else {
                    TOGGLES.remove(clicker.getUniqueId());
                }
                clicker.sendMessage(Component.text()
                    .color(NamedTextColor.GREEN)
                    .content("Banned pearls toggled " + (!bannedPearlToggle ? "on" : "off"))
                    .build());
                generateOpenPearlsMenu(clicker);
            }
        };
    }
}
