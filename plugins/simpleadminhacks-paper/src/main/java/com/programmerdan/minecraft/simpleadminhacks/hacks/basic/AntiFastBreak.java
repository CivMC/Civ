package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.PacketManager;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.ICoolDownHandler;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.MilliSecCoolDownHandler;
import vg.civcraft.mc.civmodcore.utilities.ratelimiting.RateLimiter;
import vg.civcraft.mc.civmodcore.utilities.ratelimiting.RateLimiting;

/**
 * Prevents "CivBreak" by denying continuous block break packets.
 */
public final class AntiFastBreak extends BasicHack {

	private final PacketManager packets;
	private final Map<UUID, Map<Location, Long>> miningLocations;
	private final RateLimiter violationLimiter;
	private final RateLimiter loggerLimiter;
	private ICoolDownHandler<UUID> punishCooldown;

	@AutoLoad(id = "laggLenciency") // Continue to support typo
	private double lagLeniency;

	@AutoLoad
	private long breakDenyDuration;

	public AntiFastBreak(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
		this.packets = new PacketManager();
		this.miningLocations = new TreeMap<>();
		this.violationLimiter = RateLimiting.createRateLimiter("antiCivBreak", 10, 10, 1, 2_000L);
		this.loggerLimiter = RateLimiting.createRateLimiter("antiCivBreakLogger", 4, 4, 1, 10_000L);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		if (this.breakDenyDuration <= 0) {
			this.breakDenyDuration = 3000;
			plugin().warning("Invalid break deny duration, defaulting to 3000ms");
		}
		this.punishCooldown = new MilliSecCoolDownHandler<>(this.breakDenyDuration);
		this.packets.addAdapter(new PacketAdapter(plugin(), PacketType.Play.Client.BLOCK_DIG) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				final PacketContainer packet = event.getPacket();
				final Player player = event.getPlayer();
				final BlockPosition position = packet.getBlockPositionModifier().read(0);
				final Location location = position.toLocation(player.getWorld());
				switch (packet.getPlayerDigTypes().read(0)) {
					case START_DESTROY_BLOCK:
						handleStartDigging(player, location);
						return;
					case STOP_DESTROY_BLOCK:
						handleFinishingDigging(player, location);
						return;
					default:
						// some other stuff we dont care about
						//return;
				}
			}
		});
	}

	@Override
	public void onDisable() {
		this.packets.removeAllAdapters();
		super.onDisable();
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		this.miningLocations.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event) {
		final Player player = event.getPlayer();
		if (!this.punishCooldown.onCoolDown(player.getUniqueId())) {
			return;
		}
		player.sendMessage(ChatColor.RED + "Denying break due to abnormal break speed");
		plugin().debug("Denying block break for " + player.getName() + " due cooldown for another "
				+ TextUtil.formatDuration(this.punishCooldown.getRemainingCoolDown(player.getUniqueId())));
		event.setCancelled(true);
	}

	private void handleStartDigging(final Player player, final Location location) {
		this.miningLocations
				.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>())
				.putIfAbsent(location, System.currentTimeMillis());
	}

	private void reward(final Player player) {
		this.violationLimiter.addTokens(player.getUniqueId(), 1);
	}

	private void handleFinishingDigging(final Player player, final Location location) {
		final Map<Location, Long> miningLocs = this.miningLocations
				.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>());
		final int ticksToBreak = getTicksToBreak(location.getBlock(), player);
		final Long timeStarted = miningLocs.remove(location);
		if (timeStarted == null) {
			if (ticksToBreak > 1) {
				plugin().debug(player.getName() + " tried to instabreak non-instabreakable block");
				punish(player);
			}
			else {
				reward(player);
			}
			return;
		}
		if (ticksToBreak == 0) {
			plugin().debug(player.getName() + " instabroke allowed block " + location.getBlock());
			reward(player);
			return;
		}
		final long msToBreak = ticksToBreak * 50L;
		final long now = System.currentTimeMillis();
		final long timePassed = now - timeStarted;
		//plugin().debug("Measured " + timePassed + " for allowed time of " + TextUtil.formatDuration(msToBreak));
		miningLocs.put(location, now);
		if ((timePassed * this.lagLeniency) < msToBreak) {
			punish(player);
		}
		else {
			reward(player);
		}
	}

	private void punish(final Player player) {
		plugin().debug("Attempting to decrement token count for " + player.getName());
		if (!this.violationLimiter.pullToken(player)) {
			plugin().debug("Could not decrement token count for " + player.getName() + ", punishing...");
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin(), () -> {
				this.punishCooldown.putOnCoolDown(player.getUniqueId());
				if (this.loggerLimiter.pullToken(player.getUniqueId()) || plugin().isDebugEnabled()) {
					plugin().warning(player.getName() + " is possibly using civ break, fast break detected");
				}
				player.sendMessage(ChatColor.RED + "You are breaking blocks too fast");
			});
		}
	}

	private static float getDamagePerTick(final Material material, final Player player) {
		final ItemStack tool = player.getInventory().getItemInMainHand();
		final IBlockData blockData = getNMSBlockData(material);
		if (blockData == null) {
			throw new IllegalArgumentException("Could not determine block break type for " + material);
		}
		// if you ever need to version upgrade this, search for a method in n.m.s.Item
		// calling "getDestroySpeed(this,blockData)" in n.m.s.ItemStack
		float damagePerTick = CraftItemStack.asNMSCopy(tool).a(blockData);
		// above method does not include efficiency or haste, so we add it ourselves
		final int effLevel = tool.getEnchantmentLevel(Enchantment.DIG_SPEED);
		int efficiencyBonus = 0;
		if (effLevel > 0 && damagePerTick > 1.0) { //damage per tick greater than 1.0 signals proper tool
			efficiencyBonus = effLevel * effLevel + 1;
		}
		damagePerTick += efficiencyBonus;
		int hasteLevel = 0;
		final PotionEffect hasteEffect = player.getPotionEffect(PotionEffectType.FAST_DIGGING);
		if (hasteEffect != null) {
			// amplifier of 0 is potion effect at level one
			hasteLevel = hasteEffect.getAmplifier() + 1;
		}
		damagePerTick *= 1.0 + 0.2 * hasteLevel;
		return damagePerTick;
	}

	private static int getTicksToBreak(final Block block, final Player player) {
		final Material material = block.getType();
		if (!material.isBlock() || MaterialUtils.isAir(material)) {
			// lagg, player is breaking a block already gone
			return 0;
		}
		final float damageToDeal = material.getHardness() * 30;
		final float damagePerTick = getDamagePerTick(material, player);
		if (damageToDeal <= damagePerTick) {
			// instabreak
			return 0;
		}
		return (int) Math.ceil(damageToDeal / damagePerTick);
	}

	private static IBlockData getNMSBlockData(final Material material) {
		final net.minecraft.world.level.block.Block nmsBlock = CraftMagicNumbers.getBlock(material);
		if (nmsBlock == null) {
			return null;
		}
		return nmsBlock.getBlockData();
	}

}
