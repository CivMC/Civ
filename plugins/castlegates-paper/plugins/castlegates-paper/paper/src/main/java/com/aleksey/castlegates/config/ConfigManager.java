/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.ItemStack;

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

	private final Logger _logger;
	private FileConfiguration _file;
	private Database _database;
	private HashSet<Material> _gearMaterials;
	private HashSet<Material> _bridgeMaterials;
	private ItemMaterial _stickItem;
	private ItemMaterial _creationConsumeItem;
	private int _maxPowerTransfers;
	private int _maxBridgeLength;
	private int _playerStateResetInSeconds;
	private int _switchTimeout;
	private int _maxRedstoneDistance;
	private boolean _allowAutoCreate;
	private boolean _interactWithSnitches;
	private boolean _timerEnabled;
	private int _timerMin;
	private int _timerMax;
	private int _timerDefault;
	private TimerOperation _timerDefaultOperation;
	private boolean _gearblockMustBeReinforced;
	private boolean _horizontalLinkDisabled;

	public ConfigManager(Logger logger) {
		_logger = logger;
	}

	public void load(FileConfiguration file) {
		_file = file;

		_database = new Database();
		_database.host = getString("Database.Host", "localhost");
		_database.port = getInt("Database.Port", 3306);
		_database.db = getString("Database.Schema", "castle_gates");
		_database.user = getString("Database.User", "");
		_database.password = getString("Database.Password", "");

		_allowAutoCreate = getBoolean("Settings.AllowAutoCreate", true);
		_interactWithSnitches = getBoolean("Settings.InteractWithSnitches", true);
		_maxPowerTransfers = getInt("Settings.MaxPowerTransfers", 8, 0, 10);
		_maxBridgeLength = getInt("Settings.MaxBridgeLength", 16, 1, 40);
		_playerStateResetInSeconds = getInt("Settings.PlayerStateResetInSeconds", 5 * 60);
		_switchTimeout = getInt("Settings.SwitchTimeout", 1000, 500, 100000);
		_maxRedstoneDistance = getInt("Settings.MaxRedstoneDistance", 7);
		_timerEnabled = getBoolean("Settings.Timer.Enabled", true);
		_timerMin = getInt("Settings.Timer.Min", 1);
		_timerMax = getInt("Settings.Timer.Max", 60);
		_timerDefault = getInt("Settings.Timer.DefaultInterval", 5);
		_timerDefaultOperation = getTimerOperation();
		_gearblockMustBeReinforced = getBoolean("Settings.GearblockMustBeReinforced", false);
		_horizontalLinkDisabled = getBoolean("Settings.HorizontalLinkDisabled", false);

		_gearMaterials = getBlockMaterials("Blocks.GearMaterials", ConfigDefaults.gearMaterials);
		_bridgeMaterials = getBlockMaterials("Blocks.BridgeMaterials", ConfigDefaults.bridgeMaterials);

		_stickItem = getItemMaterial("Items.Tool", Material.STICK, null);

		_creationConsumeItem = getItemMaterial("Items.CreationConsume", Material.GOLD_INGOT, 1);

		if(_creationConsumeItem.amount == null) {
			_creationConsumeItem.amount = 1;
		}

		if(_creationConsumeItem.damage == null) {
			_creationConsumeItem.damage = 0;
		}
	}

	public boolean isTimerEnabled() {
		return _timerEnabled;
	}

	public int getTimerMin() {
		return _timerMin;
	}

	public int getTimerMax() {
		return _timerMax;
	}

	public int getTimerDefault() {
		return _timerDefault;
	}

	public TimerOperation getTimerDefaultOperation() {
		return _timerDefaultOperation;
	}

	public boolean isGearblockMustBeReinforced() { return _gearblockMustBeReinforced; }

	public boolean isHorizontalLinkDisabled() { return _horizontalLinkDisabled; }

	public Database getDatabase() {
		return _database;
	}

	public boolean isGearBlockType(Block block) {
		return _gearMaterials.contains(block.getType());
	}

	public boolean isStickItem(ItemStack item) {
		if(item == null
				|| item.getType() != _stickItem.material
				|| _stickItem.damage != null && Helper.getItemDamage(item) != _stickItem.damage
				|| _stickItem.amount != null && item.getAmount() != _stickItem.amount
				)
		{
			return false;
		}

		String lore = Helper.getLore(item);

		if(Objects.equals(lore, "")) {
			lore = null;
		}

		return Objects.equals(lore, _stickItem.lore);
	}

	public boolean isBridgeMaterial(Block block) {
		return _bridgeMaterials.contains(block.getType());
	}

	public boolean isCreationConsumeItem(ItemStack item) {
		if(item == null
				|| item.getType() != _creationConsumeItem.material
				|| _creationConsumeItem.damage != null && Helper.getItemDamage(item) != _creationConsumeItem.damage
				)
		{
			return false;
		}

		String lore = Helper.getLore(item);

		if(Objects.equals(lore, "")) {
			lore = null;
		}

		return Objects.equals(lore, _creationConsumeItem.lore);
	}

	public ItemStack getCreationConsumeItem() {
		ItemStack item = new ItemStack(_creationConsumeItem.material, _creationConsumeItem.amount);

		if (_creationConsumeItem.damage != null)
			((Damageable)item.getItemMeta()).setDamage(_creationConsumeItem.damage);

		Helper.setLore(item, _creationConsumeItem.lore);

		return item;
	}

	public int getDataWorkerRate() {
		return 500;
	}

	public int getTimerWorkerRate() {
		return 500;
	}

	public int getMaxPowerTransfers() {
		return _maxPowerTransfers;
	}

	public int getMaxBridgeLength() {
		return _maxBridgeLength;
	}

	public int getPlayerStateResetInSeconds() {
		return _playerStateResetInSeconds;
	}

	public int getSwitchTimeout() {
		return _switchTimeout;
	}

	public double getMaxRedstoneDistance() {
		return _maxRedstoneDistance;
	}

	public boolean getAllowAutoCreate() {
		return _allowAutoCreate;
	}

	public boolean getInteractWithSnitches() {
		return _interactWithSnitches;
	}

	private TimerOperation getTimerOperation() {
		final String path = "Settings.Timer.DefaultOperation";
		final TimerOperation defaultData = TimerOperation.UNDRAW;

        if (_file.get(path) == null)
        	_file.set(path, defaultData.name());

        String name = _file.getString(path, defaultData.name());
        TimerOperation result = Helper.parseTimerOperation(name);

        if(result == null) {
        	_logger.log(Level.WARNING, "'" + name + "' is an invalid timer operation, '" + defaultData.name() + "'" + " will be used instead.");
        	result = defaultData;
        }

        return result;
	}

	private String getString(String path, String defaultData) {
        if (_file.get(path) == null)
        	_file.set(path, defaultData);

        return _file.getString(path, defaultData);
    }

    private int getInt(String path, int defaultData) {
        if (_file.get(path) == null)
            _file.set(path, defaultData);

        return _file.getInt(path, defaultData);
    }

    private int getInt(String path, int defaultData, int min, int max) {
        if (_file.get(path) == null)
            _file.set(path, defaultData);

        int value = _file.getInt(path, defaultData);

        if(value < min) {
        	value = min;
        }
        else if(value > max) {
        	value = max;
        }

        return value;
    }

    private boolean getBoolean(String path, boolean defaultData) {
        if (_file.get(path) == null)
        	_file.set(path, defaultData);

        return _file.getBoolean(path, defaultData);
    }

    private HashSet<Material> getBlockMaterials(String path, String[] materials) {
    	List<String> list;

    	if(_file.get(path) != null) {
    		list = _file.getStringList(path);
    	} else {
    		_file.set(path, materials);
    		list = Arrays.asList(materials);
    	}

    	HashSet<Material> result = new HashSet<>();

    	for(String materialName : list) {
    		if(materialName != null && materialName.length() > 0) {
				var material = Material.getMaterial(materialName.toUpperCase());

				if (material == null)
					throw new IllegalArgumentException("Material " + materialName + " is not found");

				result.add(material);
			}
    	}

    	return result;
    }

    private ItemMaterial getItemMaterial(String path, Material defaultMaterial, Integer defaultAmount) {
    	String material = _file.getString(path + ".material");
    	Short damage;
    	Integer amount;
    	String lore;

    	if(material == null) {
    		material = defaultMaterial.name();
    		damage = null;
    		amount = defaultAmount;
    		lore = "";

    		_file.set(path + ".material", material);
    		_file.set(path + ".damage", damage);
    		_file.set(path + ".amount", amount);
    		_file.set(path + ".lore", lore);
    	} else {
    		if(_file.get(path + ".damage") != null) {
        		damage = (short)_file.getInt(path + ".damage");
    		} else {
    			damage = null;
    			_file.set(path + ".damage", damage);
    		}

    		if(_file.get(path + ".amount") != null) {
    			amount = _file.getInt(path + ".amount");
    		} else {
    			amount = defaultAmount;
    			_file.set(path + ".amount", amount);
    		}

    		if(_file.get(path + ".lore") != null) {
    			lore = _file.getString(path + ".lore");
    		} else {
    			lore = "";
    			_file.set(path + ".lore", lore);
    		}
    	}

    	Material materialObj = Material.getMaterial(material);

    	return new ItemMaterial(materialObj, damage, amount, lore);
    }
}
