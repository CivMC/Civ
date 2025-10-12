package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public class Trowel extends BasicHack {

    private static final NamespacedKey TROWEL_ROW = new NamespacedKey(SimpleAdminHacks.instance(), "trowel_row");
    private static final ItemStack TROWEL = getTrowel(0);

    public Trowel(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getServer().addRecipe(new ShapedRecipe(new NamespacedKey(SimpleAdminHacks.instance(), "trowel"), TROWEL)
            .shape("  s", "ii ").setIngredient('i', Material.IRON_INGOT).setIngredient('s', Material.STICK));
    }

    private static ItemStack getTrowel(int row) {
        ItemStack trowel = new ItemStack(Material.IRON_HOE);
        ItemMeta meta = trowel.getItemMeta();
        meta.itemName(Component.text("Trowel"));
        meta.lore(List.of(
            Component.empty().append((Component.text("Selected row: ", NamedTextColor.GRAY).append(Component.text(row + 1, NamedTextColor.WHITE))).decoration(TextDecoration.ITALIC, false)),
            Component.text("Shift left click to cycle row."),
            Component.text("Right click to place a block from that row in your inventory."),
            Component.text("Row 1 is the top row; row 4 is the hotbar.")
        ));

        meta.setRarity(ItemRarity.COMMON);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey("heliodor", "no_combine"), PersistentDataType.BOOLEAN, true);
        pdc.set(TROWEL_ROW, PersistentDataType.INTEGER, row);
        trowel.setItemMeta(meta);

        CustomItem.registerCustomItem("trowel", trowel);

        return trowel;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack item = event.getItem();
        String customItemKey = CustomItem.getCustomItemKey(item);
        if (!"trowel".equals(customItemKey)) {
            return;
        }

        int row = item.getPersistentDataContainer().get(TROWEL_ROW, PersistentDataType.INTEGER);

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        if (player.isSneaking() && event.getAction().isLeftClick()) {
            int nextRow = ((row + 1) % 4);
            ItemStack handItem = inventory.getItemInMainHand();
            ItemMeta meta = handItem.getItemMeta();
            meta.getPersistentDataContainer().set(TROWEL_ROW, PersistentDataType.INTEGER, nextRow);
            handItem.setItemMeta(meta);
            player.sendMessage(Component.text("Cycled trowel to row ", NamedTextColor.GRAY).append(Component.text(nextRow + 1, NamedTextColor.WHITE)));
            event.setCancelled(true);
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.useInteractedBlock() == Event.Result.DENY) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);


        List<Integer> slots = new ArrayList<>();
        ItemStack[] storageContents = inventory.getStorageContents();

        int realRow = row == 3 ? 0 : row + 1;

        for (int i = 9 * realRow; i < 9 * (realRow + 1); i++) {
            ItemStack playerItem = storageContents[i];
            if (playerItem == null || playerItem.isEmpty() || !playerItem.getType().isBlock()) {
                continue;
            }

            slots.add(i);
        }
        if (slots.isEmpty()) {
            return;
        }

        int slot = slots.get(ThreadLocalRandom.current().nextInt(slots.size()));
        ItemStack selected = storageContents[slot];

        net.minecraft.world.item.ItemStack nmsItem = ((CraftItemStack) selected).handle;

        Block clicked = event.getClickedBlock();
        BlockFace face = event.getBlockFace();
        Block placed = clicked.getRelative(face);

        Location interaction = event.getInteractionPoint();

        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        event.getPlayer().getInventory().setItemInMainHand(selected);
        nmsPlayer.gameMode.useItemOn(nmsPlayer, nmsPlayer.level(), nmsItem, InteractionHand.MAIN_HAND,
            new BlockHitResult(new Vec3(interaction.x(), interaction.y(), interaction.z()), CraftBlockData.toNMS(event.getBlockFace(), Direction.class), new BlockPos(placed.getX(), placed.getY(), placed.getZ()), false, false));
        inventory.setItem(slot, inventory.getItemInMainHand());
        inventory.setItemInMainHand(hand);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
