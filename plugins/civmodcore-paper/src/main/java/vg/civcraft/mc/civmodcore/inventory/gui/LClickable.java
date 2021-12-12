package vg.civcraft.mc.civmodcore.inventory.gui;

import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

/**
 * Convience class for lambda support in clickables. Unfortunately java doesn't
 * allow usage of abstract classes as functional interfaces, see also
 * https://stackoverflow.com/questions/24610207/abstract-class-as-functional-interface
 *
 */
public class LClickable extends Clickable {

	private Consumer<Player> clickFunction;
	
	public LClickable(Material mat, String name, Consumer<Player> clickFunction) {
		this(mat, clickFunction);
		ItemUtils.setComponentDisplayName(this.item, Component.text(name));
	}
	
	public LClickable(Material mat, String name, Consumer<Player> clickFunction, String ... lore) {
		this(mat, name, clickFunction);
		if (lore.length > 0) {
			for (String s : lore) {
				ItemUtils.addComponentLore(this.item, Component.text(s));
			}
		}
	}
	
	public LClickable(Material mat, Consumer<Player> clickFunction) {
		this(new ItemStack(mat), clickFunction);
	}
	
	public LClickable(ItemStack item, Consumer<Player> clickFunction) {
		super(item);
		this.clickFunction = clickFunction;
	}

	@Override
	public void clicked(Player p) {
		clickFunction.accept(p);
	}

}
