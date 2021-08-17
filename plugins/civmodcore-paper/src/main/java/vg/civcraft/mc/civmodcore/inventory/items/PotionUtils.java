package vg.civcraft.mc.civmodcore.inventory.items;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.minecraft.world.effect.MobEffectList;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

@UtilityClass
public final class PotionUtils {

	private static final Map<Pair<Material, PotionType>, TranslatableComponent> POTION_TRANSLATIONS = new Object2ObjectAVLTreeMap<>();

	/**
	 * @param type The potion type to get a translatable component for.
	 * @return Returns a translatable component for the given potion type.
	 */
	@Nonnull
	public static TranslatableComponent asTranslatable(@Nonnull final PotionType type) {
		return asTranslatable(Material.POTION, type);
	}

	/**
	 * @param material The potion kind. Must comply with {@link MoreTags#POTIONS}.
	 * @param type The potion type to get a translatable component for.
	 * @return Returns a translatable component for the given potion type.
	 */
	@Nonnull
	public static TranslatableComponent asTranslatable(@Nonnull final Material material,
													   @Nonnull final PotionType type) {
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

	/**
	 * @param effect The potion effect to get a translatable component for.
	 * @return Returns a translatable component for the given potion effect.
	 */
	@SuppressWarnings("deprecation")
	@Nonnull
	public static TranslatableComponent asTranslatable(@Nonnull final PotionEffectType effect) {
		final MobEffectList mojang = MobEffectList.fromId(effect.getId());
		assert mojang != null;
		final String key = mojang.c(); // Gets the translation key
		// If the obfuscation has changed, look for a method on MobEffectList like:
		//
		//    protected String b() {
		//        if (this.d == null) {
		//            this.d = SystemUtils.a("effect", IRegistry.V.getKey(this));
		//        }
		//        return this.d;
		//    }
		//
		// then use whatever method calls that method
		return Component.translatable(key);
	}

	/**
	 * @param potion The potion type to get the name of.
	 * @return Returns the name of the potion, or null.
	 *
	 * @deprecated Use {@link #asTranslatable(PotionType)} or
	 *             {@link #asTranslatable(Material, PotionType)} instead.
	 */
	@Deprecated
	@Nullable
	public static String getPotionNiceName(@Nullable final PotionType potion) {
		return potion == null ? null : ChatUtils.stringify(asTranslatable(potion));
	}

	/**
	 * @param effect The potion effect to get the name of.
	 * @return Returns the name of the potion effect, or null.
	 *
	 * @deprecated Use {@link #asTranslatable(PotionEffectType)} instead.
	 */
	@Deprecated
	@Nullable
	public static String getEffectNiceName(@Nullable final PotionEffectType effect) {
		return effect == null ? null : ChatUtils.stringify(asTranslatable(effect));
	}

}
