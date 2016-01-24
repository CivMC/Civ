package vg.civcraft.mc.civchat2.zipper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bukkit.Bukkit;
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
	private CivChat2Manager chatMan;
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
		config = instance.getPluginConfig();
		dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		chatDirectory = instance.getDataFolder() + File.separator + "ChatLogs" + File.separator;
		ignoreDirectory = instance.getDataFolder() + File.separator + "IgnoredLogs" + File.separator;
		toRecord = new File(chatDirectory);
		chatMan = instance.getCivChat2Manager();
		checkDirectories(chatDirectory);
		checkDirectories(ignoreDirectory);
		initchatLog(chatDirectory + dateString + ".txt");
		fileManagement(dateString, chatDirectory);
	}
	/**
	 * Method to create/check if directory exists
	 * @param chatDirectory2
	 */
	private void checkDirectories(String dir) {
		File directory = new File(dir);
		if(!directory.exists()){
			//directory does not exist create it
			CivChat2.infoMessage("Creating new Directory...");
			try{
				directory.mkdir();
			} catch (SecurityException e){
				CivChat2.severeMessage("Error creating directory: " + e);
			}
		}
	}
	/**
	 * Method to create/get chat log text file
	 * @return true if file was created successfully, false if failed
	 */
	public void initchatLog(String filename){
		StringBuilder sb = new StringBuilder();
		File existing = new File(filename);
		CivChat2.debugmessage(sb.append("Initializing chatlog... filename=[" )
								.append( filename.toString()) 
								.append( "]")
								.toString());
		sb.delete(0, sb.length());
		//first create chatlog file
		try{
			if(existing.exists()){
				//directory already exists
				CivChat2.infoMessage(sb.append("Existing Chat File: ")
										.append( existing.getAbsolutePath())
										.toString());
				sb.delete(0, sb.length());
				FileWriter fw = new FileWriter(existing.getAbsolutePath(), true);
				fileWriter = new BufferedWriter(fw);
			} else {
				CivChat2.infoMessage(sb.append("Creating new File")
										.append( existing.getAbsolutePath())
										.toString());
				sb.delete(0, sb.length());
				existing.createNewFile();
				PrintWriter fStream = new PrintWriter(existing);
				fileWriter = new BufferedWriter(fStream);
				addHeader();
			}
		
		} catch (IOException ex){
			CivChat2.warningMessage("File Failed" + ex);
		}
	}
	
	private void addIgnoreHeader() {
		try {
			writer.write("Ignore List Format: <OwnerName>,<Ignoree1>,<Ignoree....>,...,<GROUP(groupname)>");
			writer.flush();
		} catch (IOException e) {
			CivChat2.severeMessage("Error writing to file " + e);
		}
		
		
	}
	/**
	 * Method to write text to the current chatlog
	 * @param sender Player sending the message
	 * @param msg The message you want to add to chatlog
	 * @param receive Player recieving the message
	 */
	public void writeToChatLog(String sender, String msg, String type){
		String date = new SimpleDateFormat("dd-MM HH:mm:ss").format(new Date());
		Player player = Bukkit.getPlayer(sender);
		String name, loc;
		if (player != null) {
		name = NameAPI.getCurrentName(player.getUniqueId());
		loc = (int) player.getLocation().getX() + ", "
				+ (int) player.getLocation().getY() + ", "
				+ (int) player.getLocation().getZ();
		}
		else {
			name = sender;
			loc = "Not on this server";
		}
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
		sb.append("\n");
		String writeMsg = sb.toString();
		try{
			fileWriter.write(writeMsg);
		} catch (IOException ex){
			CivChat2.severeMessage("Could not write to chatlog file " + ex);
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
		} catch (IOException ex){
			CivChat2.severeMessage("Could not write to chatlog file " + ex);
		}
	}
	
	/**
	 * Method to run to add header info to chatlog
	 */
	private void addHeader(){
		try{
			fileWriter.write("Chat log created at " + new Date());
			fileWriter.newLine();
			fileWriter.newLine();
			fileWriter.flush();
		} catch (IOException ex){
			CivChat2.severeMessage("Could not write to chatlog file " + ex);
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
			//get oldest file first
			List<String> fileDates = new ArrayList<String>();
			for(File f : filtered){
				fileDates.add(f.getName());
			}
			
			Collections.sort(fileDates, new Comparator<String>(){
				DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
				@Override
				public int compare(String s1, String s2){
					try{
						return f.parse(s1).compareTo(f.parse(s2));
					} catch (ParseException e){
						throw new IllegalArgumentException(e);
					}
				}
			});
			
			int numToZip = filtered.length - config.getFilesToZip();
			int zipCount = 1;
			CivChat2.infoMessage("Zipping [" + numToZip + "] Files");
			byte[] buffer = new byte[1024];
			try{
				CivChat2.debugmessage("Currently Zipping Files");
				FileOutputStream fos = new FileOutputStream(chatDirectory + date + ".zip");
                ZipOutputStream zos = new ZipOutputStream(fos);
                for (File file : filtered) {
                	if(zipCount >= numToZip){
                		//done zipping now
                	}
                	else{
	                    ZipEntry ze = new ZipEntry(file.toString());
	                    zos.putNextEntry(ze);
	                    int length;
	                    FileInputStream in = new FileInputStream(file);
	                    while ((length = in.read(buffer)) > 0){
	                    	zos.write(buffer, 0 , length);
	                    }
	                    zos.closeEntry();
	                    in.close();
	                    //close the file and then delete
	                    file.delete();
                	}
                    zipCount++;
                    
                }
                zos.close();
			} catch (Exception e) {
            	CivChat2.severeMessage("Error Zipping Files" + e);
            	return;
            }
			
			zipFile();
		}
	}
	
	private void zipFile(){
		File[] zipList = toRecord.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip");
            }
        });
        CivChat2.infoMessage("zipList.length = " + zipList.length);
        if (zipList != null && zipList.length > config.getMaxNumberofZips()) {
            CivChat2.infoMessage("Deleting zips...");
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
            CivChat2.infoMessage("Deleted: " + toDelete.getName());
        }
	}
	public void test() {
		CivChat2.debugmessage("CivChat2FileLogger being accessed successfully");
	}
	
	
}
