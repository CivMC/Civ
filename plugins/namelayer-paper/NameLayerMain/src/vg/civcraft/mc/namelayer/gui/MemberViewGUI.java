package vg.civcraft.mc.namelayer.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.events.PromotePlayerEvent;
import vg.civcraft.mc.namelayer.group.BlackList;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class MemberViewGUI extends GroupGUI {

	private boolean showBlacklist;
	private boolean showInvites;
	private boolean showMembers;
	private boolean showMods;
	private boolean showAdmins;
	private boolean showOwners;

	private int currentPage;
	private boolean[] savedToggleState;

	public MemberViewGUI(Player p, Group g) {
		super(g, p);
		showMembers = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("MEMBERS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
		showMods = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("MODS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
		showAdmins = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("ADMINS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
		showOwners = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("OWNER"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
		currentPage = 0;
		showScreen();
	}

	public void showScreen() {
		ClickableInventory.forceCloseInventory(p);
		if (!validGroup()) {
			return;
		}
		if (!hasPermissionToViewAnything()) {
			p.sendMessage(ChatColor.RED
					+ "You lost the required permissions to list members of this group");
			return;
		}
		ClickableInventory ci = new ClickableInventory(54, g.getName());
		final List<Clickable> clicks = constructClickables();
		if (clicks.size() < 45 * currentPage) {
			// would show an empty page, so go to previous
			currentPage--;
			showScreen();
		}
		// fill gui
		for (int i = 36 * currentPage; i < 36 * (currentPage + 1)
				&& i < clicks.size(); i++) {
			ci.setSlot(clicks.get(i), 9 + i - (36 * currentPage));
		}
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
		if ((45 * (currentPage + 1)) <= clicks.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ISUtils.setName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (currentPage + 1)) <= clicks.size()) {
						currentPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, 53);
		}

		// options

		ci.setSlot(createBlacklistToggle(), 46);
		ci.setSlot(createInviteToggle(), 47);
		ci.setSlot(setupMemberTypeToggle(PlayerType.MEMBERS, showMembers), 48);

		ci.setSlot(setupMemberTypeToggle(PlayerType.MODS, showMods), 50);

		ci.setSlot(setupMemberTypeToggle(PlayerType.ADMINS, showAdmins), 51);

		ci.setSlot(setupMemberTypeToggle(PlayerType.OWNER, showOwners), 52);

		//exit button
		ItemStack backToOverview = new ItemStack(Material.WOOD_DOOR);
		ISUtils.setName(backToOverview, ChatColor.GOLD + "Close");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				// just let it close, dont do anything
			}
		}, 49);
		
		
		//options at the top
		
		ItemStack permStack = new ItemStack(Material.FENCE_GATE);
		ISUtils.setName(permStack, ChatColor.GOLD + "View and manage group permissions");
		Clickable permClickable;
		if (gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("LIST_PERMS"))) {
			permClickable = new Clickable(permStack) {
				@Override
				public void clicked(Player arg0) {
					PermissionManageGUI pmgui = new PermissionManageGUI(g, p, MemberViewGUI.this);
					pmgui.showScreen();
				}
			};
		}
		else {
			ISUtils.addLore(permStack, ChatColor.RED + "You don't have permission", ChatColor.RED + "to do this");
			permClickable = new DecorationStack(permStack);
		}
		ci.setSlot(permClickable, 4);
		
		ItemStack inviteStack = new ItemStack(Material.COOKIE);
		ISUtils.setName(inviteStack, ChatColor.GOLD + "Invite new member");
		ci.setSlot(new Clickable(inviteStack) {
			
			@Override
			public void clicked(Player arg0) {
				new InvitationGUI(g, p);				
			}
		}, 0);
		ci.showInventory(p);
	}

	private List<Clickable> constructClickables() {
		List<Clickable> clicks = new ArrayList<Clickable>();
		if (showBlacklist) {
			final BlackList black = NameLayerPlugin.getBlackList();
			for (final UUID uuid : black.getBlacklist(g)) {
				ItemStack is = new ItemStack(Material.LEATHER_CHESTPLATE);
				LeatherArmorMeta meta = (LeatherArmorMeta) is.getItemMeta();
				meta.setColor(Color.BLACK);
				ISUtils.setName(is, NameAPI.getCurrentName(uuid));
				Clickable c;
				if (gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("BLACKLIST"))) {
					ISUtils.addLore(is, ChatColor.GREEN + "Click to remove "
							+ NameAPI.getCurrentName(uuid), ChatColor.GREEN
							+ "from the blacklist");
					c = new Clickable(is) {

						@Override
						public void clicked(Player arg0) {
							if (gm.hasAccess(g, p.getUniqueId(),
									PermissionType.getPermission("BLACKLIST"))) {
								black.removeBlacklistMember(g, uuid, true);
								checkRecacheGroup();
								p.sendMessage(ChatColor.GREEN + "You removed "
										+ NameAPI.getCurrentName(uuid)
										+ " from the blacklist");
							} else {
								p.sendMessage(ChatColor.RED
										+ "You lost permission to remove this player from the blacklist");
							}
						}
					};
				} else {
					ISUtils.addLore(is, ChatColor.RED
							+ "You dont have permission to remove",
							ChatColor.RED + NameAPI.getCurrentName(uuid)
									+ "from the blacklist");
					c = new DecorationStack(is);
				}
				clicks.add(c);
			}

		}
		if (showInvites) {
			Map<UUID, PlayerType> invites = NameLayerPlugin
					.getGroupManagerDao().getInvitesForGroup(g.getName());
			for (Entry<UUID, PlayerType> entry : invites.entrySet()) {
				ItemStack is = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
				ItemMeta im = is.getItemMeta();
				im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				is.setItemMeta(im);
				final String playerName = NameAPI
						.getCurrentName(entry.getKey());
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
							// make sure the player still has permission to do
							// this
							boolean allowed = false;
							switch (pType) {
							case MEMBERS:
								allowed = gm
										.hasAccess(
												g,
												p.getUniqueId(),
												PermissionType
														.getPermission("MEMBERS"));
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
								PlayerListener.removeNotification(invitedUUID,
										g);

								if (NameLayerPlugin.isMercuryEnabled()) {
									MercuryAPI.sendGlobalMessage(
											"removeInvitation "
													+ g.getGroupId() + " "
													+ invitedUUID, "namelayer");
								}
								p.sendMessage(ChatColor.GREEN + playerName
										+ "'s invitation has been revoked.");
							}
						}
					};
				} else {
					ISUtils.addLore(is, ChatColor.RED
							+ "You don't have permission to revoke this invite");
					c = new DecorationStack(is);
				}
				if (c != null) {
					clicks.add(c);
				}
			}
		}
		for (UUID uuid : g.getAllMembers()) {
			Clickable c = null;
			switch (g.getPlayerType(uuid)) {
			case MEMBERS:
				if (showMembers) {
					c = constructMemberClickable(Material.LEATHER_CHESTPLATE,
							uuid, PlayerType.MEMBERS);
				}
				break;
			case MODS:
				if (showMods) {
					c = constructMemberClickable(Material.GOLD_CHESTPLATE,
							uuid, PlayerType.MODS);
				}
				break;
			case ADMINS:
				if (showAdmins) {
					c = constructMemberClickable(Material.IRON_CHESTPLATE,
							uuid, PlayerType.ADMINS);
				}
				break;
			case OWNER:
				if (showOwners) {
					c = constructMemberClickable(Material.DIAMOND_CHESTPLATE,
							uuid, PlayerType.OWNER);
				}
				break;
			default:
				// should never happen
			}
			if (c != null) {
				clicks.add(c);
			}
		}

		return clicks;
	}

	private Clickable setupMemberTypeToggle(final PlayerType pType,
			boolean initialState) {
		boolean canEdit = gm.hasAccess(g, p.getUniqueId(),
				getAccordingPermission(pType))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
		ItemStack is = MenuUtils.toggleButton(initialState, ChatColor.GOLD
				+ "Show " + getDirectRankName(pType) + "s", canEdit);
		Clickable c;
		if (canEdit) {
			c = new Clickable(is) {

				@Override
				public void clicked(Player arg0) {
					switch (pType) {
					case MEMBERS:
						showMembers = !showMembers;
						break;
					case MODS:
						showMods = !showMods;
						break;
					case ADMINS:
						showAdmins = !showAdmins;
						break;
					case OWNER:
						showOwners = !showOwners;
						break;
					}
					currentPage = 0;
					showScreen();
				}
			};
		} else {
			c = new DecorationStack(is);
		}

		return c;

	}

	private Clickable constructMemberClickable(Material material,
			final UUID toDisplay, PlayerType rank) {
		Clickable c;
		ItemStack is = new ItemStack(material);
		ItemMeta im = is.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		is.setItemMeta(im);
		ISUtils.setName(is, ChatColor.GOLD + NameAPI.getCurrentName(toDisplay));
		if (g.isOwner(toDisplay)) { // special case for primary owner
			ISUtils.addLore(is, ChatColor.AQUA + "Rank: Primary Owner");
			ISUtils.addLore(is, ChatColor.RED + "You don't have permission",
					ChatColor.RED + "to modify the rank of this player");
			c = new DecorationStack(is);

		} else {
			ISUtils.addLore(is, ChatColor.AQUA + "Rank: "
					+ getRankName(toDisplay));
			if (gm.hasAccess(g, p.getUniqueId(), getAccordingPermission(rank))) {
				ISUtils.addLore(is, ChatColor.GREEN
						+ "Click to modify this player's", ChatColor.GREEN
						+ "rank or to remove them");
				c = new Clickable(is) {

					@Override
					public void clicked(Player arg0) {
						showDetail(toDisplay);
					}
				};
			} else {
				ISUtils.addLore(is,
						ChatColor.RED + "You don't have permission",
						ChatColor.RED + "to modify the rank of this player");
				c = new DecorationStack(is);
			}
		}
		return c;
	}

	public void showDetail(final UUID uuid) {
		if (!validGroup()) {
			showScreen();
			return;
		}
		ClickableInventory.forceCloseInventory(p);
		ClickableInventory ci = new ClickableInventory(27, g.getName());
		String playerName = NameAPI.getCurrentName(uuid);

		ItemStack info = new ItemStack(Material.PAPER);
		ISUtils.setName(info, ChatColor.GOLD + playerName);
		String rankName = getRankName(uuid);
		ISUtils.addLore(info, ChatColor.GOLD + "Current rank: " + rankName);
		ci.setSlot(new DecorationStack(info), 4);

		PlayerType rank = g.getCurrentRank(uuid);

		Clickable memberClick = setupDetailSlot(Material.LEATHER_CHESTPLATE,
				uuid, PlayerType.MEMBERS);
		ci.setSlot(memberClick, 10);
		Clickable modClick = setupDetailSlot(Material.GOLD_CHESTPLATE, uuid,
				PlayerType.MODS);
		ci.setSlot(modClick, 12);
		Clickable adminClick = setupDetailSlot(Material.IRON_CHESTPLATE, uuid,
				PlayerType.ADMINS);
		ci.setSlot(adminClick, 14);
		Clickable ownerClick = setupDetailSlot(Material.DIAMOND_CHESTPLATE,
				uuid, PlayerType.OWNER);
		ci.setSlot(ownerClick, 16);
		ci.showInventory(p);
	}

	private Clickable setupDetailSlot(Material slotMaterial,
			final UUID toChange, final PlayerType pType) {
		PlayerType rank = g.getCurrentRank(toChange);
		ItemStack mod = new ItemStack(slotMaterial);
		Clickable modClick;
		if (rank == pType) {
			ISUtils.setName(mod, ChatColor.GOLD + "Remove this player");
			if (!gm.hasAccess(g, p.getUniqueId(), getAccordingPermission(pType))) {
				ISUtils.addLore(mod, ChatColor.RED
						+ "You dont have permission to do this");
				modClick = new DecorationStack(mod);
			} else {
				modClick = new Clickable(mod) {

					@Override
					public void clicked(Player arg0) {
						if (gm.hasAccess(g, p.getUniqueId(),
								getAccordingPermission(g
										.getCurrentRank(toChange)))) {
							removeMember(toChange);
							showScreen();
						}
					}
				};
			}
		} else {
			ISUtils.setName(
					mod,
					ChatColor.GOLD
							+ demoteOrPromote(g.getPlayerType(toChange), pType,
									true) + " this player to "
							+ getDirectRankName(pType));
			if (!gm.hasAccess(g, p.getUniqueId(), getAccordingPermission(pType))) {
				ISUtils.addLore(mod, ChatColor.RED
						+ "You dont have permission to do this");
				modClick = new DecorationStack(mod);
			} else {
				modClick = new Clickable(mod) {

					@Override
					public void clicked(Player arg0) {
						changePlayerRank(toChange, pType);
						showDetail(toChange);
					}
				};
			}
		}
		return modClick;
	}

	private void removeMember(UUID toRemove) {
		if (gm.hasAccess(g, p.getUniqueId(),
				getAccordingPermission(g.getCurrentRank(toRemove)))) {
			if (!g.isMember(toRemove)) {
				p.sendMessage(ChatColor.RED
						+ "This player is no longer on the group and can't be removed");
				return;
			}
			if (g.isOwner(toRemove)) {
				p.sendMessage(ChatColor.RED
						+ "This player owns the group and can't be removed");
			}
			g.removeMember(toRemove);
			checkRecacheGroup();
			p.sendMessage(ChatColor.GREEN + NameAPI.getCurrentName(toRemove)
					+ " has been removed from the group");
		} else {
			p.sendMessage(ChatColor.RED
					+ "You have lost permission to remove this player");
		}
	}

	private void changePlayerRank(UUID toChange, PlayerType newRank) {
		if (gm.hasAccess(g, p.getUniqueId(),
				getAccordingPermission(g.getCurrentRank(toChange)))) {
			if (!g.isMember(toChange)) {
				p.sendMessage(ChatColor.RED
						+ "This player is no longer on the group and can't be "
						+ demoteOrPromote(g.getCurrentRank(toChange), newRank,
								false) + "d");
				return;
			}
			if (g.isOwner(toChange)) {
				p.sendMessage(ChatColor.RED
						+ "This player owns the group and can't be demoted");
			}
			OfflinePlayer prom = Bukkit.getOfflinePlayer(toChange);
			if (prom.isOnline()) {
				Player oProm = (Player) prom;
				PromotePlayerEvent event = new PromotePlayerEvent(oProm, g,
						g.getCurrentRank(toChange), newRank);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					p.sendMessage(ChatColor.RED
							+ "Could not change player rank, you should complain about this");
					return;
				}
				g.removeMember(toChange);
				g.addMember(toChange, newRank);
				oProm.sendMessage(ChatColor.GREEN
						+ "You have been promoted to " + getRankName(toChange)
						+ " in (Group) " + g.getName());
			} else {
				// player is offline change their perms
				g.removeMember(toChange);
				g.addMember(toChange, newRank);
			}
			checkRecacheGroup();
			p.sendMessage(ChatColor.GREEN
					+ NameAPI.getCurrentName(toChange)
					+ " has been "
					+ demoteOrPromote(g.getCurrentRank(toChange), newRank,
							false) + "d to " + getRankName(toChange));
		} else {
			p.sendMessage(ChatColor.RED
					+ "You have lost permission to remove this player");
		}
	}

	private Clickable createBlacklistToggle() {
		ItemStack is = MenuUtils.toggleButton(
				showBlacklist,
				ChatColor.GOLD + "Show blacklisted players",
				gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS")));
		Clickable c;
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("GROUPSTATS"))) {
			c = new Clickable(is) {

				@Override
				public void clicked(Player arg0) {
					if (!showBlacklist) {
						// currently showing members, so save state
						savedToggleState = new boolean[5];
						savedToggleState[0] = showInvites;
						savedToggleState[1] = showMembers;
						savedToggleState[2] = showMods;
						savedToggleState[3] = showAdmins;
						savedToggleState[4] = showOwners;
						showInvites = false;
						showMembers = false;
						showMods = false;
						showAdmins = false;
						showOwners = false;
						showBlacklist = true;
					} else {
						// load state
						showInvites = savedToggleState[0];
						showMembers = savedToggleState[1];
						showMods = savedToggleState[2];
						showAdmins = savedToggleState[3];
						showOwners = savedToggleState[4];
						showBlacklist = false;
					}
					showScreen();
				}
			};
		} else {
			c = new DecorationStack(is);
		}
		return c;
	}

	public Clickable createInviteToggle() {
		ItemStack is = MenuUtils.toggleButton(showInvites, ChatColor.GOLD
				+ "Show invited players", true);
		return new Clickable(is) {

			@Override
			public void clicked(Player arg0) {
				showInvites = !showInvites;
				showScreen();
			}
		};
	}

	/**
	 * Utility to get a "nice" version of the rank the given player has in the
	 * group this gui is based on
	 * 
	 * @param uuid
	 *            Player whose rank name should be checked
	 * @return Rankname of the given player ready to be put into the gui or null
	 *         if the given UUID was null or the player didn't have an explicit
	 *         rank in the group
	 */
	private String getRankName(UUID uuid) {
		if (uuid == null) {
			return null;
		}
		PlayerType pType = g.getPlayerType(uuid);
		if (pType == null) {
			return null;
		}
		return getDirectRankName(pType);
	}

	public static String getDirectRankName(PlayerType pType) {
		String res = pType.toString().toLowerCase();
		char[] chars = res.toCharArray();
		chars[0] = new String(new char[] { chars[0] }).toUpperCase()
				.toCharArray()[0];
		res = new String(chars);
		if (res.endsWith("s")) {
			return res.substring(0, res.length() - 1);
		}
		return res;
	}

	/**
	 * Gets the permission needed to make changes to the given PlayerType
	 * 
	 * @param pt
	 *            PlayerType to get permission for
	 * @return Permission belonging to the given PlayerType or null if the
	 *         permission was NOT_BLACKLISTED or null
	 */
	public static PermissionType getAccordingPermission(PlayerType pt) {
		switch (pt) {
		case MEMBERS:
			return PermissionType.getPermission("MEMBERS");
		case MODS:
			return PermissionType.getPermission("MODS");
		case ADMINS:
			return PermissionType.getPermission("ADMINS");
		case OWNER:
			return PermissionType.getPermission("OWNER");
		}
		return null;
	}

	/**
	 * Utility to determine whether the player is being promoted or demoted
	 */
	private static String demoteOrPromote(PlayerType oldRank,
			PlayerType newRank, boolean upperCaseFirstLetter) {
		int oldIndex = 0;
		int newIndex = 0;
		for (int i = 0; i < PlayerType.values().length; i++) {
			if (PlayerType.values()[i] == oldRank) {
				oldIndex = i;
			}
			if (PlayerType.values()[i] == newRank) {
				newIndex = i;
			}
		}
		String res = oldIndex <= newIndex ? "promote" : "demote";
		if (upperCaseFirstLetter) {
			return res.substring(0, 1).toUpperCase() + res.substring(1);
		}
		return res;
	}

	private boolean hasPermissionToViewAnything() {
		return gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("MEMBERS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("MODS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("ADMINS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("OWNER"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
	}
}
