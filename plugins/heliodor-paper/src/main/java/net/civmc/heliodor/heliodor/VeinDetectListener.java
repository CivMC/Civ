package net.civmc.heliodor.heliodor;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.vein.VeinCache;
import net.civmc.heliodor.vein.data.Vein;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

public class VeinDetectListener implements Listener {

    private static final int VEIN_DETECT_MARGIN = 1;
    public static final int DURABILITY_COST = 50;

    private final Map<Player, Instant> confirming = new WeakHashMap<>();

    private final VeinCache cache;
    private final int maxVeins;

    public VeinDetectListener(VeinCache cache, int maxVeins) {
        this.cache = cache;
        this.maxVeins = maxVeins;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != EquipmentSlot.HAND || !player.isSneaking()) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!HeliodorPickaxe.isPickaxe(item)) {
            return;
        }
        event.setCancelled(true);

        Damageable meta = (Damageable) item.getItemMeta();
        if (meta.getMaxDamage() - meta.getDamage() < DURABILITY_COST) {
            player.sendMessage(Component.text("Your pickaxe needs at least " + DURABILITY_COST + " durability to count global veins", NamedTextColor.RED));
            return;
        }

        Instant confirmAt = confirming.get(player);
        if (confirmAt == null || confirmAt.plusSeconds(10).isBefore(Instant.now())) {
            confirming.put(player, Instant.now());
            player.sendMessage(Component.empty().color(NamedTextColor.YELLOW)
                .append(Component.text("You are about to count the global number of veins. It will cost "))
                .append(Component.text(DURABILITY_COST, NamedTextColor.GOLD))
                .append(Component.text(" durability. Shift right click again to confirm.")));
            return;
        }

        int veinCount = 0;
        for (Vein vein : cache.getVeins()) {
            if (vein.oresRemaining() >= vein.ores() * 0.5) {
                veinCount++;
            }
        }

        item.damage(DURABILITY_COST, player);

        Logger logger = JavaPlugin.getPlugin(HeliodorPlugin.class).getLogger();

        int diff = maxVeins - veinCount;
        if (diff <= VEIN_DETECT_MARGIN) {
            logger.info("Player " + player.getName() + " saw " + veinCount + " veins");
            player.sendMessage(Component.empty().color(NamedTextColor.YELLOW)
                .append(Component.text("There are currently "))
                .append(Component.text(veinCount, NamedTextColor.GOLD))
                .append(Component.text(" meteoric iron veins, out of a maximum of "))
                .append(Component.text(maxVeins, NamedTextColor.GOLD))
                .append(Component.text(" veins.")));
            player.sendMessage(Component.text("If the maximum number of veins is reached, then veins will stop spawning until a vein is mined.", NamedTextColor.YELLOW));
        } else {
            logger.info("Player " + player.getName() + " saw " + (maxVeins - VEIN_DETECT_MARGIN - 1) + " veins or fewer");
            player.sendMessage(Component.empty().color(NamedTextColor.YELLOW)
                .append(Component.text("There are currently less than "))
                .append(Component.text(maxVeins - VEIN_DETECT_MARGIN, NamedTextColor.GOLD))
                .append(Component.text(" meteoric iron veins, out of a maximum of "))
                .append(Component.text(maxVeins, NamedTextColor.GOLD))
                .append(Component.text(" veins.")));
        }
    }
}
