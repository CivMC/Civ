package vg.civcraft.mc.civmodcore.itemHandling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_13_R2.NBTBase;
import net.minecraft.server.v1_13_R2.NBTTagByte;
import net.minecraft.server.v1_13_R2.NBTTagByteArray;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagDouble;
import net.minecraft.server.v1_13_R2.NBTTagFloat;
import net.minecraft.server.v1_13_R2.NBTTagInt;
import net.minecraft.server.v1_13_R2.NBTTagIntArray;
import net.minecraft.server.v1_13_R2.NBTTagList;
import net.minecraft.server.v1_13_R2.NBTTagLong;
import net.minecraft.server.v1_13_R2.NBTTagShort;
import net.minecraft.server.v1_13_R2.NBTTagString;

public class TagManager {
	private static final Logger log = Bukkit.getLogger();

	private NBTTagCompound tag;

	public TagManager() {
		this.tag = new NBTTagCompound();
	}

	public TagManager(ItemStack is) {
		if (is == null) {
			throw new IllegalArgumentException("Expected item stack parameter but NULL passed.");
		}

		net.minecraft.server.v1_13_R2.ItemStack s = CraftItemStack.asNMSCopy(is);
		this.tag = s.getTag();

		if (this.tag == null) {
			this.tag = new NBTTagCompound();
		}
	}

	private TagManager(NBTTagCompound tag) {
		this.tag = tag;
	}

	public String getString(String key) {
		return this.tag.getString(key);
	}

	public void setString(String key, String value) {
		this.tag.setString(key, value);
	}

	public int getInt(String key) {
		return this.tag.getInt(key);
	}

	public void setInt(String key, int value) {
		this.tag.setInt(key, value);
	}

	public short getShort(String key) {
		return this.tag.getShort(key);
	}

	public void setShort(String key, short value) {
		this.tag.setShort(key, value);
	}

	public byte getByte(String key) {
		return this.tag.getByte(key);
	}

	public void setByte(String key, byte value) {
		this.tag.setByte(key, value);
	}

	public List<String> getStringList(String key) {
		NBTTagList tagList = this.tag.getList(key, 8);
		List<String> list = new ArrayList<String>();

		for (int i = 0; i < tagList.size(); i++) {
			list.add(tagList.getString(i));
		}

		return list;
	}

	public void setStringList(String key, List<String> list) {
		NBTTagList tagList = new NBTTagList();

		for (String s : list) {
			tagList.add(new NBTTagString(s));
		}

		this.tag.set(key, tagList);
	}

	public List<Integer> getIntList(String key) {
		NBTTagList tagList = this.tag.getList(key, 3);
		List<Integer> list = new ArrayList<Integer>();

		for (int i = 0; i < tagList.size(); i++) {
			list.add(tagList.h(i));
		}

		return list;
	}

	public void setIntList(String key, List<Integer> list) {
		NBTTagList tagList = new NBTTagList();

		for (Integer i : list) {
			tagList.add(new NBTTagInt(i));
		}

		this.tag.set(key, tagList);
	}

	/**
	 * This wasn't really deprecated so much as they just didn't put in the short accessor to the
	 * tag list (not sure if they forgot or what, but it used to be missing)
	 * This basically copies the same implementation as the getIntList for shorts
	 *  Note: You need to decompile or use an IDE to get the function name that is the 'short' accessor
	 *        in this code when a new version is made
	 *
	 * So this is the old comment, I am keeping it for posterity in case it is removed again
	 * Reference the git history to see the old implementation, have fun wit hteh deobfuscation
	 *
	 * As of 1.12, the base NBTTagList has no accessor specific for Shorts, so we'll mark it deprecated here.
	 * Weirdly, the superclass has a method f() that still returns a Short for all number types, but NBTNumber isn't
	 * visible so ... hack it is.
	 * 
	 * @param key
	 * @return
	 */
	@Deprecated
	public List<Short> getShortList(String key) {
		NBTTagList tagList = this.tag.getList(key, 2);
		List<Short> list = new ArrayList<Short>();

		for (int i = 0; i < tagList.size(); ++i) {
			list.add(tagList.g(i));
		}

		return list;
	}

	/**
	 * Deprecating this as well as of 1.12, even though technically it is still supported (nothing prevents the creation)
	 * however since accessing a short list _is_, so should writing.
	 * 
	 * @param key
	 * @param list
	 */
	@Deprecated
	public void setShortList(String key, List<Short> list) {
		NBTTagList tagList = new NBTTagList();

		for (Short s : list) {
			tagList.add(new NBTTagShort(s));
		}

		this.tag.set(key, tagList);
	}

	public TagManager getCompound(String key) {
		return new TagManager(this.tag.getCompound(key));
	}

	public void setCompound(String key, TagManager tag) {
		this.tag.set(key, tag.tag);
	}

	public ItemStack enrichWithNBT(ItemStack is) {

		net.minecraft.server.v1_13_R2.ItemStack s = CraftItemStack.asNMSCopy(is);

		if (s == null) {
			log.severe("Failed to create enriched copy of " + is.toString());
			return null;
		}

		s.setTag(this.tag);

		return CraftItemStack.asBukkitCopy(s);
	}

	public void setMap(Map<String, Object> map) {
		mapToNBT(this.tag, map);
	}

	public void setList(String key, List<Object> list) {
		this.tag.set(key, listToNBT(new NBTTagList(), list));
	}

	@SuppressWarnings("unchecked")
	public static NBTTagCompound mapToNBT(NBTTagCompound base, Map<String, Object> map) {
		log.info("Representing map --> NBTTagCompound");
		if (map == null || base == null) {
			return base;
		}
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object object = entry.getValue();
			if (object instanceof Map) {
				log.info("Adding map at key " + entry.getKey());
				base.set(entry.getKey(), mapToNBT(new NBTTagCompound(), (Map<String, Object>) object));
			} else if (object instanceof MemorySection) {
				log.info("Adding map from MemorySection at key " + entry.getKey());
				base.set(entry.getKey(), mapToNBT(new NBTTagCompound(), ((MemorySection) object).getValues(true)));
			} else if (object instanceof List) {
				log.info("Adding list at key " + entry.getKey());
				base.set(entry.getKey(), listToNBT(new NBTTagList(), (List<Object>) object));
			} else if (object instanceof String) {
				log.info("Adding String " + object + " at key " + entry.getKey());
				base.setString(entry.getKey(), (String) object);
			} else if (object instanceof Double) {
				log.info("Adding Double " + object + " at key " + entry.getKey());
				base.setDouble(entry.getKey(), (Double) object);
			} else if (object instanceof Float) {
				log.info("Adding Float " + object + " at key " + entry.getKey());
				base.setFloat(entry.getKey(), (Float) object);
			} else if (object instanceof Boolean) {
				log.info("Adding Boolean " + object + " at key " + entry.getKey());
				base.setBoolean(entry.getKey(), (Boolean) object);
			} else if (object instanceof Byte) {
				log.info("Adding Byte " + object + " at key " + entry.getKey());
				base.setByte(entry.getKey(), (Byte) object);
			} else if (object instanceof Short) {
				log.info("Adding Byte " + object + " at key " + entry.getKey());
				base.setShort(entry.getKey(), (Short) object);
			} else if (object instanceof Integer) {
				log.info("Adding Integer " + object + " at key " + entry.getKey());
				base.setInt(entry.getKey(), (Integer) object);
			} else if (object instanceof Long) {
				log.info("Adding Long " + object + " at key " + entry.getKey());
				base.setLong(entry.getKey(), (Long) object);
			} else if (object instanceof byte[]) {
				log.info("Adding bytearray at key " + entry.getKey());
				base.setByteArray(entry.getKey(), (byte[]) object);
			} else if (object instanceof int[]) {
				log.info("Adding intarray at key " + entry.getKey());
				base.setIntArray(entry.getKey(), (int[]) object);
			} else if (object instanceof UUID) {
				log.info("Adding UUID " + object + " at key " + entry.getKey());
				base.a(entry.getKey(), (UUID) object);
			} else if (object instanceof NBTBase) {
				log.info("Adding nbtobject at key " + entry.getKey());
				base.set(entry.getKey(), (NBTBase) object);
			} else {
				log.warning("Unrecognized entry in map-->NBT: " + object.toString());
			}
		}
		return base;
	}

	@SuppressWarnings("unchecked")
	public static NBTTagList listToNBT(NBTTagList base, List<Object> list) {
		log.info("Representing list --> NBTTagList");
		if (list == null || base == null) {
			return base;
		}
		for (Object object : list) {
			if (object instanceof Map) {
				log.info("Adding map to list");
				base.add(mapToNBT(new NBTTagCompound(), (Map<String, Object>) object));
			} else if (object instanceof MemorySection) {
				log.info("Adding map from MemorySection to list");
				base.add(mapToNBT(new NBTTagCompound(), ((MemorySection) object).getValues(true)));
			} else if (object instanceof List) {
				log.info("Adding list to list");
				base.add(listToNBT(new NBTTagList(), (List<Object>) object));
			} else if (object instanceof String) {
				log.info("Adding string " + object + " to list");
				base.add(new NBTTagString((String) object));
			} else if (object instanceof Double) {
				log.info("Adding double " + object + " to list");
				base.add(new NBTTagDouble((Double) object));
			} else if (object instanceof Float) {
				log.info("Adding float " + object + " to list");
				base.add(new NBTTagFloat((Float) object));
			} else if (object instanceof Byte) {
				log.info("Adding byte " + object + " to list");
				base.add(new NBTTagByte((Byte) object));
			} else if (object instanceof Short) {
				log.info("Adding short " + object + " to list");
				base.add(new NBTTagShort((Short) object));
			} else if (object instanceof Integer) {
				log.info("Adding integer " + object + " to list");
				base.add(new NBTTagInt((Integer) object));
			} else if (object instanceof Long) {
				log.info("Adding long " + object + " to list");
				base.add(new NBTTagLong((Long) object));
			} else if (object instanceof byte[]) {
				log.info("Adding byte array to list");
				base.add(new NBTTagByteArray((byte[]) object));
			} else if (object instanceof int[]) {
				log.info("Adding int array to list");
				base.add(new NBTTagIntArray((int[]) object));
			} else if (object instanceof NBTBase) {
				log.info("Adding nbt object to list");
				base.add((NBTBase) object);
			} else {
				log.warning("Unrecognized entry in list-->NBT: " + base.toString());
			}
		}
		return base;
	}
}
