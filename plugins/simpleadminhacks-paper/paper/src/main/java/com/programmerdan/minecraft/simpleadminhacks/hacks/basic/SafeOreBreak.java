package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;

public final class SafeOreBreak extends BasicHack {

	@AutoLoad
	private List<List<String>> ores;

	private final List<BooleanOreSetting> oreSettings = new ArrayList<>();
	private final List<List<Material>> breakOres = new ArrayList<>();

	public SafeOreBreak(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void onEnable() {
		super.onEnable();

		MenuSection mainMenu = plugin.getSettingManager().getMainMenu();

		OUTER:
		for (List<String> parts : ores) {
			List<Material> materialParts = new ArrayList<>(parts.size());

			for (String part : parts) {
				Material material = MaterialUtils.getMaterial(part);
				if (material == null) {
					logger.warning("Invalid material '" + part + "'. Skipping.");
					continue OUTER;
				}
				materialParts.add(material);
			}

			breakOres.add(materialParts);
			String materialName = PlainTextComponentSerializer.plainText().serialize(Component.translatable(materialParts.get(0)));
			BooleanOreSetting setting = new BooleanOreSetting(plugin, true,
					"Safe " + materialName + " break",
					"safeOreBreak_" + materialParts.get(0).getKey().getKey(),
					"Prevents you from breaking " + materialName
							+ " without a silk touch pickaxe.",
					materialParts.get(0));
			oreSettings.add(setting);
			PlayerSettingAPI.registerSetting(setting, mainMenu);
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	@EventHandler(ignoreCancelled = true)
	public void onOreBreak(BlockBreakEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
			return;
		}

		ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
		if (item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
			return;
		}

		OUTER:
		for (int i = 0; i < breakOres.size(); i++) {
			List<Material> list = breakOres.get(i);
			for (Material material : list) {
				if (material == event.getBlock().getType()) {
					Boolean value = oreSettings.get(i).getValue(event.getPlayer());

					if (!value) {
						continue OUTER;
					}

					event.getPlayer().sendMessage(Component.text(
									"A SimpleAdminHacks /config option is preventing you from breaking that ore without a silk touch pickaxe.")
							.color(NamedTextColor.RED));

					event.setCancelled(true);
					return;
				}
			}
		}
	}

	private static class BooleanOreSetting extends BooleanSetting {

		private final Material ore;

		public BooleanOreSetting(JavaPlugin owningPlugin, Boolean defaultValue, String name, String identifier,
							  String description, Material ore) {
			super(owningPlugin, defaultValue, name, identifier, description);
			this.ore = ore;
		}

		@Override
		public ItemStack getGuiRepresentation(UUID player) {
			ItemStack item;
			if (getValue(player)) {
				item = new ItemStack(ore);
				item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
				item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			} else {
				item = new ItemStack(ore);
			}
			applyInfoToItemStack(item, player);
			return item;
		}
	}
}
