package isaac.bastion.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import isaac.bastion.Bastion;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
	private FileConfiguration config;
	private File main_config;
	
	private String host;
	private int port;
	private String database;
	private String prefix;
	
	private String username;
	private String password;
	
	private Material bastionBlockMaterial;
	private int bastionBlockEffectRadius;
	private double bastionBlockScaleFacStart;
	private double bastionBlockScaleFacEnd;
	private int bastionBlockScaleTime;
	private int bastionBlockMaxBreaks;
	private int bastionBlockErosion;
	
	static String file_name="config.xml";
	
	public ConfigManager(){
		config=Bastion.getPlugin().getConfig();
		main_config=new File(Bastion.getPlugin().getDataFolder()+File.pathSeparator+file_name);
		config.options().copyDefaults(true);
		if(main_config.exists()){
			try {
				config.load(main_config);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		load();
	}
	
	public String getHost(){
		return host;
	}
	public int getPort(){
		return port;
	}
	public String getDatabase(){
		return database;
	}
	public String getPrefix(){
		return prefix;
	}
	
	public String getUsername(){
		return username;
	}
	public String getPassword(){
		return password;
	}
	
	public Material getBastionBlockMaterial(){
		return bastionBlockMaterial;
	}
	public int getBastionBlockEffectRadius(){
		return bastionBlockEffectRadius;
	}
	public double getBastionBlockScaleFacStart(){
		return bastionBlockScaleFacStart;
	}
	public double getBastionBlockScaleFacEnd(){
		return bastionBlockScaleFacEnd;
	}
	public int getBastionBlockScaleTime(){
		return bastionBlockScaleTime;
	}
	public int getBastionBlockMaxBreaks(){
		return bastionBlockMaxBreaks;
	}
	public int getBastionBlockErosion(){
		return bastionBlockErosion;
	}
	
	
	public void load(){
		host=loadString("mysql.host");
		port=loadInt("mysql.port");
		database=loadString("mysql.database");
		prefix=loadString("mysql.prefix");
		
		username=loadString("mysql.username");
		password=loadString("mysql.password");
		
		bastionBlockMaterial=Material.getMaterial(loadString("BastionBlock.material"));
		bastionBlockEffectRadius=loadInt("BastionBlock.effectRadius");
		bastionBlockMaxBreaks=loadInt("BastionBlock.maxBreaksPerMinute");
		bastionBlockErosion=loadInt("BastionBlock.erosionRatePerDay");
		bastionBlockScaleFacStart=loadDouble("BastionBlock.startScaleFactor");
		bastionBlockScaleFacEnd=loadDouble("BastionBlock.finalScaleFactor");
		bastionBlockScaleTime=loadInt("BastionBlock.warmUpTime");
		Bastion.getPlugin().saveConfig();
		
	}
	private int loadInt(String field){
		if(config.isInt(field)){
			int value=config.getInt(field);
			return value;
		}
		return Integer.MIN_VALUE;
		
	}
	private String loadString(String field){
		if(config.isString(field)){
			String value=config.getString(field);
			return value;
		}
		return null;
	}
	/*private boolean loadBool(String field){
		if(config.isBoolean(field)){
			boolean value=config.getBoolean(field);
			return value;
		}
		return false;
	}*/
	private double loadDouble(String field){
		if(config.isDouble(field)){
			double value=config.getDouble(field);
			return value;
		}
		if(config.isInt(field)){
			double value=config.getInt(field);
			return value;
		}
		return Double.NaN;
	}
}
