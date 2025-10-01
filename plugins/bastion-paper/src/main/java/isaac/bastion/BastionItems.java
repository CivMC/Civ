package isaac.bastion;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import java.util.List;

public interface BastionItems {

    static void registerCustomItems() {
        // create vault bastion
        ItemStack vaultBastion = ItemStack.of(Material.SPONGE);
        vaultBastion.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>Vault Bastion</i>"));
        vaultBastion.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("This bastion will protect you from grief"),
            Component.text("It will also block pearls when they land"),
            Component.text("As well as stop elytra")
        )));
        vaultBastion.setAmount(1);
        CustomItem.registerCustomItem("vault_bastion", vaultBastion);

        // create city bastion
        ItemStack cityBastion = ItemStack.of(Material.BONE_BLOCK);
        cityBastion.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>City Bastion</i>"));
        cityBastion.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("City bastions block reinforcements and elytra")
        )));
        cityBastion.setAmount(1);
        CustomItem.registerCustomItem("city_bastion", cityBastion);
    }
}
