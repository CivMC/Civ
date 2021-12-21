package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.google.common.base.Strings;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
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
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.utilities.MoreCollectionUtils;

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
		final var angerLevel = bee.getRemainingPersistentAngerTime();
		Bukkit.getScheduler().runTask(this.plugin, () -> {
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

	private static final String BEES_LIST_KEY = "Bees";
	private static final String BEE_DATA_KEY = "EntityData";
	private static final String BEE_NAME_KEY = "CustomName";

	private static final Set<Material> HIVE_MATERIALS = Set.of(
			Material.BEE_NEST,
			Material.BEEHIVE);

	@EventHandler(ignoreCancelled = true)
	public void showHiveDetails(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		final ItemStack held = event.getItem();
		if (ItemUtils.isValidItem(held)) {
			return;
		}
		final Block block = Objects.requireNonNull(event.getClickedBlock());
		if (!HIVE_MATERIALS.contains(block.getType())) {
			return;
		}
		final BeehiveBlockEntity beehive = getBeeHive(block);
		if (beehive.isEmpty()) {
			player.sendMessage(ChatColor.GOLD + "There aren't any bees in that hive.");
			return;
		}
		final List<BeeData> bees = getBeesFromHive(beehive);
		final int numberOfUnnamed = MoreCollectionUtils.numberOfMatches(bees, BeeData::isNameless);
		bees.removeIf(BeeData::isNameless);
		// Start building response
		final var response = Component.text().color(NamedTextColor.GOLD);
		final Iterator<BeeData> nameIterator = bees.iterator();
		boolean doneFirstElement = false;
		while (nameIterator.hasNext()) {
			final BeeData bee = nameIterator.next();
			if (nameIterator.hasNext() || numberOfUnnamed > 0) {
				if (doneFirstElement) {
					response.append(Component.text(", "));
				}
			}
			else if (numberOfUnnamed == 0) {
				response.append(Component.text(", and "));
			}
			assert bee.name != null;
			response.append(bee.name);
			doneFirstElement = true;
		}
		if (numberOfUnnamed > 0) {
			if (bees.isEmpty()) {
				response.append(Component.text("There are " + numberOfUnnamed + " bees"));
			}
			else {
				response.append(Component.text(", and " + numberOfUnnamed + " others are"));
			}
		}
		else {
			response.append(Component.text(" are"));
		}
		if (beehive.isSedated()) {
			response.append(Component.text(" sedated"));
		}
		else {
			response.append(Component.text(" happily buzzing"));
		}
		response.append(Component.text(" in that hive."));
		player.sendMessage(response);
	}

	private static BeehiveBlockEntity getBeeHive(@Nonnull final Block block) {
		final CraftBlock craftBlock = (CraftBlock) block;
		final CraftWorld craftWorld = craftBlock.getCraftWorld();
		final ServerLevel worldServer = craftWorld.getHandle();
		final BlockEntity tileEntity = worldServer.getBlockEntity(craftBlock.getPosition());
		return (BeehiveBlockEntity) Objects.requireNonNull(tileEntity);
	}

	private static List<BeeData> getBeesFromHive(@Nonnull final BeehiveBlockEntity hive) {
		final CompoundTag nbt = new CompoundTag();
//		hive.save(nbt); // Serialise onto the NBT compound
		return Stream.of(nbt.getCompound(BEES_LIST_KEY))
				.map(bee -> bee.getCompound(BEE_DATA_KEY))
				.map(BeeData::new)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private static final class BeeData {

		public final Component name;

		public BeeData(@Nonnull final CompoundTag nbt) {
			// Parse name
			final String rawName = nbt.getString(BEE_NAME_KEY);
			if (Strings.isNullOrEmpty(rawName)) {
				this.name = null;
			}
			else {
				final var componentName = GsonComponentSerializer.gson().deserialize(rawName);
				this.name = ChatUtils.isNullOrEmpty(componentName) ? null : componentName;
			}
		}

		public boolean isNameless() {
			return this.name == null;
		}

	}

}
