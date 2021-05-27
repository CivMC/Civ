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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.list.LazyList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;
import vg.civcraft.mc.civmodcore.util.MoreCollectionUtils;
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
		final Player sender = player();

		final long now = System.currentTimeMillis();
		final long previousUseTime = COOLDOWNS.compute(sender.getUniqueId(),
				(uuid, value) -> value == null ? 0L : value); // This is better than getOrDefault()
		if (previousUseTime > (now - COOLDOWN)) {
			sender.sendMessage(ChatColor.RED + "You can't do that yet.");
			return;
		}
		COOLDOWNS.put(sender.getUniqueId(), now);

		final Location senderLocation = sender.getLocation();
		final double pearlExclusionRadius = this.plugin.getPearlConfig().getRulePearlRadius() * 1.2;
		final boolean isBanStickEnabled = this.plugin.isBanStickEnabled();

		final List<Supplier<IClickable>> contentSuppliers = this.plugin.getPearls().stream()
				// Sort pearls from newest to oldest
				.sorted(ComparatorUtils.reversedComparator(Comparator.comparing(ExilePearl::getPearledOn)))
				.<Supplier<IClickable>>map((pearl) -> () -> {
					final Location pearlLocation = pearl.getLocation();
					final boolean isPlayerBanned = isBanStickEnabled
							&& BanHandler.isPlayerBanned(pearl.getPlayerId());
					final boolean showLocation = WorldUtils.blockDistance(
							senderLocation, pearlLocation, true) <= pearlExclusionRadius;

					CompletableFuture<ItemStack> itemReadyFuture = new CompletableFuture<>();
					final ItemStack item = isPlayerBanned
							? new ItemStack(Material.ARMOR_STAND)
							: CivModCorePlugin.getInstance().getSkinCache().getHeadItem(
									pearl.getPlayerId(),
									() -> new ItemStack(Material.ENDER_PEARL),
									itemReadyFuture::complete);

					Consumer<ItemStack> itemMetaMod = itemToMod ->
							ItemUtils.handleItemMeta(itemToMod, (ItemMeta meta) -> {
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

					itemMetaMod.accept(item);

					return new Clickable(item) {
						@Override
						protected void clicked(final Player clicker) {
							if (showLocation) {
								final var location = pearl.getLocation();
								if (!Objects.equals(location.getWorld(), clicker.getWorld())) {
									clicker.sendMessage(ChatColor.RED + "That pearl is in a different world!");
									return;
								}
								clicker.sendMessage('['
										+ "name:" + pearl.getPlayerName() + "'s pearl,"
										+ "x:" + location.getBlockX() + ','
										+ "y:" + location.getBlockY() + ','
										+ "z:" + location.getBlockZ()
										+ ']');
							}
						}

						@Override
						public void addedToInventory(ClickableInventory inv, int slot) {
							itemReadyFuture.thenAccept(item -> {
								itemMetaMod.accept(item);
								this.item = item;
								inv.setItem(item, slot);
							});
							super.addedToInventory(inv, slot);
						}
					};
				})
				.collect(Collectors.toCollection(ArrayList::new));

		if (contentSuppliers.isEmpty()) {
			final var item = new ItemStack(Material.BARRIER);
			ItemUtils.setComponentDisplayName(item,
					ChatUtils.newComponent("There are currently no pearls.")
							.color(NamedTextColor.RED));
			contentSuppliers.add(() -> new DecorationStack(item));
		}

		LazyList<IClickable> lazyContents = MoreCollectionUtils.lazyList(contentSuppliers);
		new MultiPageView(sender, lazyContents, "All Pearls", true).showScreen();
	}

}
