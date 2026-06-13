package com.github.devotedmc.hiddenore.util;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.PlayerInventory;

public final class FakePlayer {

    private static final String NAME = "Spoof";

    private FakePlayer() {
    }

    public static Player create(final Location location, final ItemStack itemInHand) {
        final ItemStack heldItem = itemInHand == null ? ItemStack.empty() : itemInHand;
        final Player[] player = new Player[1];
        final PlayerInventory inventory = inventory(location, heldItem, player);
        player[0] = (Player) Proxy.newProxyInstance(FakePlayer.class.getClassLoader(), new Class<?>[]{Player.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getName", "getDisplayName", "getPlayerListName" -> NAME;
                case "getInventory" -> inventory;
                case "getLocation", "getEyeLocation" -> location;
                case "getWorld" -> location.getWorld();
                case "getServer" -> Bukkit.getServer();
                case "getUniqueId" -> UUID.nameUUIDFromBytes(("HiddenOre:" + location.getWorld().getUID()).getBytes());
                case "getActivePotionEffects" -> Collections.emptyList();
                case "getGameMode" -> GameMode.SURVIVAL;
                case "getMainHand" -> MainHand.RIGHT;
                case "getType" -> EntityType.PLAYER;
                default -> defaultValue(proxy, method, args);
            });
        return player[0];
    }

    private static PlayerInventory inventory(final Location location, final ItemStack heldItem, final Player[] player) {
        return (PlayerInventory) Proxy.newProxyInstance(FakePlayer.class.getClassLoader(), new Class<?>[]{PlayerInventory.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getItemInMainHand", "getItemInHand" -> heldItem;
                case "getItemInOffHand" -> ItemStack.empty();
                case "getLocation" -> location;
                case "getHolder" -> player[0];
                case "getContents", "getStorageContents", "getArmorContents", "getExtraContents" -> new ItemStack[0];
                case "getViewers" -> Collections.emptyList();
                case "getType" -> InventoryType.PLAYER;
                case "iterator" -> Collections.emptyList().listIterator();
                case "addItem", "removeItem", "all" -> new HashMap<Integer, ItemStack>();
                case "first", "firstEmpty" -> -1;
                case "getMaxStackSize" -> 64;
                default -> defaultValue(proxy, method, args);
            });
    }

    private static Object defaultValue(final Object proxy, final Method method, final Object[] args) {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "toString" -> NAME;
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> null;
            };
        }
        final Class<?> type = method.getReturnType();
        if (type == Void.TYPE) return null;
        if (type == Boolean.TYPE) return false;
        if (type == Byte.TYPE) return (byte) 0;
        if (type == Short.TYPE) return (short) 0;
        if (type == Integer.TYPE) return 0;
        if (type == Long.TYPE) return 0L;
        if (type == Float.TYPE) return 0.0f;
        if (type == Double.TYPE) return 0.0d;
        if (type == Character.TYPE) return '\0';
        if (Set.class.isAssignableFrom(type)) return Collections.emptySet();
        if (Collection.class.isAssignableFrom(type) || List.class.isAssignableFrom(type)) return Collections.emptyList();
        return null;
    }
}
