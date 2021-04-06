package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;

public class CmdShowAllPearls extends PearlCommand {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");

	public CmdShowAllPearls(final ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("showall");
		this.setHelpShort("Lists all pearled players.");

		this.senderMustBePlayer = true;
	}

	@Override
	public void perform() {
		final List<IClickable> contents = this.plugin.getPearls().stream()
				.sorted(Comparator.comparing(ExilePearl::getPearledOn))
				.sorted(Collections.reverseOrder())
				.map(this::generatePearlClickable)
				.collect(Collectors.toCollection(ArrayList::new));

		if (contents.isEmpty()) {
			final var item = new ItemStack(Material.BARRIER);
			ItemUtils.setDisplayName(item, ChatColor.RED + "There are currently no pearls.");
			contents.add(new DecorationStack(item));
		}

		new MultiPageView(player(), contents, "All Snitches", true).showScreen();
	}

	private IClickable generatePearlClickable(final ExilePearl pearl) {
		final Location pearlLocation = pearl.getLocation();

		final var item = new ItemStack(Material.PLAYER_HEAD);
		ItemUtils.handleItemMeta(item, (SkullMeta meta) -> {
			// Pearled player's head
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(pearl.getPlayerId()));
			// Pearled player's name
			meta.setDisplayName(ChatColor.AQUA + pearl.getPlayerName());
			meta.setLore(Arrays.asList(
					// Pearl type
					ChatColor.GREEN + pearl.getItemName(),
					// Pearled player's name and hash
					ChatColor.GOLD + "Player: " + ChatColor.GRAY + pearl.getPlayerName() + " " +
							ChatColor.DARK_GRAY + Integer.toString(pearl.getPearlId(), 36).toUpperCase(),
					// Pearled Date
					ChatColor.GOLD + "Pearled: " + ChatColor.GRAY + DATE_FORMAT.format(pearl.getPearledOn()),
					// Killer's name
					ChatColor.GOLD + "Killed by: " + ChatColor.GRAY + pearl.getKillerName(),
					// Pearl location
					ChatColor.GOLD + "Kept at: " +
							ChatColor.RED + pearlLocation.getBlockX() + " " +
							ChatColor.GREEN + pearlLocation.getBlockY() + " " +
							ChatColor.BLUE + pearlLocation.getBlockZ()
			));
			return true;
		});

		return new DecorationStack(item);
	}

}
