package vg.civcraft.mc.civchat2.utility;

import org.bukkit.configuration.file.FileConfiguration;

public class CivChat2Config {
	private static FileConfiguration config;
	
	public void setConfigOptions(FileConfiguration config){
		CivChat2Config.config = config;
	}
	
	public boolean getGroupsEnabled(){
		return config.getBoolean("info.groups");
	}
	
	public boolean getDebug(){
		return config.getBoolean("info.debug");
	}
	
	public String getAfkMessage(){
		return config.getString("info.afkmessage");
	}

	public boolean getLoginAnnounce() {
		return config.getBoolean("info.loginAnnounce", false);
	}

	public int getFilesToZip() {
		return config.getInt("file.filesToZip");
	}

	public int getMaxNumberofZips() {
		return config.getInt("file.maxNumberofZips");
	}

	public int getChatRange() {
		return config.getInt("chat.globalChatRange");
	}

	public String getDefaultColor() {
		return config.getString("chat.defaultChatColor");
	}
	
	public String getOpChatColor(){
		return config.getString("chat.opChatColor");
	}

	public int getYInc() {
		return config.getInt("chat.yIncreaseDist");
	}

	public double getYScale() {
		return config.getDouble("chat.yIncreaseScale");
	}
	
	public String getMysqlUsername(){
		return config.getString("mysql.username");
	}
	
	public String getMysqlHost(){
		return config.getString("mysql.host");
	}
	
	public int getMysqlPort(){
		return config.getInt("mysql.port");
	}
	
	public String getMysqlPassword(){
		return config.getString("mysql.password");
	}
	
	public String getMysqlDBname(){
		return config.getString("mysql.dbname");
	}

	//This could be changed to a string instead of a char fairly easily in the future
	public char getColorAtDistance(double distance) {
		char ret = '-';
		double minRange = Double.MAX_VALUE;
		if(config.contains("chat.colors")) {
			for(String s : config.getConfigurationSection("chat.colors").getKeys(false)) {
				double range = config.getDouble("chat.colors." + s + ".range");
				if(config.contains("chat.colors." + s + ".range") && distance < range && range < minRange) {
					minRange = range;
					ret = config.getString("chat.colors." + s + ".color").charAt(0);
				}
			}
		}
		return ret;
	}
}
