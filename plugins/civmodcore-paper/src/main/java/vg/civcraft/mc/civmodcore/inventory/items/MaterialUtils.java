package vg.civcraft.mc.civmodcore.inventory.items;

import com.destroystokyo.paper.MaterialTags;
import com.google.common.math.IntMath;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>See <a href="https://github.com/Protonull/BukkitReport/tree/master/reports">BukkitReports</a>.</p>
 *
 * <ul>
 *     <li>See also:</li>
 *     <li>{@link MoreTags MoreTags}</li>
 * </ul>
 */
public final class MaterialUtils {

    private static final List<Material> HASH_MATERIALS = new ArrayList<>() {{
        addAll(Tag.WOOL.getValues());
        addAll(MaterialTags.STAINED_GLASS.getValues());
        addAll(MaterialTags.STAINED_GLASS_PANES.getValues());
        addAll(MaterialTags.CONCRETES.getValues());
    }};

    /**
     * Attempts to retrieve a material by its slug.
     *
     * @param value The value to search for a matching material by.
     * @return Returns a matched material or null.
     */
    @Nullable
    public static Material getMaterial(@Nullable final String value) {
        return StringUtils.isEmpty(value) ? null : Material.getMaterial(value.toUpperCase());
    }

    /**
     * Checks whether a material is air.
     * Will also return true if the given material is null.
     *
     * @param material The material to check.
     * @return Returns true if the material is air.
     */
    public static boolean isAir(@Nullable final Material material) {
        return material == null || material.isAir();
    }

    /**
     * Gets a random material based on the given objects hashcode.
     *
     * @param object Object to base returned material on
     * @return Material hash of the given object
     */
    @NotNull
    public static Material getMaterialHash(@Nullable final Object object) {
        if (object == null) {
            return HASH_MATERIALS.get(0);
        }
        final int index = IntMath.mod(object.hashCode(), HASH_MATERIALS.size());
        return HASH_MATERIALS.get(index);
    }

}
