package vg.civcraft.mc.civmodcore.players.settings.gui;

import java.util.function.Function;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public class ClickableMenuItem extends MenuItem {
	
	private Function<Player, IClickable> clickable;

	public ClickableMenuItem(Function<Player, IClickable> clickable, MenuSection parent) {
		super("", parent);
		this.clickable = clickable;
	}

	@Override
	public IClickable getMenuRepresentation(Player player) {
		return clickable.apply(player);
	}

}
