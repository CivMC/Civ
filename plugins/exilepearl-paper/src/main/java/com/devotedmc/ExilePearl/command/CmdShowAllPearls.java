package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.programmerdan.minecraft.banstick.handler.BanHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.collections4.ComparatorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class CmdShowAllPearls extends PearlCommand {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");
	private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
	private static final long COOLDOWN = 10_000; // 10 seconds

	private boolean isBanStickEnabled = false;

	public CmdShowAllPearls(final ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("showall");
		setHelpShort("Lists all pearled players.");

		this.senderMustBePlayer = true;
	}

	@Override
	public void perform() {
		final Player player = player();

		final long now = System.currentTimeMillis();
		final long previousUseTime = COOLDOWNS.compute(player.getUniqueId(),
				(uuid, value) -> value == null ? 0L : value); // This is better than getOrDefault()
		if (previousUseTime > (now - COOLDOWN)) {
			player.sendMessage(ChatColor.RED + "You can't do that yet.");
			return;
		}
		COOLDOWNS.put(player.getUniqueId(), now);

		this.isBanStickEnabled = this.plugin.isBanStickEnabled();

		final Location playerLocation = player.getLocation();
		final int pearlExclusionRadius = this.plugin.getPearlConfig().getRulePearlRadius();

		final List<IClickable> contents = this.plugin.getPearls().stream()
				.sorted(ComparatorUtils.reversedComparator(Comparator.comparing(ExilePearl::getPearledOn)))
				.map(pearl -> {
					final Location pearlLocation = pearl.getLocation();
					final boolean isPlayerBanned = isPlayerBanned(pearl.getPlayerId());
					final boolean showLocation = WorldUtils.blockDistance(
							playerLocation, pearlLocation, true) <= pearlExclusionRadius;

					final var item = isPlayerBanned ?
							new ItemStack(Material.ARMOR_STAND) :
							getSkullForPlayer(pearl.getPlayerId());

					ItemUtils.handleItemMeta(item, (ItemMeta meta) -> {
						// Pearled player's name
						meta.displayName(ChatUtils.newComponent(pearl.getPlayerName())
								.color(NamedTextColor.AQUA)
								.append(isPlayerBanned ?
										Component.text(" <banned>")
												.color(NamedTextColor.RED) :
										Component.empty()));

						meta.lore(List.of(
								// Pearl type
								ChatUtils.newComponent(pearl.getItemName())
										.color(NamedTextColor.GREEN),
								// Pearled player's name and hash
								ChatUtils.newComponent("Player: ")
										.color(NamedTextColor.GOLD)
										.append(Component.text(pearl.getPlayerName())
												.color(NamedTextColor.GRAY))
										.append(Component.space())
										.append(Component.text(Integer.toString(pearl.getPearlId(), 36).toUpperCase())
												.color(NamedTextColor.DARK_GRAY)),
								// Pearled Date
								ChatUtils.newComponent("Pearled: ")
										.color(NamedTextColor.GOLD)
										.append(Component.text(DATE_FORMAT.format(pearl.getPearledOn()))
												.color(NamedTextColor.GRAY)),
								// Killer's name
								ChatUtils.newComponent("Killed by: ")
										.color(NamedTextColor.GOLD)
										.append(Component.text(pearl.getKillerName())
												.color(NamedTextColor.GRAY))));

						if (showLocation) {
							MetaUtils.addComponentLore(meta,
									// Pearl location
									ChatUtils.newComponent("Location: ")
											.color(NamedTextColor.GOLD)
											.append(Component.text(pearlLocation.getWorld().getName())
													.color(NamedTextColor.WHITE))
											.append(Component.space())
											.append(Component.text(pearlLocation.getBlockX())
													.color(NamedTextColor.RED))
											.append(Component.space())
											.append(Component.text(pearlLocation.getBlockY())
													.color(NamedTextColor.GREEN))
											.append(Component.space())
											.append(Component.text(pearlLocation.getBlockZ())
													.color(NamedTextColor.BLUE)),
									// Waypoint
									Component.space(),
									ChatUtils.newComponent("Click to receive a waypoint")
											.color(NamedTextColor.GREEN));
						}
						return true;
					});
					return new Clickable(item) {
						@Override
						protected void clicked(final Player clicker) {
							if (showLocation) {
								final var location = pearl.getLocation();
								if (!Objects.equals(location.getWorld(), clicker.getWorld())) {
									clicker.sendMessage(ChatColor.RED + "That pearl is in a different world!");
									return;
								}
								clicker.sendMessage("["
										+ "name:" + pearl.getPlayerName() + "'s pearl,"
										+ "x:" + location.getBlockX() + ","
										+ "y:" + location.getBlockY() + ","
										+ "z:" + location.getBlockZ()
										+ "]");
							}
						}
					};
				})
				.collect(Collectors.toCollection(ArrayList::new));

		if (contents.isEmpty()) {
			final var item = new ItemStack(Material.BARRIER);
			ItemUtils.setComponentDisplayName(item,
					ChatUtils.newComponent("There are currently no pearls.")
							.color(NamedTextColor.RED));
			contents.add(new DecorationStack(item));
		}

		new MultiPageView(player, contents, "All Pearls", true).showScreen();
	}

	private boolean isPlayerBanned(final UUID player) {
		if (!this.isBanStickEnabled) {
			return false;
		}
		return BanHandler.isPlayerBanned(player);
	}

	public static ItemStack getSkullForPlayer(final UUID player) {
		final var item = new ItemStack(Material.PLAYER_HEAD);
		ItemUtils.handleItemMeta(item, (SkullMeta meta) -> {
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(player));
			return true;
		});
		return item;
	}

}
