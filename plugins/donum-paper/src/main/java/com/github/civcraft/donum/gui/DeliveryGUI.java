package com.github.civcraft.donum.gui;

import com.github.civcraft.donum.Donum;
import com.github.civcraft.donum.inventories.DeliveryInventory;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class DeliveryGUI {

	private UUID viewer;
	private int currentPage;
	private DeliveryInventory inventory;

	public DeliveryGUI(UUID viewer, DeliveryInventory inventory) {
		this.inventory = inventory;
		this.viewer = viewer;
		this.currentPage = 0;
	}

	public void showScreen() {
		Player p = Bukkit.getPlayer(viewer);
		if (p == null) {
			return;
		}
		ClickableInventory.forceCloseInventory(p);
		ClickableInventory ci = new ClickableInventory(54, "Delivery inventory");
		List<ItemStack> stacks = inventory.getInventory().getItemStackRepresentation();
		if (stacks.size() < 45 * currentPage) {
			// would show an empty page, so go to previous
			currentPage--;
			showScreen();
		}
		if (stacks.size() == 0) {
			//item to indicate that there is nothing to claim
			ItemStack noClaim = new ItemStack(Material.BARRIER);
			ItemUtils.setDisplayName(noClaim, ChatColor.GOLD + "No items available");
			ItemUtils.addLore(noClaim, ChatColor.RED + "You currently have no items you could claim");
			ci.setSlot(new DecorationStack(noClaim), 4);
		} else {
			for (int i = 45 * currentPage; i < 45 * (currentPage + 1) && i < stacks.size(); i++) {
				ci.setSlot(createRemoveItemClickable(stacks.get(i)), i - (45 * currentPage));
			}
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
		if ((45 * (currentPage + 1)) <= stacks.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (currentPage + 1)) <= stacks.size()) {
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

		// complain button
		ItemStack gibStuffBack = new ItemStack(Material.OAK_SIGN);
		ItemUtils.setDisplayName(gibStuffBack, ChatColor.GOLD + "Request item return");
		ItemUtils.addLore(gibStuffBack, ChatColor.AQUA + "If you think you lost items due to a glitch", ChatColor.AQUA
				+ "you can send us a message", ChatColor.AQUA + "to get your items back", ChatColor.GREEN
				+ "Click here to do so");
		Clickable openComplaintForm = new Clickable(gibStuffBack) {

			@Override
			public void clicked(Player p) {
				TextComponent link = new TextComponent(
						"Click this message to write the admins a message about lost items. This will open a new tab in your default browser!");
				link.setColor(ChatColor.GREEN);
				link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Donum.getConfiguration()
						.getComplaintURL()));
				link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Open link")
						.create()));
				p.spigot().sendMessage(link);
			}
		};
		ci.setSlot(openComplaintForm, 47);

		// claim all button
		ItemStack gibAll = new ItemStack(Material.BLAZE_POWDER);
		ItemUtils.setDisplayName(gibAll, ChatColor.GOLD + "Claim all items");
		ItemUtils.addLore(gibAll, ChatColor.AQUA
				+ "Click to automatically claim items until your inventory is full or your delivery inventory is empty");
		Clickable autoClaimClick = new Clickable(gibAll) {

			@Override
			public void clicked(Player p) {
				PlayerInventory pInv = p.getInventory();
				for (ItemStack current : inventory.getInventory().getItemStackRepresentation()) {
					if (new ItemMap(current).fitsIn(pInv)) {
						Donum.getInstance().debug(
								p.getName() + " got " + current.toString() + " auto delivered from delivery inventory");
						inventory.getInventory().removeItemStack(current);
						pInv.addItem(current);
						inventory.setDirty(true);
					} else {
						p.sendMessage(ChatColor.RED + "Your inventory its full!");
						break;
					}
				}
				p.updateInventory();
				if (inventory.getInventory().getTotalItemAmount() == 0) {
					p.sendMessage(ChatColor.GREEN + "Successfully claimed all items");
				} else {
					showScreen();
				}
			}

		};
		ci.setSlot(autoClaimClick, 51);

		ci.showInventory(p);
	}

	private Clickable createRemoveItemClickable(final ItemStack is) {
		return new Clickable(is) {

			@Override
			public void clicked(Player p) {
				PlayerInventory pInv = p.getInventory();
				if (new ItemMap(is).fitsIn(pInv)) {
					Donum.getInstance().debug(p.getName() + " took " + is.toString() + " from delivery inventory");
					inventory.getInventory().removeItemStack(is);
					pInv.addItem(is);
					inventory.setDirty(true);
				} else {
					p.sendMessage(ChatColor.RED + "There is not enough space in your inventory");
				}
				p.updateInventory();
				showScreen();
			}
		};
	}

}
