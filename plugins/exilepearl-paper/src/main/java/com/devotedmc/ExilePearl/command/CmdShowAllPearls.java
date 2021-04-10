package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.collections4.ComparatorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
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

		final Location playerLocation = player.getLocation();
		final int pearlExclusionRadius = this.plugin.getPearlConfig().getRulePearlRadius();

		final List<IClickable> contents = this.plugin.getPearls().stream()
				.sorted(ComparatorUtils.reversedComparator(Comparator.comparing(ExilePearl::getPearledOn)))
				.map(pearl -> {
					final Location pearlLocation = pearl.getLocation();
					final boolean showLocation = WorldUtils.blockDistance(
							playerLocation, pearlLocation, true) <= pearlExclusionRadius;

					final var item = new ItemStack(Material.PLAYER_HEAD);
					ItemUtils.handleItemMeta(item, (SkullMeta skull) -> {
						// Pearled player's head
						skull.setOwningPlayer(Bukkit.getOfflinePlayer(pearl.getPlayerId()));

						// Pearled player's name
						skull.setDisplayName(ChatColor.AQUA + pearl.getPlayerName());

						skull.setLore(Arrays.asList(
								// Pearl type
								ChatColor.GREEN + pearl.getItemName(),
								// Pearled player's name and hash
								ChatColor.GOLD + "Player: " + ChatColor.GRAY + pearl.getPlayerName() + " " +
										ChatColor.DARK_GRAY + Integer.toString(pearl.getPearlId(), 36).toUpperCase(),
								// Pearled Date
								ChatColor.GOLD + "Pearled: " + ChatColor.GRAY + DATE_FORMAT.format(pearl.getPearledOn()),
								// Killer's name
								ChatColor.GOLD + "Killed by: " + ChatColor.GRAY + pearl.getKillerName()
						));

						if (showLocation) {
							MetaUtils.addLore(skull,
									// Pearl location
									ChatColor.GOLD + "Location: " +
											ChatColor.WHITE + pearlLocation.getWorld().getName() + " " +
											ChatColor.RED + pearlLocation.getBlockX() + " " +
											ChatColor.GREEN + pearlLocation.getBlockY() + " " +
											ChatColor.BLUE + pearlLocation.getBlockZ(),
									// Waypoint indicator
									"", ChatColor.GREEN + "Click to receive a waypoint");
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
			ItemUtils.setDisplayName(item, ChatColor.RED + "There are currently no pearls.");
			contents.add(new DecorationStack(item));
		}

		new MultiPageView(player, contents, "All Pearls", true).showScreen();
	}

}
