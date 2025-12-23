package isaac.bastion;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItemFactory;
import java.util.List;

public interface BastionItems {

    CustomItemFactory VAULT_BASTION = CustomItem.registerCustomItem("vault_bastion", () -> {
        final ItemStack vaultBastion = ItemStack.of(Material.SPONGE);
        vaultBastion.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>Vault Bastion</i>"));
        vaultBastion.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("This bastion will protect you from grief"),
            Component.text("It will also block pearls when they land"),
            Component.text("As well as stop elytra")
        )));
        vaultBastion.setAmount(1);
        return vaultBastion;
    });

    CustomItemFactory CITY_BASTION = CustomItem.registerCustomItem("city_bastion", () -> {
        final ItemStack cityBastion = ItemStack.of(Material.BONE_BLOCK);
        cityBastion.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>City Bastion</i>"));
        cityBastion.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("City bastions block reinforcements and elytra")
        )));
        cityBastion.setAmount(1);
        return cityBastion;
    });

    /**
     * Register custom items for use in the config
     */
    static void registerCustomItems() {
        // not sure if this is necessary, but just in case the above registration doesn't happen
        VAULT_BASTION.createItem();
        CITY_BASTION.createItem();
    }
}
