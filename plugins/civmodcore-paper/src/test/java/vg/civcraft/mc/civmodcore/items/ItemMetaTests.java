package vg.civcraft.mc.civmodcore.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public class ItemMetaTests {

    private ItemStack templateItem;

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        templateItem = new ItemStack(Material.STICK);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testAdvancedDisplayNameEquality() {
        final var formerItem = templateItem.clone();
        ItemUtils.handleItemMeta(formerItem, (ItemMeta meta) -> {
            meta.setDisplayName("Hello!");
            return true;
        });
        final var latterItem = templateItem.clone();
        ItemUtils.handleItemMeta(latterItem, (ItemMeta meta) -> {
            meta.displayName(Component.text("Hello!"));
            return true;
        });
        Assertions.assertTrue(ChatUtils.areComponentsEqual(
            ItemUtils.getComponentDisplayName(formerItem),
            ItemUtils.getComponentDisplayName(latterItem)));
        Assertions.assertTrue(ItemUtils.areItemsSimilar(formerItem, latterItem));
    }

    @Test
    @Disabled("Paper's switch to native Component display names removed the legacy/Adventure split this assertion relied on")
    @SuppressWarnings("deprecation")
    public void testBaseComponent() {
        final var formerItem = templateItem.clone();
        ItemUtils.handleItemMeta(formerItem, (ItemMeta meta) -> {
            meta.setDisplayName("Hello!");
            return true;
        });
        final var latterItem = templateItem.clone();
        ItemUtils.handleItemMeta(latterItem, (ItemMeta meta) -> {
            meta.displayName(Component.text("Hello!"));
            return true;
        });
        Assertions.assertTrue(ChatUtils.isBaseComponent(
            ItemUtils.getComponentDisplayName(formerItem)));
        Assertions.assertFalse(ChatUtils.isBaseComponent(
            ItemUtils.getComponentDisplayName(latterItem)));
    }

}
