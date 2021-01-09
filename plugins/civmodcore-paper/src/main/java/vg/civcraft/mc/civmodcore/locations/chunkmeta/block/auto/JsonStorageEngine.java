package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.auto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class JsonStorageEngine<D extends JsonableDataObject<D>> extends AutoStorageEngine<D> {

	private static final JsonParser jsonParser = new JsonParser();

	public JsonStorageEngine(ManagedDatasource db, Logger logger,
			BiFunction<Location, JsonObject, D> dataDeserializer) {
		super(db, logger, (l, s) -> {
			return dataDeserializer.apply(l, (JsonObject)jsonParser.parse(s));
		});
	}

}
