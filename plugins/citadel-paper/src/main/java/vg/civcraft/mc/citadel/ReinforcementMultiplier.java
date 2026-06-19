package vg.civcraft.mc.citadel;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ReinforcementMultiplier {

    private final Map<String, List<ReinforcementMultiplierConfig>> multipliers;

    private ReinforcementMultiplier(Map<String, List<ReinforcementMultiplierConfig>> multipliers) {
        this.multipliers = multipliers;
    }

    public static ReinforcementMultiplier parse(List<Map<?, ?>> mapList) {
        Map<String, List<ReinforcementMultiplierConfig>> multipliers = new HashMap<>();
        for (Map<?, ?> map : mapList) {
            String name = (String) map.get("custom-key");
            List<ReinforcementMultiplierConfig> multiplierList = new ArrayList<>();
            for (Map<String, Object> multiplier : (List<Map<String, Object>>) map.get("multipliers")) {
                Set<Material> setTypes = new HashSet<>();
                Object types = multiplier.get("types");
                if (types != null) {
                    for (String type : (List<String>) types) {
                        setTypes.add(Objects.requireNonNull(Material.matchMaterial(type)));
                    }
                }

                Set<Tag<Material>> setTags = new HashSet<>();
                Object tags = multiplier.get("tags");
                if (tags != null) {
                    for (String tag : (List<String>) tags) {
                        setTags.add(Objects.requireNonNull(Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(tag.toLowerCase()), Material.class)));
                    }
                }


                multiplierList.add(new ReinforcementMultiplierConfig(
                    ((Number) multiplier.get("multiplier")).doubleValue(),
                    setTypes,
                    setTags
                ));
            }
            multipliers.put(name, multiplierList);
        }
        return new ReinforcementMultiplier(multipliers);
    }

    public double getMultiplier(Material type, ItemStack item) {
        String customItemKey = CustomItem.getCustomItemKey(item);
        if (customItemKey == null) {
            return 1;
        }
        List<ReinforcementMultiplierConfig> list = multipliers.get(customItemKey);
        if (list == null) {
            return 1;
        }
        for (ReinforcementMultiplierConfig config : list) {
            for (Material m : config.mats()) {
                if (m == type) {
                    return config.multiplier();
                }
            }
            for (Tag<Material> t : config.tags()) {
                if (t.isTagged(type)) {
                    return config.multiplier();
                }
            }
        }
        return 1;
    }
}

record ReinforcementMultiplierConfig(double multiplier, Set<Material> mats, Set<Tag<Material>> tags) {

}
