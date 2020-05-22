package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.auto;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class YamlDataObject <D extends YamlDataObject<D>> extends SerializableDataObject<D> {

	public YamlDataObject(Location location, boolean isNew) {
		super(location, isNew);
	}
	
	protected abstract void concreteSerialize(YamlConfiguration config);

	@Override
	public String serialize() {
		YamlConfiguration config = new YamlConfiguration();
		concreteSerialize(config);
		return config.saveToString();
	}

}
