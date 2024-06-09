package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Bee;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public final class BeeKeeping extends BasicHack {
	private static final Random RANDOM = new SecureRandom();

	public BeeKeeping(
		final @NotNull SimpleAdminHacks plugin,
		final @NotNull BasicHackConfig config
	) {
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
		EntityType.RABBIT
	);

	@AutoLoad
	private double spawnChance;

	@EventHandler(
		ignoreCancelled = true
	)
	private void beeSpawning(
		final @NotNull CreatureSpawnEvent event
	) {
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

	@EventHandler(
		priority = EventPriority.MONITOR,
		ignoreCancelled = true
	)
	public void attackedByBee(
		final @NotNull EntityDamageByEntityEvent event
	) {
		final Entity attacker = event.getDamager();
		if (attacker.getType() != EntityType.BEE
				|| event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK // Ignore if not attacked
				|| this.severStingerChance >= 1.0d // Ignore if severance is guaranteed
				|| (this.severStingerChance > 0.0d && RANDOM.nextDouble() <= this.severStingerChance)) {
			return;
		}
		final var bee = (Bee) ((CraftEntity) attacker).getHandle();
		// Undo bee pacification and allow the bee to sting again
		/** See the code in {@link NeutralMob#stopBeingAngry()} */
		final LivingEntity lastDamageCause = bee.getLastHurtByMob();
		final UUID angerTarget = bee.getPersistentAngerTarget();
		final LivingEntity goalTarget = bee.getTarget();
		final int angerLevel = bee.getRemainingPersistentAngerTime();
		Bukkit.getScheduler().runTask(plugin(), () -> {
			bee.setHasStung(false);
			// Reset the above values
			bee.setLastHurtByMob(lastDamageCause);
			bee.setPersistentAngerTarget(angerTarget);
			bee.setTarget(goalTarget, EntityTargetEvent.TargetReason.FORGOT_TARGET, false); // Params: target, cause, emit event
			bee.setRemainingPersistentAngerTime(angerLevel);
		});
	}

	// ------------------------------------------------------------
	// Bee Hive/Nest Details
	// ------------------------------------------------------------

	@EventHandler(
		ignoreCancelled = true
	)
	public void showHiveDetails(
		final @NotNull PlayerInteractEvent event
	) {
		final Player player = event.getPlayer();
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (event.getHand() != EquipmentSlot.HAND) { // Prevent double-interaction
			return;
		}
		if (event.hasItem()) {
			return;
		}
		final Block block = event.getClickedBlock();
		if (block == null || !(block.getState() instanceof final Beehive beeHive)) {
			return;
		}
		final int numberOfBees = beeHive.getEntityCount();
		if (numberOfBees < 1) {
			player.sendMessage(Component.text(
				"There aren't any bees in that hive.",
				NamedTextColor.GOLD
			));
			return;
		}
		player.sendMessage(Component.text(
			"There "
				+ (numberOfBees == 1 ? "is 1 bee " : "are " + numberOfBees + " bees ")
				+ (beeHive.isSedated() ? "sedated " : "happily buzzing ")
				+ "in that hive.",
			NamedTextColor.GOLD
		));
	}
}
