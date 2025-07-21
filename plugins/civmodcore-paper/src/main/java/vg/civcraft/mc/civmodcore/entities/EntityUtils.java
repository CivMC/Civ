package vg.civcraft.mc.civmodcore.entities;

import com.google.common.base.Strings;
import org.bukkit.entity.EntityType;

/**
 * Class of static APIs for Entities.
 */
public final class EntityUtils {

    /**
     * Attempts to retrieve an entity type by its slug.
     *
     * @param value The value to search for a matching entity type by.
     * @return Returns a matched entity type or null.
     */
    @SuppressWarnings("deprecation")
    public static EntityType getEntityType(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        try {
            return EntityType.valueOf(value.toUpperCase());
        } catch (final Throwable ignored) {
        }
        return null;
    }
}
