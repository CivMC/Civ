package vg.civcraft.mc.namelayer.misc;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

//All credit for this code to evilmidget38
//Original source: https://gist.github.com/evilmidget38/a5c971d2f2b2c3b3fb37

public class NameFetcher implements Callable<Map<UUID, String>> {
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private final JsonParser jsonParser = new JsonParser();
    private final List<UUID> uuids;
    public NameFetcher(List<UUID> uuids) {
        this.uuids = ImmutableList.copyOf(uuids);
    }

    @Override
    public Map<UUID, String> call() throws Exception {
        Map<UUID, String> uuidStringMap = new HashMap<>();
        for (UUID uuid: uuids) {
            HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL+uuid.toString().replace("-", "")).openConnection();
            JsonObject response = (JsonObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            String name = response.get("name").getAsString();
            if (name == null) {
                continue;
            }
            JsonElement cause = response.get("cause");
            JsonElement errorMessage = response.get("errorMessage");
            if (cause != null && cause.toString().length() > 0) {
                throw new IllegalStateException(errorMessage.getAsString());
            }
            uuidStringMap.put(uuid, name);
        }
        return uuidStringMap;
    }
}
