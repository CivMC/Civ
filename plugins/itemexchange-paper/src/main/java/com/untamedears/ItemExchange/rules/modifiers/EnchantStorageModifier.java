package com.untamedears.itemexchange.rules.modifiers;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.chain;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.Utilities;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import vg.civcraft.mc.civmodcore.api.EnchantAPI;
import vg.civcraft.mc.civmodcore.api.EnchantNames;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

public final class EnchantStorageModifier extends ModifierData {

    public static final String SLUG = "ENCHANT";

    public EnchantStorageModifier() {
        super(SLUG, 0);
    }

    @Override
    public boolean isValid() {
        return !this.nbt.isEmpty();
    }

    @Override
    public void trace(ItemStack item) {
        ItemAPI.handleItemMeta(item, (EnchantmentStorageMeta meta) -> {
            setEnchants(meta.getStoredEnchants());
            return false;
        });
    }

    @Override
    public boolean conforms(ItemStack item) {
        boolean[] conforms = { false };
        ItemAPI.handleItemMeta(item, (EnchantmentStorageMeta meta) -> {
            if (meta.hasStoredEnchants() != hasEnchants()) {
                return false;
            }
            Map<Enchantment, Integer> ruleEnchants = getEnchants();
            Map<Enchantment, Integer> metaEnchants = meta.getStoredEnchants();
            if (!Utilities.conformsRequiresEnchants(ruleEnchants, metaEnchants, false)) {
                return false;
            }
            conforms[0] = true;
            return false;
        });
        return conforms[0];
    }

    @Override
    public List<String> getDisplayedInfo() {
        if (!hasEnchants()) {
            return Collections.singletonList(ChatColor.DARK_AQUA + "No stored enchants.");
        }
        else {
            return Collections.singletonList(ChatColor.DARK_AQUA + "Stored enchants: " +
                    ChatColor.YELLOW + getEnchants().entrySet().stream().
                    filter((entry) -> EnchantAPI.isSafeEnchantment(entry.getKey(), entry.getValue())).
                    map((entry) -> NullCoalescing.chain(() ->
                            EnchantNames.findByEnchantment(entry.getKey()).getAbbreviation(), "UNKNOWN") +
                            entry.getValue()).
                    collect(Collectors.joining(" ")));
        }
    }

    public boolean hasEnchants() {
        return this.nbt.hasKey("bookEnchants");
    }

	@SuppressWarnings("deprecation")
    public Map<Enchantment, Integer> getEnchants() {
        return Arrays.
                stream(this.nbt.getCompoundArray("bookEnchants")).
                collect(Collectors.toMap(
                		(nbt) -> Enchantment.getByName(nbt.getString("enchant")),
						(nbt) -> nbt.getInteger("level")));
    }

	@SuppressWarnings("deprecation")
    public void setEnchants(Map<Enchantment, Integer> enchants) {
        this.nbt.setCompoundArray("bookEnchants", chain(() -> enchants.entrySet().stream().
                map(entry -> new NBTCompound() {{
                    setString("enchant", chain(() -> entry.getKey().getName()));
                    setInteger("level", entry.getValue());
                }}).
                toArray(NBTCompound[]::new)));
    }

    public static ModifierData fromItem(ItemStack item) {
        if (item.getType() == Material.ENCHANTED_BOOK) {
            EnchantStorageModifier modifier = new EnchantStorageModifier();
            modifier.trace(item);
            return modifier;
        }
        return null;
    }

}
