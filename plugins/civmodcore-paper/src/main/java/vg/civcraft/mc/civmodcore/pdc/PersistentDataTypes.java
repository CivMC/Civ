package vg.civcraft.mc.civmodcore.pdc;

import javax.annotation.Nonnull;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

@UtilityClass
public final class PersistentDataTypes {
	public static final String DECODER_ERROR = "Was unable to decode that %s! [%s]";

	/**
	 * Boolean data type... because <i>believe it or not</i> but PDC doesn't already have this ಠ_ಠ
	 */
	public static final PersistentDataType<Byte, Boolean> BOOLEAN = new AbstractPersistentDataType<>(Byte.class, Boolean.class) {
		@Nonnull
		@Override
		public Byte toPrimitive(@Nonnull final Boolean bool,
								@Nonnull final PersistentDataAdapterContext adapter) {
			return (byte) (bool ? 1 : 0);
		}
		@Nonnull
		@Override
		public Boolean fromPrimitive(@Nonnull final Byte raw,
									 @Nonnull final PersistentDataAdapterContext adapter) {
			return raw != (byte) 0;
		}
	};

	/**
	 * Converts Components to Strings and vice versa.
	 */
	public static final PersistentDataType<String, Component> COMPONENT = new AbstractPersistentDataType<>(String.class, Component.class) {
		@Nonnull
		@Override
		public String toPrimitive(@Nonnull final Component component,
								  @Nonnull final PersistentDataAdapterContext adapter) {
			return GsonComponentSerializer.gson().serialize(component);
		}
		@Nonnull
		@Override
		public Component fromPrimitive(@Nonnull final String raw,
									   @Nonnull final PersistentDataAdapterContext adapter) {
			return GsonComponentSerializer.gson().deserialize(raw);
		}
	};

}
