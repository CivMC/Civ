package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.auto;

import java.util.function.BiFunction;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class YamlStorageEngine<D extends YamlDataObject<D>> extends AutoStorageEngine<D> {

	public YamlStorageEngine(ManagedDatasource db, Logger logger,
			BiFunction<Location, YamlConfiguration, D> dataDeserializer) {
		super(db, logger, (l, s) -> {
			YamlConfiguration yaml = new YamlConfiguration();
			try {
				yaml.loadFromString(s);
			} catch (InvalidConfigurationException e) {
				logger.severe("Failed to decode yaml data " + e);
			}
			return dataDeserializer.apply(l,yaml);
		});
	}

}
