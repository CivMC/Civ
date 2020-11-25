package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.server.v1_16_R1.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.ratelimiting.RateLimiter;
import vg.civcraft.mc.civmodcore.ratelimiting.RateLimiting;
import vg.civcraft.mc.civmodcore.util.cooldowns.ICoolDownHandler;
import vg.civcraft.mc.civmodcore.util.cooldowns.MilliSecCoolDownHandler;

/**
 * Prevents "CivBreak" by denying continuos block break packages for
 * non-instabreaking
 */
public class AntiFastBreak extends BasicHack {

	@AutoLoad
	private boolean debug = false;

	private Map<UUID, Map<Location, Long>> miningLocations;
	private RateLimiter violationLimiter;

	@AutoLoad
	private double laggLenciency;
	@AutoLoad
	private long breakDenyDuration = 5000;

	private ICoolDownHandler<UUID> punishCooldown;

	public AntiFastBreak(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
		miningLocations = new TreeMap<>();
		violationLimiter = RateLimiting.createRateLimiter("antiCivBreak", 10, 10, 1, 2000L);
		if (config.isEnabled()) {
			registerPacketListener();
			Bukkit.getPluginManager().registerEvents(this, plugin);
		}
	}

	public static BasicHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

	private void registerPacketListener() {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		manager.addPacketListener(new PacketAdapter(SimpleAdminHacks.instance(), PacketType.Play.Client.BLOCK_DIG) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				BlockPosition pos = packet.getBlockPositionModifier().read(0);
				Location loc = pos.toLocation(event.getPlayer().getWorld());
				switch (packet.getPlayerDigTypes().read(0)) {
					case START_DESTROY_BLOCK:
						handleStartDigging(event.getPlayer(), loc);
						return;
					case STOP_DESTROY_BLOCK:
						handleFinishingDigging(event.getPlayer(), loc);
						return;
					default:
						// some other stuff we dont care about
						return;
				}
			}
		});
	}

	@EventHandler
	public void logOff(PlayerQuitEvent e) {
		miningLocations.remove(e.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void blockBreak(BlockBreakEvent e) {
		if (punishCooldown != null && punishCooldown.onCoolDown(e.getPlayer().getUniqueId())) {
			if (debug) {
				plugin().getLogger()
						.info("Denying block break for " + e.getPlayer().getName() + " due cooldown for another "
								+ punishCooldown.getRemainingCoolDown(e.getPlayer().getUniqueId()));
			}
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Denying break due to abnormal break speed");
		}
	}

	private void handleStartDigging(Player player, Location loc) {
		Map<Location, Long> miningLocs = miningLocations.computeIfAbsent(player.getUniqueId(), p -> new HashMap<>());
		miningLocs.putIfAbsent(loc, System.currentTimeMillis());
	}

	private void reward(Player player) {
		violationLimiter.addTokens(player.getUniqueId(), 1);
	}

	private void handleFinishingDigging(Player player, Location loc) {
		Map<Location, Long> miningLocs = miningLocations.computeIfAbsent(player.getUniqueId(), p -> new HashMap<>());
		int ticksToBreak = getTicksToBreak(loc.getBlock(), player);
		Long timeStarted = miningLocs.remove(loc);
		if (timeStarted == null) {
			if (ticksToBreak > 1) {
				if (debug) {
					plugin().getLogger().info(player.getName() + " tried to instabreak non-instabreakable block");
				}
				punish(player);
			} else {
				reward(player);
			}
			return;
		}
		if (ticksToBreak == 0) {
			if (debug) {
				plugin().getLogger().info(player.getName() + " instabroke allowed block " + loc.getBlock().toString());
			}
			reward(player);
			return;
		}

		long msToBreak = ticksToBreak * 50L;
		long now = System.currentTimeMillis();
		long timePassed = now - timeStarted;
		if (debug) {
			plugin().getLogger().info("Measured " + timePassed + " for allowed time of " + msToBreak);
		}
		miningLocs.put(loc, now);
		if (timePassed * laggLenciency < msToBreak) {
			punish(player);
		} else {
			reward(player);
		}
	}

	private void punish(Player player) {
		if (punishCooldown == null) {
			// delayed instanciation, because config values are not available in constructor
			punishCooldown = new MilliSecCoolDownHandler<>(breakDenyDuration);
		}
		if (debug) {
			plugin().getLogger().info("Attempting to decrement token count for " + player.getName());
		}
		if (!violationLimiter.pullToken(player)) {
			if (debug) {
				plugin().getLogger().info("Could not decrement token count for " + player.getName() + ", punishing...");
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(SimpleAdminHacks.instance(), () -> {
				punishCooldown.putOnCoolDown(player.getUniqueId());
				plugin().getLogger()
						.info(String.format("%s is possibly using civ break, fast break detected", player.getName()));
				player.sendMessage(ChatColor.RED + "You are breaking blocks too fast");
			});
		}
	}

	private static float getDamagePerTick(Material mat, Player player) {
		ItemStack tool = player.getInventory().getItemInMainHand();
		IBlockData blockData = getNMSBlockData(mat);
		if (blockData == null) {
			throw new IllegalArgumentException("Could not determine block break type for " + mat);
		}
		// if you ever need to version upgrade this, search for a method in n.m.s.Item
		// calling
		// "getDestroySpeed(this,blockData)" in n.m.s.ItemStack
		float damagePerTick = CraftItemStack.asNMSCopy(tool).a(blockData);
		// above method does not include efficiency or haste, so we add it ourselves
		int effLevel = tool.getEnchantmentLevel(Enchantment.DIG_SPEED);
		int efficiencyBonus = 0;
		if (effLevel > 0 && damagePerTick > 1.0) { //damage per tick greater than 1.0 signals proper tool
			efficiencyBonus = effLevel * effLevel + 1;
		}
		damagePerTick += efficiencyBonus;
		int hasteLevel = 0;
		PotionEffect hasteEffect = player.getPotionEffect(PotionEffectType.FAST_DIGGING);
		if (hasteEffect != null) {
			// amplifier of 0 is potion effect at level one
			hasteLevel = hasteEffect.getAmplifier() + 1;
		}
		damagePerTick *= 1.0 + 0.2 * hasteLevel;
		return damagePerTick;
	}

	private static int getTicksToBreak(Block b, Player p) {
		Material mat = b.getType();
		if (!mat.isBlock() || MaterialAPI.isAir(mat)) {
			// lagg, player is breaking a block already gone
			return 0;
		}
		float damageToDeal = mat.getHardness() * 30;
		float damagePerTick = getDamagePerTick(mat, p);
		if (damageToDeal <= damagePerTick) {
			// instabreak
			return 0;
		}
		return (int) Math.ceil(damageToDeal / damagePerTick);
	}

	private static IBlockData getNMSBlockData(Material mat) {
		net.minecraft.server.v1_16_R1.Block nmsBlock = org.bukkit.craftbukkit.v1_16_R1.util.CraftMagicNumbers
				.getBlock(mat);
		if (nmsBlock == null) {
			return null;
		}
		return nmsBlock.getBlockData();
	}

}
