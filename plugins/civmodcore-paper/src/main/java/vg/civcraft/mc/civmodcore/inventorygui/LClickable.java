package vg.civcraft.mc.civmodcore.inventorygui;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Convience class for lambda support in clickables. Unfortunately java doesn't
 * allow usage of abstract classes as functional interfaces, see also
 * https://stackoverflow.com/questions/24610207/abstract-class-as-functional-interface
 *
 */
public class LClickable extends Clickable {

	private Consumer<Player> clickFunction;
	
	public LClickable(ItemStack item, Consumer<Player> clickFunction) {
		super(item);
		this.clickFunction = clickFunction;
	}

	@Override
	public void clicked(Player p) {
		clickFunction.accept(p);
	}

}
