package net.civmc.civproxy.renamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DisplayNameStorage {
    private final File storageFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Map<String, List<String>> displayNames; // UUID string -> list of names (chronological)

    public DisplayNameStorage(File dataFolder) {
        this.storageFile = new File(dataFolder, "display_names.json");
        loadFromFile();
    }

    private void loadFromFile() {
        if (!storageFile.exists()) {
            displayNames = new HashMap<>();
            return;
        }

        try (FileReader reader = new FileReader(storageFile)) {
            displayNames = gson.fromJson(reader, new TypeToken<Map<String, List<String>>>(){}.getType());
            if (displayNames == null) {
                displayNames = new HashMap<>();
            }
        } catch (IOException e) {
            displayNames = new HashMap<>();
            e.printStackTrace();
        }
    }

    public void setDisplayName(UUID uuid, String newName) {
        String uuidString = uuid.toString();
        displayNames.computeIfAbsent(uuidString, k -> new ArrayList<>()).add(newName);
        saveToFile();
    }

    public String getDisplayName(UUID uuid) {
        List<String> names = displayNames.get(uuid.toString());
        return names == null || names.isEmpty() ? null : names.get(0); // Original name
    }

    public String getCurrentDisplayName(UUID uuid) {
        List<String> names = displayNames.get(uuid.toString());
        return names == null || names.isEmpty() ? null : names.get(names.size() - 1); // Most recent name
    }

    public List<String> getDisplayNameHistory(UUID uuid) {
        return displayNames.getOrDefault(uuid.toString(), new ArrayList<>());
    }

    private void saveToFile() {
        storageFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(storageFile)) {
            gson.toJson(displayNames, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}