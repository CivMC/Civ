package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.data.Kit;
import net.civmc.kitpvp.data.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class PotionsSelectionGui extends ItemSelectionGui {

    private Material base = Material.POTION;

    public PotionsSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, "Potions", player, slot, kit, parent, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        int slot = 0;
        for (PotionType potionType : new PotionType[] {
            PotionType.NIGHT_VISION,
            PotionType.LONG_NIGHT_VISION,
            PotionType.INVISIBILITY,
            PotionType.LONG_INVISIBILITY,
            PotionType.LEAPING,
            PotionType.LONG_LEAPING,
            PotionType.STRONG_LEAPING,
            PotionType.FIRE_RESISTANCE,
            PotionType.LONG_FIRE_RESISTANCE,
            PotionType.SWIFTNESS,
            PotionType.LONG_SWIFTNESS,
            PotionType.STRONG_SWIFTNESS,
            PotionType.SLOWNESS,
            PotionType.LONG_SLOWNESS,
            PotionType.STRONG_SLOWNESS,
            PotionType.WATER_BREATHING,
            PotionType.LONG_WATER_BREATHING,
            PotionType.HEALING,
            PotionType.STRONG_HEALING,
            PotionType.HARMING,
            PotionType.STRONG_HARMING,
            PotionType.POISON,
            PotionType.LONG_POISON,
            PotionType.STRONG_POISON,
            PotionType.REGENERATION,
            PotionType.LONG_REGENERATION,
            PotionType.STRONG_REGENERATION,
            PotionType.STRENGTH,
            PotionType.LONG_STRENGTH,
            PotionType.STRONG_STRENGTH,
            PotionType.WEAKNESS,
            PotionType.LONG_WEAKNESS,
            PotionType.LUCK,
            PotionType.TURTLE_MASTER,
            PotionType.LONG_TURTLE_MASTER,
            PotionType.STRONG_TURTLE_MASTER,
            PotionType.SLOW_FALLING,
            PotionType.LONG_SLOW_FALLING,
        }) {
            ItemStack potion = new ItemStack(base);
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            meta.setBasePotionType(potionType);
            potion.setItemMeta(meta);
            inventory.setSlot(toClickable(potion), slot++);
        }
        Runnable redraw = () -> Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
            inventory.setOnClose(null);
            this.open();
        });

        if (this.base != Material.POTION) {
            ItemStack showDrinkablePotions = new ItemStack(Material.POTION);
            PotionMeta meta = (PotionMeta) showDrinkablePotions.getItemMeta();
            meta.setColor(Color.YELLOW);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.displayName(Component.text("Show drinkable potions", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            showDrinkablePotions.setItemMeta(meta);
            inventory.setSlot(new Clickable(showDrinkablePotions) {
                @Override
                protected void clicked(@NotNull Player clicker) {
                    base = Material.POTION;
                    redraw.run();
                }
            }, 53);
        }
        if (this.base != Material.SPLASH_POTION) {
            ItemStack showSplashPotions = new ItemStack(Material.SPLASH_POTION);
            PotionMeta meta = (PotionMeta) showSplashPotions.getItemMeta();
            meta.setColor(Color.YELLOW);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.displayName(Component.text("Show splash potions", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            showSplashPotions.setItemMeta(meta);
            inventory.setSlot(new Clickable(showSplashPotions) {
                @Override
                protected void clicked(@NotNull Player clicker) {
                    base = Material.SPLASH_POTION;
                    redraw.run();
                }
            }, 52);
        }
        if (this.base != Material.TIPPED_ARROW) {
            ItemStack showTippedArrows = new ItemStack(Material.TIPPED_ARROW);
            PotionMeta meta = (PotionMeta) showTippedArrows.getItemMeta();
            meta.setColor(Color.YELLOW);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.displayName(Component.text("Show tipped arrows", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            showTippedArrows.setItemMeta(meta);
            inventory.setSlot(new Clickable(showTippedArrows) {
                @Override
                protected void clicked(@NotNull Player clicker) {
                    base = Material.TIPPED_ARROW;
                    redraw.run();
                }
            }, 51);
        }
    }
}
