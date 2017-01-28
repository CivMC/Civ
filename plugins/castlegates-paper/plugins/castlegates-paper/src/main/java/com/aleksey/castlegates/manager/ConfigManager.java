/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.manager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import com.aleksey.castlegates.DeprecatedMethods;
import com.aleksey.castlegates.types.TimerOperation;
import com.aleksey.castlegates.utils.Helper;

public class ConfigManager {
	public static class Database {
		public String host;
		public int port;
		public String db;
		public String user;
		public String password;
	}

	private static class ItemMaterial {
		public Material material;
		public Short damage;
		public Integer amount;
		public String lore;

		public ItemMaterial(Material material, Short damage, Integer amount, String lore) {
			this.material = material;
			this.damage = damage;
			this.amount = amount;
			this.lore = lore;

			if(Objects.equals(this.lore, "")) {
				this.lore = null;
			}
		}
	}

	private Logger logger;
	private FileConfiguration file;
	private Database database;
	private HashSet<Material> gearMaterials;
	private HashSet<Material> bridgeMaterials;
	private ItemMaterial stickItem;
	private ItemMaterial creationConsumeItem;
	private int maxPowerTransfers;
	private int maxBridgeLength;
	private int playerStateResetInSeconds;
	private int switchTimeout;
	private int maxRedstoneDistance;
	private boolean allowAutoCreate;
	private boolean interactWithSnitches;
	private boolean logChanges;
	private boolean timerEnabled;
	private int timerMin;
	private int timerMax;
	private int timerDefault;
	private TimerOperation timerDefaultOperation;

	public ConfigManager(Logger logger) {
		this.logger = logger;
	}

	public void load(FileConfiguration file) {
		this.file = file;

		this.database = new Database();
		this.database.host = getString("Database.Host", "localhost");
		this.database.port = getInt("Database.Port", 3306);
		this.database.db = getString("Database.Schema", "castle_gates");
		this.database.user = getString("Database.User", "");
		this.database.password = getString("Database.Password", "");

		this.allowAutoCreate = getBoolean("Settings.AllowAutoCreate", true);
		this.interactWithSnitches = getBoolean("Settings.InteractWithSnitches", true);
		this.maxPowerTransfers = getInt("Settings.MaxPowerTransfers", 8, 0, 10);
		this.maxBridgeLength = getInt("Settings.MaxBridgeLength", 16, 1, 40);
		this.playerStateResetInSeconds = getInt("Settings.PlayerStateResetInSeconds", 5 * 60);
		this.switchTimeout = getInt("Settings.SwitchTimeout", 1000, 500, 100000);
		this.maxRedstoneDistance = getInt("Settings.MaxRedstoneDistance", 7);
		this.logChanges = getBoolean("Settings.LogChanges", true);
		this.timerEnabled = getBoolean("Settings.Timer.Enabled", true);
		this.timerMin = getInt("Settings.Timer.Min", 1);
		this.timerMax = getInt("Settings.Timer.Max", 60);
		this.timerDefault = getInt("Settings.Timer.DefaultInterval", 5);
		this.timerDefaultOperation = getTimerOperation("Settings.Timer.DefaultOperation", TimerOperation.UNDRAW);

		this.gearMaterials = getBlockMaterials("Blocks.GearMaterials", ConfigDefaults.gearMaterials);
		this.bridgeMaterials = getBlockMaterials("Blocks.BridgeMaterials", ConfigDefaults.bridgeMaterials);

		this.stickItem = getItemMaterial("Items.Tool", Material.STICK, null);

		this.creationConsumeItem = getItemMaterial("Items.CreationConsume", Material.GOLD_INGOT, 1);

		if(this.creationConsumeItem.amount == null) {
			this.creationConsumeItem.amount = 1;
		}

		if(this.creationConsumeItem.damage == null) {
			this.creationConsumeItem.damage = 0;
		}
	}

	public boolean isTimerEnabled() {
		return this.timerEnabled;
	}

	public int getTimerMin() {
		return this.timerMin;
	}

	public int getTimerMax() {
		return this.timerMax;
	}

	public int getTimerDefault() {
		return this.timerDefault;
	}

	public TimerOperation getTimerDefaultOperation() {
		return this.timerDefaultOperation;
	}

	public Database getDatabase() {
		return this.database;
	}

	public boolean isGearBlockType(Block block) {
		return this.gearMaterials.contains(block.getType());
	}

	public boolean isStickItem(ItemStack item) {
		if(item == null
				|| item.getType() != this.stickItem.material
				|| this.stickItem.damage != null && item.getDurability() != this.stickItem.damage
				|| this.stickItem.amount != null && item.getAmount() != this.stickItem.amount
				)
		{
			return false;
		}

		String lore = Helper.getLore(item);

		if(Objects.equals(lore, "")) {
			lore = null;
		}

		return Objects.equals(lore, this.stickItem.lore);
	}

	public boolean isBridgeMaterial(Block block) {
		return this.bridgeMaterials.contains(block.getType());
	}

	public boolean isCreationConsumeItem(ItemStack item) {
		if(item == null
				|| item.getType() != this.creationConsumeItem.material
				|| this.creationConsumeItem.damage != null && item.getDurability() != this.creationConsumeItem.damage
				)
		{
			return false;
		}

		String lore = Helper.getLore(item);

		if(Objects.equals(lore, "")) {
			lore = null;
		}

		return Objects.equals(lore, this.creationConsumeItem.lore);
	}

	public ItemStack getCreationConsumeItem() {
		ItemStack item = new ItemStack(
				this.creationConsumeItem.material,
				this.creationConsumeItem.amount,
				this.creationConsumeItem.damage
				);

		Helper.setLore(item, this.creationConsumeItem.lore);

		return item;
	}

	public int getDataWorkerRate() {
		return 500;
	}

	public int getTimerWorkerRate() {
		return 500;
	}

	public int getMaxPowerTransfers() {
		return this.maxPowerTransfers;
	}

	public int getMaxBridgeLength() {
		return this.maxBridgeLength;
	}

	public int getPlayerStateResetInSeconds() {
		return this.playerStateResetInSeconds;
	}

	public int getSwitchTimeout() {
		return this.switchTimeout;
	}

	public double getMaxRedstoneDistance() {
		return this.maxRedstoneDistance;
	}

	public boolean getAllowAutoCreate() {
		return this.allowAutoCreate;
	}

	public boolean getInteractWithSnitches() {
		return this.interactWithSnitches;
	}

	public boolean getLogChanges() {
		return this.logChanges;
	}

	private TimerOperation getTimerOperation(String path, TimerOperation defaultData) {
        if (this.file.get(path) == null)
        	this.file.set(path, defaultData.name());

        String name = this.file.getString(path, defaultData.name());
        TimerOperation result = Helper.parseTimerOperation(name);

        if(result == null) {
        	this.logger.log(Level.WARNING, "'" + name + "' is invalid timer operation, will be used '" + defaultData.name() + "'");
        	result = defaultData;
        }

        return result;
	}

	private String getString(String path, String defaultData) {
        if (this.file.get(path) == null)
        	this.file.set(path, defaultData);

        return this.file.getString(path, defaultData);
    }

    private int getInt(String path, int defaultData) {
        if (this.file.get(path) == null)
            this.file.set(path, defaultData);

        return this.file.getInt(path, defaultData);
    }

    private int getInt(String path, int defaultData, int min, int max) {
        if (this.file.get(path) == null)
            this.file.set(path, defaultData);

        int value = this.file.getInt(path, defaultData);

        if(value < min) {
        	value = min;
        }
        else if(value > max) {
        	value = max;
        }

        return value;
    }

    private boolean getBoolean(String path, boolean defaultData) {
        if (this.file.get(path) == null)
        	this.file.set(path, defaultData);

        return this.file.getBoolean(path, defaultData);
    }

    private HashSet<Material> getBlockMaterials(String path, String[] materials) {
    	List<String> list;

    	if(this.file.get(path) != null) {
    		list = this.file.getStringList(path);
    	} else {
    		this.file.set(path, materials);
    		list = Arrays.asList(materials);
    	}

    	HashSet<Material> result = new HashSet<Material>();

    	for(String material : list) {
    		if(material == null || material.length() == 0) continue;

    		if(Character.isDigit(material.charAt(0))) {
    			result.add(DeprecatedMethods.getMaterial(Integer.parseInt(material)));
    		} else {
    			result.add(Material.getMaterial(material.toUpperCase()));
    		}
    	}

    	return result;
    }

    private ItemMaterial getItemMaterial(String path, Material defaultMaterial, Integer defaultAmount) {
    	String material = this.file.getString(path + ".material");
    	Short damage;
    	Integer amount;
    	String lore;

    	if(material == null) {
    		material = defaultMaterial.name();
    		damage = null;
    		amount = defaultAmount != null ? defaultAmount: null;
    		lore = "";

    		this.file.set(path + ".material", material);
    		this.file.set(path + ".damage", damage);
    		this.file.set(path + ".amount", amount);
    		this.file.set(path + ".lore", lore);
    	} else {
    		if(this.file.get(path + ".damage") != null) {
        		damage = (short)this.file.getInt(path + ".damage");
    		} else {
    			damage = null;
    			this.file.set(path + ".damage", damage);
    		}

    		if(this.file.get(path + ".amount") != null) {
    			amount = this.file.getInt(path + ".amount");
    		} else {
    			amount = defaultAmount;
    			this.file.set(path + ".amount", amount);
    		}

    		if(this.file.get(path + ".lore") != null) {
    			lore = this.file.getString(path + ".lore");
    		} else {
    			lore = "";
    			this.file.set(path + ".lore", lore);
    		}
    	}

    	Material materialObj = Material.getMaterial(material);

    	return new ItemMaterial(materialObj, damage, amount, lore);
    }
}
