package vg.civcraft.mc.civmodcore.players.settings.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;

public class MenuOption extends MenuItem {
	
	private final PlayerSetting<?> setting;

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

	public PlayerSetting<?> getSetting() {
		return this.setting;
	}

}
