package vg.civcraft.mc.civmodcore.inventory.items;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

public final class PotionUtils {
    private static final Map<Pair<Material, PotionType>, TranslatableComponent> POTION_TRANSLATIONS = new Object2ObjectAVLTreeMap<>();

    /**
     * @param type The potion type to get a translatable component for.
     * @return Returns a translatable component for the given potion type.
     */
    @NotNull
    public static TranslatableComponent asTranslatable(@NotNull final PotionType type) {
        return asTranslatable(Material.POTION, type);
    }

    /**
     * @param material The potion kind. Must comply with {@link MoreTags#POTIONS}.
     * @param type     The potion type to get a translatable component for.
     * @return Returns a translatable component for the given potion type.
     */
    @NotNull
    public static TranslatableComponent asTranslatable(@NotNull final Material material,
                                                       @NotNull final PotionType type) {
        if (!MoreTags.POTIONS.isTagged(material)) {
            throw new IllegalArgumentException("That is not a recognised potion material! [" + material.name() + "]");
        }
        return POTION_TRANSLATIONS.computeIfAbsent(Pair.of(material, type), (_key) -> {
            final var item = new ItemStack(material);
            ItemUtils.handleItemMeta(item, (PotionMeta meta) -> {
                meta.setBasePotionData(new PotionData(type, false, false));
                return true;
            });
            return Component.translatable(item.translationKey());
        });
    }
}
