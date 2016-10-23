package vg.civcraft.mc.civmodcore.itemHandling;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.NBTTagList;

import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class TagReader {
	private NBTTagCompound tag;
	
	public TagReader(ItemStack is) {
		net.minecraft.server.v1_10_R1.ItemStack s = CraftItemStack.asNMSCopy(is);
		this.tag = s.getTag();
	}
	
	public String getString(String key) {
		return this.tag.getString(key);
	}
	
	public int getInt(String key) {
		return this.tag.getInt(key);
	}

	public byte getByte(String key) {
		return this.tag.getByte(key);
	}

	public List<String> getStringList(String key) {
		NBTTagList tagList = this.tag.getList(key, 8);
		List<String> list = new ArrayList<String>();
		
		for(int i = 0; i < tagList.size(); i++) {
			list.add(tagList.getString(i));
		}
		
		return list;
	}
}
