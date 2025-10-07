package vg.civcraft.mc.civmodcore.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InventoryUtils {

    public static final int CHEST_1_ROW = 9;
    public static final int CHEST_2_ROWS = 9 * 2;
    public static final int CHEST_3_ROWS = 9 * 3;
    public static final int CHEST_4_ROWS = 9 * 4;
    public static final int CHEST_5_ROWS = 9 * 5;
    public static final int CHEST_6_ROWS = 9 * 6;

    /**
     * Tests an inventory to see if it's valid.
     *
     * @param inventory The inventory to test.
     * @return Returns true if it's more likely than not valid.
     */
    public static boolean isValidInventory(@Nullable final Inventory inventory) {
        return inventory != null
            && inventory.getSize() > 0;
    }

    /**
     * Get the players viewing an inventory.
     *
     * @param inventory The inventory to get the viewers of.
     * @return Returns a list of players. Returns an empty list if the inventory is null.
     * @deprecated Use Java 16's instanceof pattern matching instead.
     */
    @Deprecated
    public static List<Player> getViewingPlayers(@Nullable final Inventory inventory) {
        if (!isValidInventory(inventory)) {
            return new ArrayList<>(0);
        }
        return inventory.getViewers().stream()
            .filter(entity -> entity instanceof Player)
            .map(player -> (Player) player)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Contract("!null -> !null")
    public static @Nullable ItemStack @Nullable [] clone(
        @Nullable ItemStack [] contents
    ) {
        if (contents == null) {
            return null;
        }
        contents = contents.clone();
        for (int i = 0; i < contents.length; i++) {
            final ItemStack item = contents[i];
            if (item != null) { /// Oh, how I yearn for optional chaining
                contents[i] = item.clone();
            }
        }
        return contents;
    }

    public static void clearContents(
        final @Nullable ItemStack @NotNull [] contents
    ) {
        Arrays.fill(contents, null);
    }

    public static void fillContents(
        final @Nullable ItemStack @NotNull [] contents,
        final @NotNull IntFunction<@NotNull ItemStack> itemSupplier
    ) {
        for (int i = 0; i < contents.length; i++) {
            contents[i] = itemSupplier.apply(i);
        }
    }

    /**
     * Checks whether an inventory has more than one viewer.
     *
     * @param inventory The inventory to check.
     * @return Returns true if an inventory has multiple viewers.
     */
    public static boolean hasOtherViewers(@Nullable final Inventory inventory) {
        return inventory != null && inventory.getViewers().size() > 1;
    }

    /**
     * Checks whether a certain amount of slots would be considered a valid chest inventory.
     *
     * @param slots The slot amount to check.
     * @return Returns true if the slot count is or between 1-6 multiples of 9.
     */
    public static boolean isValidChestSize(final int slots) {
        return slots > 0
            && slots <= 54
            && (slots % 9) == 0;
    }
}
