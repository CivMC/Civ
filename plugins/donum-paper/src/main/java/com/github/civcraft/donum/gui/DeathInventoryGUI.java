package com.github.civcraft.donum.gui;

import com.github.civcraft.donum.Donum;
import com.github.civcraft.donum.inventories.DeathInventory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.namelayer.NameAPI;

public class DeathInventoryGUI {
	private UUID viewer;
	private int currentPage;
	private List<DeathInventory> inventories;
	private DateFormat dateFormat;

	public DeathInventoryGUI(UUID viewer, List<DeathInventory> inventories) {
		this.inventories = inventories;
		this.viewer = viewer;
		this.currentPage = 0;
		this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}

	public void showScreen() {
		Player p = Bukkit.getPlayer(viewer);
		if (p == null) {
			return;
		}
		ClickableInventory.forceCloseInventory(p);
		ClickableInventory ci = new ClickableInventory(54, "DeathInventory " + NameAPI.getCurrentName(viewer));
		if (inventories.size() < 45 * currentPage) {
			// would show an empty page, so go to previous
			currentPage--;
			showScreen();
		}
		for (int i = 45 * currentPage; i < 45 * (currentPage + 1) && i < inventories.size(); i++) {
			ci.setSlot(createInventoryClickable(inventories.get(i)), i - (45 * currentPage));
		}
		// previous button
		if (currentPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(back, ChatColor.GOLD + "Go to previous page");
			Clickable baCl = new Clickable(back) {

				@Override
				public void clicked(Player arg0) {
					if (currentPage > 0) {
						currentPage--;
					}
					showScreen();
				}
			};
			ci.setSlot(baCl, 45);
		}
		// next button
		if ((45 * (currentPage + 1)) <= inventories.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (currentPage + 1)) <= inventories.size()) {
						currentPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, 53);
		}
		// exit button
		ItemStack backToOverview = new ItemStack(Material.OAK_DOOR);
		ItemUtils.setDisplayName(backToOverview, ChatColor.GOLD + "Close");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				// just let it close, dont do anything
			}
		}, 49);

		ci.showInventory(p);
	}

	private Clickable createInventoryClickable(final DeathInventory i) {
		ItemStack is = new ItemStack(Material.BOOK);
		ItemUtils.addLore(is, ChatColor.GREEN + "Owner: " + NameAPI.getCurrentName(i.getOwner()));
		ItemUtils.addLore(is, ChatColor.GOLD + "Died at " + dateFormat.format(i.getDeathTime()));
		ItemUtils.addLore(is, ChatColor.LIGHT_PURPLE + "Unique ID: " + i.getID());
		if (i.wasReturned()) {
			ItemUtils.addLore(is, ChatColor.RED + "This inventory was already returned");
		} else {
			ItemUtils.addLore(is, ChatColor.AQUA + "This inventory was not returned yet");
		}

		return new Clickable(is) {

			@Override
			public void clicked(Player p) {
				ClickableInventory ci = new ClickableInventory(54, dateFormat.format(i.getDeathTime()));
				for (ItemStack is : i.getInventory().getItemStackRepresentation()) {
					ci.addSlot(new DecorationStack(is));
				}
				// back button
				ItemStack backToOverview = new ItemStack(Material.OAK_DOOR);
				ItemUtils.setDisplayName(backToOverview, ChatColor.GOLD + "Go back to overview");
				ci.setSlot(new Clickable(backToOverview) {

					@Override
					public void clicked(Player arg0) {
						showScreen();
					}
				}, 49);
				ItemStack returnStack = new ItemStack(Material.ANVIL);
				ItemUtils.setDisplayName(returnStack, ChatColor.GOLD + "Return items");
				ItemUtils.addLore(
						returnStack,
						ChatColor.GOLD
								+ "This inventory was already returned previously. You may return it again, if you know what you are doing");
				ItemUtils.addLore(returnStack, ChatColor.RED + "You can not undo this!");
				ci.setSlot(new Clickable(returnStack) {

					@Override
					public void clicked(Player p) {
						Donum.getManager().returnDeathInventory(i);
						Donum.getInstance().info(p.getName() + " returned inventory " + i.getID());
						p.sendMessage(ChatColor.GREEN + "Successfully returned inventory with id " + i.getID()
								+ " with a total of " + i.getInventory().getTotalItemAmount() + " items");
						showScreen();
					}
				}, 53);
				ci.showInventory(p);
			}
		};
	}
}
