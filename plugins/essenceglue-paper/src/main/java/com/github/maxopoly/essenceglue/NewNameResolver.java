package com.github.maxopoly.essenceglue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NewNameResolver {

	private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";
	private static final JsonParser jsonParser = new JsonParser();

	public static UUID getUUIDForMojangName(String userName) {
		JsonObject response;
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) new URL(String.format(PROFILE_URL, userName, System.currentTimeMillis() / 1000)).openConnection();
			response = (JsonObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
		} catch (IOException e) {
			EssenceGluePlugin.instance().getLogger().log(Level.SEVERE, "Failed to lookup current mojang name", e);
			return null;
		}
		String uuidASString = response.get("id").getAsString();
		if (uuidASString == null) {
			return null;
		}
		//add dashes into uuid
		uuidASString = uuidASString.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
		return UUID.fromString(uuidASString);
	}

}
