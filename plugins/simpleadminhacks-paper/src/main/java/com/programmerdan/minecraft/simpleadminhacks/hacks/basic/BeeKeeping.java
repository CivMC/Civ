package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.entity.animal.bee.Bee;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public final class BeeKeeping extends BasicHack {

    private static final Random RANDOM = new SecureRandom();

    public BeeKeeping(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    // ------------------------------------------------------------
    // Natural Bee Spawning
    // ------------------------------------------------------------

    private static final Set<EntityType> NEUTRAL_MOBS = Set.of(
        EntityType.CHICKEN,
        EntityType.COW,
        EntityType.PIG,
        EntityType.SHEEP,
        EntityType.RABBIT);

    @AutoLoad
    private double spawnChance;

    @EventHandler(ignoreCancelled = true)
    public void beeSpawning(final CreatureSpawnEvent event) {
        if (!NEUTRAL_MOBS.contains(event.getEntityType())
            || event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL
            || this.spawnChance <= 0.0d
            || (this.spawnChance < 1.0d && RANDOM.nextDouble() > this.spawnChance)) {
            return;
        }
        final Location location = event.getLocation();
        final World world = location.getWorld();
        world.spawnEntity(location, EntityType.BEE);
        event.setCancelled(true);
    }

    // ------------------------------------------------------------
    // Bee Combat
    // ------------------------------------------------------------

    @AutoLoad
    private double severStingerChance;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void attackedByBee(final EntityDamageByEntityEvent event) {
        final Entity attacker = event.getDamager();
        if (attacker.getType() != EntityType.BEE
            || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK // Ignore if not attacked
            || this.severStingerChance >= 1.0d // Ignore if severance is guaranteed
            || (this.severStingerChance > 0.0d && RANDOM.nextDouble() <= this.severStingerChance)) {
            return;
        }
        final Bee bee = (Bee) ((CraftEntity) attacker).getHandle();
        // Undo bee pacification and allow the bee to sting again
        /** See the code in {@link IEntityAngerable#pacify()} */
        final var lastDamageCause = bee.getLastHurtByMob();
        final var angerTarget = bee.getPersistentAngerTarget();
        final var goalTarget = bee.getTarget();
        final var angerLevel = bee.getPersistentAngerEndTime();
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            bee.setHasStung(false);
            // Reset the above values
            bee.setLastHurtByMob(lastDamageCause);
            bee.setPersistentAngerTarget(angerTarget);
            bee.setTarget(goalTarget, EntityTargetEvent.TargetReason.FORGOT_TARGET); // Params: target, cause, emit event
            bee.setPersistentAngerEndTime(angerLevel);
        });
    }

    // ------------------------------------------------------------
    // Bee Hive/Nest Details
    // ------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void showHiveDetails(final PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        final Material blockMaterial = block.getType();
        if (!Tag.BEEHIVES.isTagged(blockMaterial)) {
            return;
        }
        final ItemStack held = event.getItem();
        if (ItemUtils.isValidItem(held)) {
            return;
        }
        record HiveState(int count, boolean sedated) {}
        final HiveState state = switch (block.getState()) {
            case final org.bukkit.block.Beehive hive -> new HiveState(hive.getEntityCount(), hive.isSedated());
            case null, default -> null;
        };
        enum HoneyLevel { EMPTY, PARTIAL, FULL }
        final HoneyLevel level = switch (block.getBlockData()) {
            case final org.bukkit.block.data.type.Beehive hive -> {
                final int tempLevel = hive.getHoneyLevel();
                if (tempLevel <= 0) {
                    yield HoneyLevel.EMPTY;
                }
                if (tempLevel >= hive.getMaximumHoneyLevel()) {
                    yield HoneyLevel.FULL;
                }
                yield HoneyLevel.PARTIAL;
            }
            case null, default -> null;
        };
        event.getPlayer().sendMessage(Component.textOfChildren(
            Component.text("Hive type: ", NamedTextColor.YELLOW),
            Component.text()
                .color(NamedTextColor.WHITE)
                .append(Component.translatable(blockMaterial))
                .hoverEvent(HoverEvent.showText(Component.translatable(
                    "chat.coordinates",
                    Component.text(block.getX()),
                    Component.text(block.getY()),
                    Component.text(block.getZ())
                ))),
            Component.newline(),
            Component.text("Bee count: ", NamedTextColor.YELLOW),
            switch (state) {
                case null -> Component.text("unknown", NamedTextColor.RED);
                case HiveState(var amount, _) when amount == 1 -> Component.text("1 bee", NamedTextColor.WHITE);
                case HiveState(var amount, _) -> Component.text("%s bees".formatted(amount), NamedTextColor.WHITE);
            },
            Component.newline(),
            Component.text("Condition: ", NamedTextColor.YELLOW),
            switch (state) {
                case null -> Component.text("unknown", NamedTextColor.RED);
                case HiveState(_, var sedated) when sedated -> Component.text("sedated", NamedTextColor.WHITE);
                case HiveState(_, _) -> Component.text("normal", NamedTextColor.WHITE);
            },
            Component.newline(),
            Component.text("Honey level: ", NamedTextColor.YELLOW),
            switch (level) {
                case null -> Component.text("unknown", NamedTextColor.RED);
                case EMPTY -> Component.text("empty", NamedTextColor.WHITE);
                case PARTIAL -> Component.text("insufficient", NamedTextColor.WHITE);
                case FULL -> Component.text("full", NamedTextColor.WHITE);
            }
        ));
    }

}
