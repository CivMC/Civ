package net.civmc.kitpvp.gui.selection;

import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.data.Kit;
import net.civmc.kitpvp.data.KitPvpDao;
import net.civmc.kitpvp.gui.EditKitGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import java.util.logging.Level;

public class CountSelectionGui extends ItemSelectionGui {

    public CountSelectionGui(KitPvpDao dao, Player player, int slot, Kit kit, EditKitGui gui) {
        super(dao, "Change item count", player, slot, kit, gui::open, gui);
    }

    @Override
    public void addItems(ClickableInventory inventory) {
        ItemStack kitItem = kit.items()[this.slot].clone();
        ItemStack custom = new ItemStack(Material.PAPER);
        ItemMeta customMeta = custom.getItemMeta();
        customMeta.itemName(Component.text("Custom amount", NamedTextColor.GOLD));
        custom.setItemMeta(customMeta);
        inventory.setSlot(new Clickable(custom) {
            @Override
            protected void clicked(@NotNull Player clicker) {
                inventory.setOnClose(null);
                clicker.closeInventory();
                clicker.beginConversation(new ConversationFactory(JavaPlugin.getPlugin(KitPvpPlugin.class))
                    .withFirstPrompt(new ValidatingPrompt() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public @NotNull String getPromptText(@NotNull ConversationContext context) {
                            return ChatColor.GOLD + "Enter item count or 'cancel' to cancel:";
                        }

                        @Override
                        public @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
                            try {
                                int num = Integer.parseInt(input);

                                ItemStack[] items = kit.items().clone();
                                ItemStack item = kit.items()[slot];
                                item.setAmount(num);
                                gui.setLastItem(item);
                                items[slot] = item;

                                Kit updatedKit = dao.updateKit(kit.id(), kit.icon(), items);
                                gui.updateKit(updatedKit);
                                Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(KitPvpPlugin.class), () -> {
                                    inventory.setOnClose(null);
                                    gui.open();
                                });
                            } catch (Exception e) {
                                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Error setting item count", e);
                            }
                            return null;
                        }

                        @Override
                        protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
                            try {
                                int num = Integer.parseInt(input);
                                if (num >= 1 && num <= kitItem.getMaxStackSize()) {
                                    return true;
                                } else {
                                    clicker.sendMessage(Component.text("Must be between 1 and " + kitItem.getMaxStackSize(), NamedTextColor.RED));
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
                            clicker.sendMessage(Component.text("Cancelled updating item count", NamedTextColor.GOLD));
                            open();
                        }
                    })
                    .withTimeout(30)
                    .withModality(false)
                    .withLocalEcho(false)
                    .withEscapeSequence("cancel")
                    .buildConversation(clicker));
            }
        }, 18);
        if (kitItem.getMaxStackSize() == 64) {
            kitItem.setAmount(1);
            inventory.setSlot(toClickable(kitItem.clone()), 19);
            kitItem.setAmount(8);
            inventory.setSlot(toClickable(kitItem.clone()), 20);
            kitItem.setAmount(16);
            inventory.setSlot(toClickable(kitItem.clone()), 21);
            kitItem.setAmount(32);
            inventory.setSlot(toClickable(kitItem.clone()), 22);
            kitItem.setAmount(64);
            inventory.setSlot(toClickable(kitItem.clone()), 23);
        } else if (kitItem.getMaxStackSize() == 16) {
            kitItem.setAmount(1);
            inventory.setSlot(toClickable(kitItem.clone()), 19);
            kitItem.setAmount(2);
            inventory.setSlot(toClickable(kitItem.clone()), 20);
            kitItem.setAmount(4);
            inventory.setSlot(toClickable(kitItem.clone()), 21);
            kitItem.setAmount(8);
            inventory.setSlot(toClickable(kitItem.clone()), 22);
            kitItem.setAmount(16);
            inventory.setSlot(toClickable(kitItem.clone()), 23);
        }
    }
}
