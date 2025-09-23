package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitPotion;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.civmc.kitpvp.kit.KitCost;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
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
        for (KitPotion potionType : KitPotion.values()) {
            ItemStack potion = new ItemStack(base);
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            meta.setBasePotionType(potionType.getType());
            potion.setItemMeta(meta);
            inventory.setSlot(toClickable(KitCost.setPoints(potion, potionType.getCost() + (base == Material.TIPPED_ARROW ? 3 : 0)), potion), slot++);
        }
        Runnable redraw = () -> Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
            inventory.setOnClose(null);
            this.open();
        });

        if (this.base != Material.POTION) {
            ItemStack showDrinkablePotions = new ItemStack(Material.POTION);
            PotionMeta meta = (PotionMeta) showDrinkablePotions.getItemMeta();
            meta.setColor(Color.YELLOW);
            meta.setHideTooltip(true);
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
            meta.setHideTooltip(true);
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
            meta.setHideTooltip(true);
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
