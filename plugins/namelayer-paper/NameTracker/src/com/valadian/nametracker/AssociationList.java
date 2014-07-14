package com.valadian.nametracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

class Association {
	UUID uuid;
	String CurrentName;
	Set<String> AllNames;

	public Association(UUID uuid, String CurrentName)
	{
		this.uuid = uuid;
		this.CurrentName = CurrentName;
		this.AllNames = new HashSet<String>();
		AllNames.add(CurrentName);
	}
	public Association(UUID uuid, String CurrentName, Set<String> AllNames)
	{
		this.uuid = uuid;
		this.CurrentName = CurrentName;
		this.AllNames = AllNames;
	}
	public Association(String line)
	{
		if (line.length() > 1) {
			String[] parts = line.split(" ");
			uuid = UUID.fromString(parts[0]);
			CurrentName = parts[1];
			String[] names = Arrays.copyOfRange(parts, 1, parts.length);
			AllNames = new HashSet<String>();
			AllNames.addAll(Arrays.asList(names));
		}
	}
	
	public void add(String name){
		if(!AllNames.contains(name)){
			AllNames.add(name);
			//CurrentName = name;
			//dirty = true;
		}
	}
	
	public String toString(){
		String value = uuid.toString() + " " + CurrentName;
		for(String name: AllNames) {
			value+=" "+name;
		}
		return value;
	}
	
	public boolean contains(String name) {
		return AllNames.contains(name);
	}
}

class AssociationList {
	File uuidFile;
	private HashMap<UUID, Association> associationHash;
	private boolean initialised = false;


	private boolean dirty = false;
	public boolean isDirty() {
		return dirty;
	}
	public void markDirty() {
		dirty = true;
	}
	
	public AssociationList(File dataFolder){
		 uuidFile =  new File(dataFolder, "uuids.txt");
		 load();
	}
	
	private void load() {
		try {
			loadAssociations();
			initialised = true;
		} catch (IOException e) {
			e.printStackTrace();
			Bukkit.getLogger().info("Failed to load file!");
			initialised = false;
		}
	}
	public void save() throws IOException {
		if(!dirty) return;
		
		FileOutputStream fos = new FileOutputStream(uuidFile);
		BufferedWriter br = new BufferedWriter(new OutputStreamWriter(fos));
		
		for (UUID uuid : getAllUUIDs()) {
			br.append(uuid.toString());
			for(String name : getAllNames(uuid)) {
				br.append(" ");
				br.append(name);
			}
			br.append("\n");
		}
		
		br.flush();
		fos.close();
	}
	private void loadAssociations() throws IOException {
		dirty = false;
		associationHash = new HashMap<UUID, Association>();
		FileInputStream fis;
		fis = new FileInputStream(uuidFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.length() > 1) {
				Association assoc = new Association(line);
				associationHash.put(assoc.uuid, assoc);
			}
		}
		
		br.close();
	}
	
	public Set<UUID> getAllUUIDs() {
		return associationHash.keySet();
	}
	
	public void addPlayerName(UUID uuid, String playerName){
		if(associationHash.containsKey(playerName)){
			Association assoc = associationHash.get(playerName);
			if(!assoc.contains(playerName)) {
				assoc.add(playerName);
				dirty = true;
			}
		}
		else {
			Association assoc = new Association(uuid, playerName);
			associationHash.put(uuid, assoc);
			dirty = true;
		}
	}
	
	public UUID getUUID(String playerName) {
		for(Association assoc : associationHash.values()) {
			for (String name : assoc.AllNames) {
				if(name.equals(playerName)) {
					return assoc.uuid;
				}
			}
		}
		return null;
	}
	
	public Set<String> getAllNames(UUID uuid){
		if (initialised && associationHash.containsKey(uuid)) {
			return associationHash.get(uuid).AllNames;
		}
		return new HashSet<String>();
	}
	
	public String getCurrentName(UUID uuid) {
		if (initialised && associationHash.containsKey(uuid)) {
			return associationHash.get(uuid).CurrentName;
		}
		return null;
		
	}
}
