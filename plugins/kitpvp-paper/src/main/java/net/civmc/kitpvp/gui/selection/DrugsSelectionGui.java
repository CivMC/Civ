package net.civmc.kitpvp.gui.selection;

import com.dre.brewery.Brew;
import com.dre.brewery.api.BreweryApi;
import com.dre.brewery.recipe.BRecipe;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.data.Kit;
import net.civmc.kitpvp.data.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class DrugsSelectionGui extends ItemSelectionGui {

    public DrugsSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, "Drugs", player, slot, kit, parent, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        int slot = 0;
        for (String drug : new String[]{
            "Cyanide",
            "Cannabis",
            "Meth",
            "Blue Meth",
            "Heroin",
            "Oestrogen",
            "Caffeine",
            "Ivermectin",
            "DMT",
            "Xanax",
            "Steroids",
            "Testosterone",
            "Vicodin",
            "Yakult",
            "Cocaine",
            "Speed",
            "NAD+",
            "Epinephrine",
            "Firefoam",
            "Nitroglycerin"
        }) {
            BRecipe matching = BRecipe.getMatching(drug);
            if (matching != null) {
                Brew brew = matching.createBrew(10);
                ItemStack item = brew.createItem(matching);
                brew.seal(item, null);
                inventory.setSlot(toClickable(item), slot++);
            }
        }
    }
}
