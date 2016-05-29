package vg.civcraft.mc.namelayer.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.namelayer.group.Group;

public class LinkingGUI extends AbstractGroupGUI{
	
	private AdminFunctionsGUI parent;
	
	public LinkingGUI(Group g, Player p, AdminFunctionsGUI parent) {
		super(g, p);
		this.parent = parent;
	}

	public void showScreen() {
		ClickableInventory ci = new ClickableInventory(27, g.getName());
		ItemStack makeSuper = new ItemStack(Material.DIAMOND);
		ISUtils.setName(makeSuper, ChatColor.GOLD + "Make this group a super group of another group");
		ISUtils.addLore(makeSuper, ChatColor.AQUA + "This option means that the additional group you chose will inherit all members of this group with their ranks");
		Clickable superClick = new Clickable(makeSuper) {
			
			@Override
			public void clicked(Player arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		ItemStack makeSub = new ItemStack(Material.)
	}

}
