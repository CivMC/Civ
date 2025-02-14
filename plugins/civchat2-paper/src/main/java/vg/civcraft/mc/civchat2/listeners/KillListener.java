package vg.civcraft.mc.civchat2.listeners;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civchat2.utility.CivChat2Config;
import vg.civcraft.mc.civchat2.utility.CivChat2SettingsManager;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public final class KillListener implements Listener {
    private static final Style ITALIC = Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE).build();

    private final CivChat2SettingsManager settings;
    private final CivChat2Config config;
    private final CivChatDAO dao;

    public KillListener(
        final @NotNull CivChat2Config config,
        final @NotNull CivChatDAO dao,
        final @NotNull CivChat2SettingsManager settings
    ) {
        this.config = Objects.requireNonNull(config);
        this.dao = Objects.requireNonNull(dao);
        this.settings = Objects.requireNonNull(settings);
    }

    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true
    )
    public void handleDeathMessage(
        final @NotNull PlayerDeathEvent event
    ) {
        switch (this.config.getDeathMessageType()) {
            case DISABLED -> {
                event.deathMessage(null);
                return;
            }
            case VANILLA -> {
                return;
            }
            case CUSTOM -> {
                event.deathMessage(null);
            }
        }
        final Player victim = event.getEntity();
        if (victim.getKiller() == null) {
            return;
        }
        final int killBroadcastRange = this.config.getKillBroadcastRange();
        if (killBroadcastRange <= 0) {
            return;
        }
        final Player killer = victim.getKiller();
        if (!this.settings.getSendOwnKills(killer.getUniqueId())) {
            return;
        }
        final Component killMessage; {
            final TextComponent.Builder killMessageBuilder = Component.text().color(NamedTextColor.DARK_GRAY).append(
                Component.text(victim.getName(), ITALIC),
                Component.text(" was killed by "),
                Component.text(killer.getName(), ITALIC)
            );
            if (this.settings.includeWeaponInKillBroadcasts(killer.getUniqueId())) {
                final ItemStack weapon = killer.getInventory().getItemInMainHand();
                if (ItemUtils.isEmptyItem(weapon)) {
                    killMessageBuilder.append(
                        Component.text(" by hand")
                    );
                }
                else if (getWordbankName(weapon) instanceof final Component name) {
                    final var hoverItem = new ItemStack(weapon.getType(), weapon.getAmount()); {
                        final ItemMeta hoverMeta = hoverItem.getItemMeta();
                        hoverMeta.displayName(name);
                        hoverItem.setItemMeta(hoverMeta);
                    }
                    killMessageBuilder.append(
                        Component.text(" with "),
                        name.hoverEvent(hoverItem.asHoverEvent())
                    );
                }
                else {
                    final var hoverItem = new ItemStack(weapon.getType(), weapon.getAmount());
                    killMessageBuilder.append(
                        Component.text(" with a "),
                        Component.translatable(hoverItem.translationKey()).hoverEvent(hoverItem.asHoverEvent())
                    );
                }
            }
            killMessage = killMessageBuilder.build();
        }

        final Location victimLocation = victim.getLocation();
        for (final Player recipient : Bukkit.getOnlinePlayers()) {
            if (!Objects.equals(victim.getWorld(), recipient.getWorld())) {
                continue;
            }
            if (victimLocation.distance(recipient.getLocation()) > killBroadcastRange) {
                continue;
            }
            if (!this.settings.getReceiveKills(recipient.getUniqueId())) {
                continue;
            }
            if (!this.settings.getReceiveKillsFromIgnored(recipient.getUniqueId()) && this.dao.isIgnoringPlayer(recipient.getUniqueId(), killer.getUniqueId())) {
                continue;
            }
            recipient.sendMessage(killMessage);
        }
    }

    private static @Nullable Component getWordbankName(
        final @NotNull ItemStack item
    ) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        final Component displayName = meta.displayName();
        if (displayName == null || Component.empty().equals(displayName)) {
            return null;
        }
        // There *HAS* to be a better way of detecting wordbank other than this!
        if (displayName.children().isEmpty()) {
            return null;
        }
        return displayName.asComponent();
    }
}
