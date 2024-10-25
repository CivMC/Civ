package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.data.Kit;
import net.civmc.kitpvp.data.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class BlockSelectionGui extends ItemSelectionGui {

    public BlockSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, Runnable parent, EditKitGui gui) {
        super(dao, "Blocks", player, slot, kit, parent, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        Material[] types = new Material[] {
            Material.OBSIDIAN,
            Material.COBBLESTONE,
            Material.COBWEB,
            Material.IRON_DOOR,
            Material.OAK_DOOR,
            Material.OAK_PLANKS,
            Material.OAK_LOG,
            Material.CRAFTING_TABLE,
            Material.WHITE_WOOL,
            Material.BONE_BLOCK,
            Material.SANDSTONE,
            Material.GRAVEL,
            Material.ICE,
            Material.PACKED_ICE,
            Material.BLUE_ICE,
            Material.OAK_TRAPDOOR,
            Material.HAY_BLOCK,
            Material.SOUL_SAND,
            Material.SOUL_SOIL,
            Material.DIRT,
            Material.SAND,
            Material.STONE,
            Material.TNT,
            Material.SCAFFOLDING,
            Material.SLIME_BLOCK,
            Material.HONEY_BLOCK,
            Material.GLOWSTONE,
            Material.GLASS,
            Material.NETHERRACK
        };
        for (int i = 0; i < types.length; i++) {
            inventory.setSlot(toClickable(new ItemStack(types[i])), i);
        }
    }
}
