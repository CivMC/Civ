package vg.civcraft.mc.civchat2.zipper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.utility.CivChat2Config;
import vg.civcraft.mc.namelayer.NameAPI;

/**
 * @author jjj5311
 * 
 * Class to create, modify, save all chat log files
 *
 */

public class CivChat2FileLogger {
	
	private CivChat2 instance;
	private FileConfiguration fConfig;
	private static CivChat2Manager chatMan;
	private CivChat2Config config;
	private File toRecord;
	private File ignoredPlayers;
	public BufferedWriter fileWriter;
	public BufferedWriter writer;
	
	private String dateString; 
	private String chatDirectory;
	private String ignoreDirectory;
	
	/**
	 * Method to initialize variables, must be called before using any other methods from this class
	 */
	public void Init(){
		instance = CivChat2.getInstance();
		config = CivChat2.getPluginConfig();
		dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		chatDirectory = instance.getDataFolder() + File.separator + "ChatLogs" + File.separator;
		ignoreDirectory = instance.getDataFolder() + File.separator + "IgnoredLogs" + File.separator;
		checkDirectories(chatDirectory);
		checkDirectories(ignoreDirectory);
		initchatLog(chatDirectory + dateString + ".txt");
		initIgnoreLog(ignoreDirectory + "ignorelist.txt");
		serverStartup();
	}
	/**
	 * Method to create/check if directory exists
	 * @param chatDirectory2
	 */
	private void checkDirectories(String dir) {
		File directory = new File(dir);
		if(!directory.exists()){
			//directory does not exist create it
			instance.infoMessage("Creating new Directory for CivChat2");
			try{
				directory.mkdir();
			} catch (SecurityException e){
				instance.severeMessage("Error creating directory: " + e);
			}
		}
	}
	/**
	 * Method to create/get chat log text file
	 * @return true if file was created successfully, false if failed
	 */
	public void initchatLog(String filename){
		File existing = new File(filename);
		instance.debugmessage("Initializing chatlog... filename=[" + filename.toString() + "]");
		//first create chatlog file
		try{
			if(existing.exists()){
				//directory already exists
				instance.infoMessage("Existing Chat File: " + existing.getAbsolutePath());
				FileWriter fw = new FileWriter(existing.getAbsolutePath(), true);
				fileWriter = new BufferedWriter(fw);
			} else {
				instance.infoMessage("Creating new File" + existing.getAbsolutePath());
				existing.createNewFile();
				PrintWriter fStream = new PrintWriter(existing);
				fileWriter = new BufferedWriter(fStream);
				toRecord = existing;
			}
		
		} catch (IOException ex){
			instance.warningMessage("File Failed" + ex);
		}
	}
	
	/**
	 * Method to create/get ignoring players file
	 * @param filename
	 */
	public void initIgnoreLog(String filename){
		File existing = new File(filename);
		instance.debugmessage("Initializing IgnoreLog...");
		try{
			if(existing.exists()){
				//ignore file exists load it
				instance.infoMessage("Existing Ignore file: " + existing.getAbsolutePath());
				FileWriter fw = new FileWriter(existing, true);
				writer = new BufferedWriter(fw);
				ignoredPlayers = existing;
				loadIgnoredPlayers(ignoredPlayers);
			} else {
				//create new ignore file
				instance.infoMessage("Creating new Ignore file: " + existing.getAbsolutePath());
				existing.createNewFile();
				PrintWriter pw = new PrintWriter(existing);
				writer = new BufferedWriter(pw);
				ignoredPlayers = existing;
			}
		} catch (IOException ex){
			instance.warningMessage("File Failed: " + ex);
		}
	}
	
	/**
	 * Method to pass ignoredFile Contents to ChatManager
	 * @param file ignoredPlayers file
	 */
	private void loadIgnoredPlayers(File file) {
		try{
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
			HashMap<String, List<String>> ignoreListForChatMan = new HashMap<String, List<String>>();
			while ((line = br.readLine()) != null) {
				String parts[] = line.split(",");
				String owner = parts[0];
				List<String> participants = new ArrayList<>();
				for (int x = 1; x < parts.length; x++) {
					participants.add(parts[x]);
				}
				ignoreListForChatMan.put(owner, participants);
			}
			if(ignoreListForChatMan != null){
				instance.debugmessage("Loaded ignore list... [" + ignoreListForChatMan.size() + "] ignore entries");
				if(ignoreListForChatMan.size() != 0){
					chatMan.setIgnoredPlayer(ignoreListForChatMan);
				}
			}
		br.close();
		fis.close();
		} catch (IOException ex){
			instance.severeMessage("Could not read ignore file " + ex);
		}
	}
	
	
	/**
	 * Method to write text to the current chatlog
	 * @param sender Player sending the message
	 * @param msg The message you want to add to chatlog
	 * @param receive Player recieving the message
	 */
	public void writeToChatLog(Player sender, String msg, String type){
		String date = new SimpleDateFormat("dd-MM HH:mm:ss").format(new Date());
		String name = NameAPI.getCurrentName(sender.getUniqueId());
		String loc = (int) sender.getLocation().getX() + ", "
				+ (int) sender.getLocation().getY() + ", "
				+ (int) sender.getLocation().getZ();
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(date);
		sb.append("] ");
		sb.append("[");
		sb.append(loc);
		sb.append("] ");
		sb.append("[");
		sb.append(type);
		sb.append("] ");
		sb.append("[");
		sb.append(name);
		sb.append("] ");
		sb.append("[");
		sb.append(msg);
		sb.append("] ");
		try{
			fileWriter.write(msg);
		} catch (IOException ex){
			instance.severeMessage("Could not write to chatlog file " + ex);
		}
	}
	
	/**
	 * Method to run at shutdown
	 * Call this in plugin.onDisable()
	 */
	public void serverShutdown(){
		try{
			fileWriter.write("Server closed at " + new Date());
			fileWriter.newLine();
			fileWriter.newLine();
			fileWriter.flush();
			fileWriter.close();
			saveIgnoredFile(ignoredPlayers);
		} catch (IOException ex){
			instance.severeMessage("Could not write to chatlog file " + ex);
		}
	}
	
	/**
	 * Method to run at init
	 */
	private void serverStartup(){
		try{
			fileWriter.write("Chat log created at " + new Date());
			fileWriter.newLine();
			fileWriter.newLine();
			fileWriter.flush();
		} catch (IOException ex){
			instance.severeMessage("Could not write to chatlog file " + ex);
		}
	}
	
	/**
	 * Method to save the ignored users file text file
	 * @param toSave the File to save to
	 */
	public void saveIgnoredFile(File toSave){
		if(toSave == null){
			instance.debugmessage("toSave file is null.....");
			return;
		}
		instance.debugmessage("Saving ignoredFile [" + toSave.toString() + "]");
		try{
			FileOutputStream fos = new FileOutputStream(toSave);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));			
			HashMap<String, List<String>> ignoredList = chatMan.getIgnoredPlayer();
			Set<String> players = ignoredList.keySet();
			for(String player : players){
				bw.append(player + ",");
				for(String ignoredUser : ignoredList.get(player)){	
					bw.append(ignoredUser + ",");
				}
				bw.newLine();
			}
			bw.flush();
			fos.close();
		}catch (IOException ex){
			instance.severeMessage("Could not write to ignore file " + ex);
		}
	}
	
	/**
	 * Method to zipfiles and take care of directories
	 * @param date the current datestring
	 * @param directory the directory to manage
	 */
	private void fileManagement(String date, String directory){
		File[] filtered = toRecord.listFiles(new FilenameFilter() { 
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".txt"); 
				}
			});
		
		if(filtered != null && filtered.length > config.getFilesToZip()){
			instance.infoMessage("Zipping [" + filtered.length + "] Files");
			byte[] buffer = new byte[1024];
			try{
				instance.debugmessage("Currently Zipping Files");
				FileOutputStream fos = new FileOutputStream(chatDirectory + date + ".zip");
                ZipOutputStream zos = new ZipOutputStream(fos);
                for (File file : filtered) {
                    ZipEntry ze = new ZipEntry(file.toString());
                    zos.putNextEntry(ze);
                    int length;
                    FileInputStream in = new FileInputStream(file);
                    while ((length = in.read(buffer)) > 0){
                    	zos.write(buffer, 0 , length);
                    }
                    zos.closeEntry();
                    file.delete();
                    in.close();
                }
                zos.close();
			} catch (Exception e) {
            	instance.severeMessage("Error Zipping Files" + e);
            	return;
            }
			
			zipFile();
		}
		instance.debugmessage("Nothing to Zip");
	}
	
	private void zipFile(){
		File[] zipList = toRecord.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip");
            }
        });
        instance.infoMessage("zipList.length = " + zipList.length);
        if (zipList != null && zipList.length > config.getMaxNumberofZips()) {
            instance.infoMessage("Deleting zips...");
            long holder = 0;
            long tester;
            File toDelete = zipList[0];
            for (File file : zipList) {
                tester = file.lastModified();
                if (tester < holder) {
                    holder = tester;
                    toDelete = file;
                }
            }
            toDelete.delete();
            instance.infoMessage("Deleted: " + toDelete.getName());
        }
	}
	
	
}
