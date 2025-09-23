package net.civmc.kitpvp.gui.selection;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.anvil.AnvilGui;
import net.civmc.kitpvp.anvil.AnvilGuiListener;
import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.civmc.kitpvp.kit.KitCost;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.component.DataComponents;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;

public class EnchantmentGui extends ItemSelectionGui {

    private static final int[] ENCHANT_START_SLOTS = new int[]{0, 9, 18, 27, 36, 5, 14, 23, 32, 41};

    private final AnvilGui anvilGui;

    public EnchantmentGui(KitPvpDao dao, AnvilGui anvilGui, Player player, int slot, Kit kit, EditKitGui gui) {
        super(dao, "Enchant", player, slot, kit, gui::open, gui);
        this.anvilGui = anvilGui;
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        ItemStack kitItem = kit.items()[this.slot].clone();
        if (!CustomItem.isCustomItem(kitItem)) {
            int slot = 0;
            List<Enchantment> enchants = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream()
                .sorted(Comparator.comparingInt(Enchantment::getMaxLevel).reversed())
                .toList();
            OUTER:
            for (Enchantment enchantment : enchants) {
                if (enchantment.canEnchantItem(kitItem)
                    && !enchantment.isCursed()
                    && enchantment != Enchantment.BANE_OF_ARTHROPODS
                    && enchantment != Enchantment.SMITE
                    && enchantment != Enchantment.MENDING) {
                    for (Enchantment currentEnchantment : kitItem.getEnchantments().keySet()) {
                        if (currentEnchantment.conflictsWith(enchantment) && currentEnchantment != enchantment) {
                            slot++;
                            continue OUTER;
                        }
                    }
                    int pos = ENCHANT_START_SLOTS[slot++];
                    for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
                        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                        ItemMeta meta = book.getItemMeta();
                        meta.itemName(enchantment.displayName(level).color(NamedTextColor.GOLD));
                        book.setItemMeta(meta);

                        ItemStack enchantedKitItem = kitItem.clone();
                        enchantedKitItem.addEnchantment(enchantment, level);
                        int cost = KitCost.ENCHANTMENT_COST_PER_LEVEL.getOrDefault(enchantment, 0) * level;
                        if (cost > 0) {
                            inventory.setSlot(toClickable(KitCost.setPoints(book, cost), enchantedKitItem), pos++);
                        } else {
                            inventory.setSlot(toClickable(book, enchantedKitItem), pos++);
                        }
                    }
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
                inventory.setSlot(toClickable(clearEnchants, clearEnchantsItem), 51);
            }
        }

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
                inventory.setSlot(toClickable(KitCost.setPoints(breakable, -100), breakableItem), 52);
            } else {
                ItemStack unbreakable = new ItemStack(Material.BEDROCK);
                ItemMeta unbreakableMeta = unbreakable.getItemMeta();
                unbreakableMeta.itemName(Component.text("Make unbreakable", NamedTextColor.GOLD));
                unbreakable.setItemMeta(unbreakableMeta);

                ItemStack unbreakableItem = kitItem.clone();
                ItemMeta unbreakableItemMeta = unbreakableItem.getItemMeta();
                unbreakableItemMeta.setUnbreakable(true);
                unbreakableItem.setItemMeta(unbreakableItemMeta);
                inventory.setSlot(toClickable(KitCost.setPoints(unbreakable, 100), unbreakableItem), 52);
            }
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
                    anvilGui.open(clicker, Component.text("Durability"), new AnvilGuiListener() {
                        @Override
                        public void onClose() {
                            JavaPlugin plugin = JavaPlugin.getProvidingPlugin(KitPvpPlugin.class);
                            Bukkit.getScheduler().runTask(plugin, EnchantmentGui.this::open);
                        }

                        @Override
                        public boolean onRename(String name) {
                            int num;
                            try {
                                num = Integer.parseInt(name);
                                if (num < 1 || num > maxDamage) {
                                    clicker.sendMessage(Component.text("Must be between 1 and " + maxDamage, NamedTextColor.RED));
                                    return false;
                                }
                            } catch (NumberFormatException e) {
                                clicker.sendMessage(Component.text("Must be a number", NamedTextColor.RED));
                                return false;
                            }

                            try {
                                ItemStack[] items = kit.items().clone();
                                ItemStack item = kit.items()[EnchantmentGui.this.slot];
                                Damageable meta = (Damageable) item.getItemMeta();
                                meta.setDamage(maxDamage - num);
                                item.setItemMeta(meta);
                                items[EnchantmentGui.this.slot] = item;
                                gui.setLastItem(item);

                                JavaPlugin plugin = JavaPlugin.getProvidingPlugin(KitPvpPlugin.class);
                                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                    Kit updatedKit = dao.updateKit(kit.id(), kit.icon(), items);
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        gui.updateKit(updatedKit);
                                        inventory.setOnClose(null);
                                        gui.open();
                                    });
                                });
                            } catch (Exception e) {
                                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error setting durability", e);
                            }
                            return true;
                        }
                    });
                }
            }, 53);
        }
    }
}
