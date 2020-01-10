package vg.civcraft.mc.namelayer.gui;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.chatDialog.Dialog;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.RunnableOnGroup;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GUIGroupOverview {

	private int currentPage;
	private Player p;
	private static GroupManager gm;
	private boolean autoAccept;

	public GUIGroupOverview(Player p) {
		if (gm == null) {
			gm = NameAPI.getGroupManager();
		}
		autoAccept = NameLayerPlugin.getAutoAcceptHandler().getAutoAccept(p.getUniqueId());
		this.p = p;
		this.currentPage = 0;
	}

	public void showScreen() {

		ClickableInventory ci = new ClickableInventory(54, p.getName()
				+ "'s groups");
		final List<Clickable> groups = getGroupClickables();
		if (groups.size() < 45 * currentPage) {
			// would show an empty page, so go to previous
			currentPage--;
			showScreen();
		}

		for (int i = 45 * currentPage; i < 45 * (currentPage + 1)
				&& i < groups.size(); i++) {
			ci.setSlot(groups.get(i), i - (45 * currentPage));
		}

		// create group
		ci.setSlot(getCreateGroupClickable(), 46);
		//join group
		ci.setSlot(getJoinGroupClickable(), 47);
		
		//autoaccept toggle
		ItemStack toggle = MenuUtils.toggleButton(autoAccept, ChatColor.GOLD + "Automatically accept group invites", true);
		Clickable toggleClick = new Clickable(toggle) {
			
			@Override
			public void clicked(Player p) {
				if (autoAccept){
					NameLayerPlugin.log(Level.INFO,
							p.getName() + " turned autoaccept for invites off "
									+ " via the gui");
					p.sendMessage(ChatColor.GREEN + "You will no longer automatically accept group invites");
				}
				else {
					NameLayerPlugin.log(Level.INFO,
							p.getName() + " turned autoaccept for invites on "
									+ " via the gui");
					p.sendMessage(ChatColor.GREEN + "You will automatically accept group invites");
				}
				autoAccept = !autoAccept;
				NameLayerPlugin.getAutoAcceptHandler().toggleAutoAccept(p.getUniqueId(), true);
				showScreen();			
			}
		};
		ci.setSlot(toggleClick, 48);

		// previous button
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
		if ((45 * (currentPage + 1)) <= groups.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ISUtils.setName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (currentPage + 1)) <= groups.size()) {
						currentPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, 53);
		}

		// close button
		ItemStack backToOverview = new ItemStack(Material.WOOD_DOOR);
		ISUtils.setName(backToOverview, ChatColor.GOLD + "Close");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				ClickableInventory.forceCloseInventory(arg0);
			}
		}, 49);
		ci.showInventory(p);
	}

	private List<Clickable> getGroupClickables() {
		String defaultGroupName = gm.getDefaultGroup(p.getUniqueId());
		List<String> groupNames = gm.getAllGroupNames(p.getUniqueId());
		List<Clickable> result = new ArrayList<Clickable>();
		Set<String> alreadyProcessed = new HashSet<String>();
		for (String groupName : groupNames) {
			if (alreadyProcessed.contains(groupName)) {
				continue;
			}
			final Group g = GroupManager.getGroup(groupName);
			if (g == null) {
				continue;
			}
			Clickable c;
			PlayerType pType = g.getPlayerType(p.getUniqueId());
			if (pType == null) {
				continue;
			}
			ItemStack is = null;
			switch (pType) {
			case MEMBERS:
				is = new ItemStack(Material.LEATHER_CHESTPLATE);
				ISUtils.addLore(is, ChatColor.AQUA + "Your rank: Member");
				break;
			case MODS:
				is = new ItemStack(Material.GOLD_CHESTPLATE);
				ISUtils.addLore(is, ChatColor.AQUA + "Your rank: Mod");
				break;
			case ADMINS:
				is = new ItemStack(Material.IRON_CHESTPLATE);
				ISUtils.addLore(is, ChatColor.AQUA + "Your rank: Admin");
				break;
			case OWNER:
				is = new ItemStack(Material.DIAMOND_CHESTPLATE);
				if (g.isOwner(p.getUniqueId())) {
					ISUtils.addLore(is, ChatColor.AQUA
							+ "Your rank: Primary owner");
				} else {
					ISUtils.addLore(is, ChatColor.AQUA + "Your rank: Owner");
				}
				break;
			}
			if (is == null) {
				continue;
			}
			ItemMeta im = is.getItemMeta();
			im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
			if (g.getName().equals(defaultGroupName)) {
				List<String> lore = im.getLore();
				lore.add(ChatColor.DARK_AQUA + "Your current default group");
				im.setLore(lore);
				im.addEnchant(Enchantment.DURABILITY, 1, true);
			}
			is.setItemMeta(im);
			if (gm.hasAccess(g, p.getUniqueId(),
					PermissionType.getPermission("GROUPSTATS"))) {
				ISUtils.addLore(
						is,
						ChatColor.AQUA
								+ String.valueOf(g.getAllMembers().size())
								+ " member"
								+ (g.getAllMembers().size() > 1 ? "s" : ""));
			}
			ISUtils.setName(is, ChatColor.GOLD + g.getName());
			if (gm.hasAccess(g, p.getUniqueId(),
					PermissionType.getPermission("OPEN_GUI"))) {
				c = new Clickable(is) {

					@Override
					public void clicked(Player arg0) {
						MainGroupGUI mgui = new MainGroupGUI(p, g);
					}
				};
			} else {
				ISUtils.addLore(is, ChatColor.RED
						+ "You aren't permitted to open", ChatColor.RED
						+ "the GUI for this group");
				c = new DecorationStack(is);
			}
			result.add(c);
			alreadyProcessed.add(groupName);
		}
		return result;
	}

	private Clickable getCreateGroupClickable() {
		ItemStack is = new ItemStack(Material.APPLE);
		ISUtils.setName(is, ChatColor.GOLD + "Create group");
		Clickable c = new Clickable(is) {

			@Override
			public void clicked(final Player p) {
				p.sendMessage(ChatColor.YELLOW
						+ "Enter the name of your new group or \"cancel\" to exit this prompt");
				ClickableInventory.forceCloseInventory(p);
				Dialog dia = new Dialog(p, NameLayerPlugin.getInstance()) {

					@Override
					public List<String> onTabComplete(String wordCompleted,
							String[] fullMessage) {
						return new LinkedList<String>();
					}

					@Override
					public void onReply(String[] message) {
						if (message.length > 1) {
							p.sendMessage(ChatColor.RED
									+ "Group names may not contain spaces");
							showScreen();
							return;
						}
						String groupName = message[0];
						if (groupName.equals("")) {
							p.sendMessage(ChatColor.RED
									+ "You didn't enter anything!");
							showScreen();
							return;
						}
						if (groupName.equals("cancel")) {
							showScreen();
							return;
						}
						if (NameLayerPlugin.getInstance().getGroupLimit() < gm
								.countGroups(p.getUniqueId()) + 1
								&& !(p.isOp() || p
										.hasPermission("namelayer.admin"))) {
							p.sendMessage(ChatColor.RED
									+ "You cannot create any more groups! Please delete an un-needed group before making more.");
							showScreen();
							return;
						}
						// enforce regulations on the name
						if (groupName.length() > 32) {
							p.sendMessage(ChatColor.RED
									+ "The group name is not allowed to contain more than 32 characters");
							showScreen();
							return;
						}
						Charset latin1 = StandardCharsets.ISO_8859_1;
						boolean invalidChars = false;
						if (!latin1.newEncoder().canEncode(groupName)) {
							invalidChars = true;
						}

						for (char c : groupName.toCharArray()) {
							if (Character.isISOControl(c)) {
								invalidChars = true;
							}
						}

						if (invalidChars) {
							p.sendMessage(ChatColor.RED
									+ "You used characters, which are not allowed");
							showScreen();
							return;
						}

						if (GroupManager.getGroup(groupName) != null) {
							p.sendMessage(ChatColor.RED
									+ "That group is already taken. Try another unique group name.");
							showScreen();
							return;
						}

						final UUID uuid = p.getUniqueId();
						Group g = new Group(groupName, uuid, false, null, -1);
						gm.createGroupAsync(g, new RunnableOnGroup() {
							@Override
							public void run() {
								Player p = null;
								p = Bukkit.getPlayer(uuid);
								Group g = getGroup();
								if (p != null) {
									if (g.getGroupId() == -1) { // failure
										p.sendMessage(ChatColor.RED + "That group is already taken or creation failed.");
									} else {
										p.sendMessage(ChatColor.GREEN + "The group " + g.getName() + " was successfully created.");
										if (NameLayerPlugin.getInstance().getGroupLimit() == gm.countGroups(p.getUniqueId())) {
											p.sendMessage(ChatColor.YELLOW + "You have reached the group limit with " 
													+ NameLayerPlugin.getInstance().getGroupLimit()
													+ " groups! Please delete un-needed groups if you wish to create more.");
										}
									}
									showScreen();

								} else {
									NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group {0} creation complete resulting in group id: {1}",
											new Object[] {g.getName(), g.getGroupId()});
								}
							}
						}, false);
					}
				};

			}
		};
		return c;
	}
	
	private Clickable getJoinGroupClickable() {
		ItemStack is = new ItemStack(Material.CHEST);
		ISUtils.setName(is, ChatColor.GOLD + "Join password protected group");
		Clickable c = new Clickable(is) {
			
			@Override
			public void clicked(final Player p) {
				p.sendMessage(ChatColor.YELLOW + "Enter the name of the group or \"cancel\" to leave this prompt");
				ClickableInventory.forceCloseInventory(p);
				Dialog dia = new Dialog(p, NameLayerPlugin.getInstance()) {
					
					@Override
					public List<String> onTabComplete(String wordCompleted, String[] fullMessage) {
						return new LinkedList<String>();
					}
					
					@Override
					public void onReply(String[] message) {
						if (message.length > 1) {
							p.sendMessage(ChatColor.RED + "Group names can't contain spaces");
							showScreen();
							return;
						}
						String groupName = message [0];
						if (groupName.equals("cancel")) {
							showScreen();
							return;
						}
						final Group g = gm.getGroup(groupName);
						if (g == null) {
							p.sendMessage(ChatColor.RED + "This group doesn't exist");
							showScreen();
							return;
						}
						if (g.isMember(p.getUniqueId())) {
							p.sendMessage(ChatColor.RED + "You are already a member of this group");
							showScreen();
							return;
						}
						p.sendMessage(ChatColor.YELLOW + "Enter the group password");
						Dialog passDia = new Dialog(p, NameLayerPlugin.getInstance()) {
							
							@Override
							public List<String> onTabComplete(String wordCompleted, String[] fullMessage) {
								return new LinkedList<String>();
							}
							
							@Override
							public void onReply(String[] message) {
								if (g.getPassword() == null || !g.getPassword().equals(message [0])) {
									p.sendMessage(ChatColor.RED + "Wrong password");
									showScreen();
								}
								else {
									Group gro = ensureFreshGroup(g);
									GroupPermission groupPerm = gm.getPermissionforGroup(gro);
									PlayerType pType = groupPerm.getFirstWithPerm(PermissionType.getPermission("JOIN_PASSWORD"));
									if (pType == null){
										p.sendMessage(ChatColor.RED + "Someone derped. This group does not have the specified permission to let you join, sorry.");
										showScreen();
										return;
									}
									NameLayerPlugin.log(Level.INFO,
											p.getName() + " joined with password "
													+ " to group " + g.getName()
													+ " via the gui");
									gro.addMember(p.getUniqueId(), pType);
									p.sendMessage(ChatColor.GREEN + "You have successfully been added to "  + gro.getName());
									showScreen();
								}
								
							}
						};
					}
				};
				
			}
		};
		return c;
	}
	
	private Group ensureFreshGroup(Group g) {
		if (!g.isValid()) {
			return GroupManager.getGroup(g.getName());
		}
		return g;
	}
}
