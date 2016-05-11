package vg.civcraft.mc.namelayer.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class InvitationViewGUI extends GroupGUI {

	private int currentPage;
	private NameLayerGUI parentGUI;
	private boolean showNotAllowedToRevoke;

	public InvitationViewGUI(Player p, Group g, NameLayerGUI parentGUI) {
		super(g, p);
		this.parentGUI = parentGUI;
		currentPage = 0;
		showNotAllowedToRevoke = true;
		showScreen();
	}

	private void showScreen() {
		ClickableInventory.forceCloseInventory(p);
		if (!validGroup()) {
			return;
		}
		Map<UUID, PlayerType> invites = NameLayerPlugin.getGroupManagerDao()
				.getInvitesForGroup(g.getName());
		ClickableInventory ci = new ClickableInventory(54, g.getName());
		final List<Clickable> menuContent = getInviteClickables(invites);
		if (menuContent.size() < 45 * currentPage) {
			currentPage--;
			showScreen();
		}
		for (int i = 45 * currentPage; i < 45 * (currentPage + 1)
				&& i < menuContent.size(); i++) {
			ci.setSlot(menuContent.get(i), i);
		}

		// toggle showing invites the player cant revoke
		ItemStack toggler = MenuUtils.toggleButton(showNotAllowedToRevoke,
				"Show unrevokable invites", true);
		ci.setSlot(new Clickable(toggler) {

			@Override
			public void clicked(Player arg0) {
				showNotAllowedToRevoke = !showNotAllowedToRevoke;
				currentPage = 0;
				showScreen();
			}
		}, 47);

		// back button
		if (currentPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ISUtils.setName(back, ChatColor.GOLD + "Go to previous page");
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
		if ((45 * (currentPage + 1)) <= menuContent.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ISUtils.setName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (currentPage + 1)) <= menuContent.size()) {
						currentPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, 53);
		}

		// to previous gui
		ItemStack backToOverview = new ItemStack(Material.WOOD_DOOR);
		ISUtils.setName(backToOverview, ChatColor.GOLD
				+ "Go back to group member management overview");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				parentGUI.showMemberManageScreen();
			}
		}, 49);
		ci.showInventory(p);
	}

	private List<Clickable> getInviteClickables(Map<UUID, PlayerType> invites) {
		List<Clickable> clicks = new ArrayList<Clickable>();
		for (Entry<UUID, PlayerType> entry : invites.entrySet()) {
			ItemStack is = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
			final String playerName = NameAPI.getCurrentName(entry.getKey());
			ISUtils.setName(is, ChatColor.GOLD + playerName);
			boolean canRevoke = false;
			switch (entry.getValue()) {
			case MEMBERS:
				ISUtils.addLore(is, ChatColor.AQUA + "Invited as: Member");
				if (gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("MEMBERS"))) {
					canRevoke = true;
				}
				break;
			case MODS:
				ISUtils.addLore(is, ChatColor.AQUA + "Invited as: Mod");
				if (gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("MODS"))) {
					canRevoke = true;
				}
				break;
			case ADMINS:
				ISUtils.addLore(is, ChatColor.AQUA + "Invited as: Admin");
				if (gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("ADMINS"))) {
					canRevoke = true;
				}
				break;
			case OWNER:
				ISUtils.addLore(is, ChatColor.AQUA + "Invited as: Owner");
				if (gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("OWNER"))) {
					canRevoke = true;
				}
				break;
			default:
				continue;
			}
			Clickable c = null;
			if (canRevoke) {
				ISUtils.addLore(is, ChatColor.GREEN
						+ "Click to revoke this invite");
				c = new Clickable(is) {

					@Override
					public void clicked(Player arg0) {
						UUID invitedUUID = NameAPI.getUUID(playerName);
						PlayerType pType = g.getInvite(invitedUUID);
						if (pType == null) {
							p.sendMessage(ChatColor.RED
									+ "Failed to revoke invite for "
									+ playerName
									+ ". This player isn't invited currently.");
							showScreen();
						}
						// make sure the player still has permission to do this
						boolean allowed = false;
						switch (pType) {
						case MEMBERS:
							allowed = gm.hasAccess(g, p.getUniqueId(),
									PermissionType.getPermission("MEMBERS"));
							break;
						case MODS:
							allowed = gm.hasAccess(g, p.getUniqueId(),
									PermissionType.getPermission("MODS"));
							break;
						case ADMINS:
							allowed = gm.hasAccess(g, p.getUniqueId(),
									PermissionType.getPermission("ADMINS"));
							break;
						case OWNER:
							allowed = gm.hasAccess(g, p.getUniqueId(),
									PermissionType.getPermission("OWNER"));
							break;
						default:
							allowed = false;
							break;
						}
						if (!allowed) {
							p.sendMessage(ChatColor.RED
									+ "You don't have permission to revoke this invite");
							showScreen();
						} else {
							g.removeInvite(invitedUUID, true);
							PlayerListener.removeNotification(invitedUUID, g);

							if (NameLayerPlugin.isMercuryEnabled()) {
								MercuryAPI.sendGlobalMessage(
										"removeInvitation " + g.getGroupId()
												+ " " + invitedUUID,
										"namelayer");
							}
							p.sendMessage(ChatColor.GREEN + playerName
									+ "'s invitation has been revoked.");
						}
					}
				};
			} else {
				if (showNotAllowedToRevoke) {
					ISUtils.addLore(is, ChatColor.RED
							+ "You don't have permission to revoke this invite");
					c = new DecorationStack(is);
				}
			}
			if (c != null) {
				clicks.add(c);
			}
		}
		return clicks;
	}

}
