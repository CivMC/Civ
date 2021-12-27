package vg.civcraft.mc.civmodcore.players.settings.impl;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;

public class EnumSetting<T extends Enum<T>> extends PlayerSetting<T> {

	private final Class<T> enumClass;

	public EnumSetting(final JavaPlugin owningPlugin,
                       final T defaultValue,
                       final String niceName,
                       final String identifier,
                       final ItemStack gui,
                       final String description,
                       final boolean canBeChangedByPlayer,
                       final Class<T> enumClass) {
		super(owningPlugin, defaultValue, niceName, identifier, gui, description, canBeChangedByPlayer);
		this.enumClass = Objects.requireNonNull(enumClass);
	}

	@Override
	public T deserialize(final String raw) {
		return EnumUtils.getEnum(this.enumClass, raw, getDefaultValue());
	}

	@Override
	public boolean isValidValue(final String raw) {
		return EnumUtils.isValidEnum(this.enumClass, raw);
	}

	@Override
	public String serialize(final T value) {
		if (value == null) {
			return null;
		}
		return value.name();
	}

	@Override
	public String toText(final T value) {
		if (value == null) {
			return "<null>";
		}
		return value.name();
	}

	@Override
	public void handleMenuClick(final Player player, final MenuSection menu) {
		final T currentValue = getValue(player);

		final var view = new MultiPageView(player,
				EnumUtils.getEnumList(this.enumClass).stream()
						.sorted(Comparator.comparing(Enum::name))
						.map(value -> {
							final var item = new ItemStack(value == currentValue ? Material.GREEN_DYE : Material.RED_DYE);
							ItemUtils.setDisplayName(item, ChatColor.GOLD + toText(value));
							return new Clickable(item) {
								@Override
								protected void clicked(final Player ignored) {
									setValue(player, value);
									handleMenuClick(player, menu);
								}
							};
						})
						.collect(Collectors.<IClickable>toList()),
				getNiceName(),
				true);

		final var backButtonItem = new ItemStack(Material.ARROW);
		ItemUtils.setDisplayName(backButtonItem, ChatColor.AQUA + "Go back to " + menu.getName());
		view.setMenuSlot(new Clickable(backButtonItem) {
			@Override
			public void clicked(final Player clicker) {
				menu.showScreen(clicker);
			}
		}, 0);

		view.showScreen();
	}

}
