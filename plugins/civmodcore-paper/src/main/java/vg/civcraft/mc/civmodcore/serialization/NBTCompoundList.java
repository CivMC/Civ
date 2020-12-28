package vg.civcraft.mc.civmodcore.serialization;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import org.apache.commons.lang.reflect.FieldUtils;
import vg.civcraft.mc.civmodcore.util.Validation;

/**
 * <p>Represents a list of nbt class serializable elements of the same type.</p>
 *
 * <p>Read More:</p>
 * <ul>
 *     <li>{@link NBTSerialization}</li>
 *     <li>{@link NBTSerialization#serialize(NBTSerializable)}</li>
 *     <li>{@link NBTSerialization#deserialize(NBTCompound)}</li>
 * </ul>
 */
public class NBTCompoundList<T extends NBTSerializable> extends ArrayList<T> {

	/**
	 * Serializes each element into an {@link NBTTagList}.
	 *
	 * @return Returns a populated {@link NBTTagList}.
	 */
	public NBTTagList serialize() {
		NBTTagList list = new NBTTagList();
		List<NBTBase> inner = getInnerList(list);
		stream().filter(Objects::nonNull)
				.map(NBTSerialization::serialize)
				.filter(Validation::checkValidity)
				.map(NBTCompound::getRAW)
				.forEachOrdered(inner::add);
		return list;
	}

	/**
	 * Converts a {@link NBTTagList} into an NBTCompoundList, deserializing each element to
	 * the given generic type.
	 *
	 * @param <T> The generic type each element should be cast to.
	 * @param list The {@link NBTTagList} to convert into NBTCompoundList.
	 * @return Returns a new NBTCompoundList.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends NBTSerializable> NBTCompoundList<T> deserialize(NBTTagList list) {
		NBTCompoundList<T> wrapper = new NBTCompoundList<>();
		if (list == null) {
			return wrapper;
		}
		if (list.d_() != 10) {
			return wrapper;
		}
		getInnerList(list).stream()
				.map(nbt -> chain(() -> new NBTCompound((NBTTagCompound) nbt)))
				.map(nbt -> chain(() -> (T) NBTSerialization.deserialize(nbt)))
				.filter(Objects::nonNull)
				.forEachOrdered(wrapper::add);
		return wrapper;
	}

	@SuppressWarnings("unchecked")
	private static List<NBTBase> getInnerList(NBTTagList list) {
		try {
			return  (List<NBTBase>) FieldUtils.readField(list, "list", true);
		}
		catch (Exception exception) {
			throw new NBTSerializationException(
					"Could not encode NBTCompound list to NBTTagList: could not access inner list of NBTTagList.",
					exception);
		}
	}

}
