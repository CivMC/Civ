package net.civmc.kitpvp.gui.selection;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.data.Kit;
import net.civmc.kitpvp.data.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.component.DataComponents;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class EnchantmentGui extends ItemSelectionGui {

    private static final int[] ENCHANT_START_SLOTS = new int[]{0, 9, 18, 27, 36, 5, 14, 23, 32, 41};

    public EnchantmentGui(KitPvpDao dao, Player player, int slot, Kit kit, EditKitGui gui) {
        super(dao, "Enchant", player, slot, kit, gui::open, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        ItemStack kitItem = kit.items()[this.slot].clone();
        int slot = 0;
        List<Enchantment> enchants = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream()
            .sorted(Comparator.comparingInt(Enchantment::getMaxLevel).reversed())
            .toList();
        for (Enchantment enchantment : enchants) {
            if (enchantment.canEnchantItem(kitItem)
                && !enchantment.isCursed()
                && enchantment != Enchantment.BANE_OF_ARTHROPODS
                && enchantment != Enchantment.SMITE
                && enchantment != Enchantment.MENDING) {
                int pos = ENCHANT_START_SLOTS[slot++];
                for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                    ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                    ItemMeta meta = book.getItemMeta();
                    meta.itemName(enchantment.displayName(level).color(NamedTextColor.GOLD));
                    book.setItemMeta(meta);

                    ItemStack enchantedKitItem = kitItem.clone();
                    enchantedKitItem.addEnchantment(enchantment, level);
                    inventory.setSlot(toClickable(book, enchantedKitItem), pos++);
                }
            }
        }

        // Damageable has a similar interface but for some reason it doesn't work
        if (((CraftItemStack) kitItem).handle.has(DataComponents.MAX_DAMAGE)) {
            if (kitItem.getItemMeta().isUnbreakable()) {
                ItemStack breakable = new ItemStack(Material.CRACKED_STONE_BRICKS);
                ItemMeta breakableMeta = breakable.getItemMeta();
                breakableMeta.itemName(Component.text("Make breakable", NamedTextColor.GOLD));
                breakable.setItemMeta(breakableMeta);

                ItemStack breakableItem = kitItem.clone();
                ItemMeta breakableItemMeta = breakableItem.getItemMeta();
                breakableItemMeta.setUnbreakable(false);
                breakableItem.setItemMeta(breakableItemMeta);
                inventory.setSlot(toClickable(breakable, breakableItem), 51);
            } else {
                ItemStack unbreakable = new ItemStack(Material.BEDROCK);
                ItemMeta unbreakableMeta = unbreakable.getItemMeta();
                unbreakableMeta.itemName(Component.text("Make unbreakable", NamedTextColor.GOLD));
                unbreakable.setItemMeta(unbreakableMeta);

                ItemStack unbreakableItem = kitItem.clone();
                ItemMeta unbreakableItemMeta = unbreakableItem.getItemMeta();
                unbreakableItemMeta.setUnbreakable(true);
                unbreakableItem.setItemMeta(unbreakableItemMeta);
                inventory.setSlot(toClickable(unbreakable, unbreakableItem), 51);
            }
        }

        if (!kitItem.getEnchantments().isEmpty()) {
            ItemStack clearEnchants = new ItemStack(Material.BARRIER);
            ItemMeta clearEnchantsMeta = clearEnchants.getItemMeta();
            clearEnchantsMeta.itemName(Component.text("Clear enchants", NamedTextColor.GOLD));
            clearEnchants.setItemMeta(clearEnchantsMeta);

            ItemStack clearEnchantsItem = kitItem.clone();
            ItemMeta clearEnchantsItemMeta = clearEnchantsItem.getItemMeta();
            clearEnchantsItemMeta.removeEnchantments();
            clearEnchantsItem.setItemMeta(clearEnchantsItemMeta);
            inventory.setSlot(toClickable(clearEnchants, clearEnchantsItem), 52);
        }

        int maxDamage = ((CraftItemStack) kitItem).handle.getMaxDamage();
        if (maxDamage > 0) {
            ItemStack durability = new ItemStack(Material.ANVIL);
            ItemMeta durabilityMeta = durability.getItemMeta();
            durabilityMeta.itemName(Component.text("Change durability", NamedTextColor.GOLD));
            durability.setItemMeta(durabilityMeta);

            inventory.setSlot(new Clickable(durability) {
                @Override
                protected void clicked(@NotNull Player clicker) {
                    inventory.setOnClose(null);
                    clicker.closeInventory();
                    clicker.beginConversation(new ConversationFactory(JavaPlugin.getPlugin(KitPvpPlugin.class))
                        .withFirstPrompt(new ValidatingPrompt() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public @NotNull String getPromptText(@NotNull ConversationContext context) {
                                return ChatColor.GOLD + "Enter durability (between %s and %s) or 'cancel' to cancel:".formatted(1, maxDamage);
                            }

                            @Override
                            public @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
                                try {
                                    int num = Integer.parseInt(input);

                                    ItemStack[] items = kit.items().clone();
                                    ItemStack item = kit.items()[EnchantmentGui.this.slot];
                                    Damageable meta = (Damageable) item.getItemMeta();
                                    meta.setDamage(maxDamage - num);
                                    item.setItemMeta(meta);
                                    items[EnchantmentGui.this.slot] = item;
                                    gui.setLastItem(item);

                                    Kit updatedKit = dao.updateKit(kit.id(), kit.icon(), items);
                                    gui.updateKit(updatedKit);
                                    Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
                                        inventory.setOnClose(null);
                                        gui.open();
                                    });
                                } catch (Exception e) {
                                    JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error setting durability", e);
                                }
                                return null;
                            }

                            @Override
                            protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
                                try {
                                    int num = Integer.parseInt(input);
                                    if (num >= 1 && num <= maxDamage) {
                                        return true;
                                    } else {
                                        clicker.sendMessage(Component.text("Must be between 1 and " + maxDamage, NamedTextColor.RED));
                                        return false;
                                    }
                                } catch (NumberFormatException e) {
                                    clicker.sendMessage(Component.text("Must be a number", NamedTextColor.RED));
                                    return false;
                                }
                            }
                        })
                        .addConversationAbandonedListener(abandonedEvent -> {
                            if (!abandonedEvent.gracefulExit()) {
                                clicker.sendMessage(Component.text("Cancelled updating durability", NamedTextColor.GOLD));
                                open();
                            }
                        })
                        .withTimeout(30)
                        .withModality(false)
                        .withLocalEcho(false)
                        .withEscapeSequence("cancel")
                        .buildConversation(clicker));
                }
            }, 53);
        }
    }
}
