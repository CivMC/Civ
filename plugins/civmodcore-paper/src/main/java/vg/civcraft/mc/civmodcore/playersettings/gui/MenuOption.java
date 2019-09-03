package vg.civcraft.mc.civmodcore.playersettings.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;

public class MenuOption extends MenuItem {
	
	private PlayerSetting<?> setting;

	public MenuOption(MenuSection menu, PlayerSetting<?> setting) {
		super(setting.getNiceName(), menu);
		this.setting = setting;
	}

	@Override
	public IClickable getMenuRepresentation(Player player) {
		ItemStack item = setting.getGuiRepresentation(player.getUniqueId());
		return new Clickable(item) {
			
			@Override
			public void clicked(Player p) {
				setting.handleMenuClick(p, getParent());				
			}
		};
	}

}
