package vg.civcraft.mc.civmodcore.annotations;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import vg.civcraft.mc.civmodcore.Config;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;

public class ConfigOption {
  private final Config config_;
  private final String name_;
  private final CivConfigType type_;
  private final Object default_;
  private Object value_;

  public ConfigOption(Config config, CivConfig bug) {
	config_ = config;
    name_ = bug.name();
    type_ = bug.type();
    Object failure = new Object();
    if (type_ == CivConfigType.Object) {
      YamlConfiguration yc = null;
      try {
        yc = new YamlConfiguration();
        yc.loadFromString(bug.def());
      } catch (InvalidConfigurationException ice) {
        yc = null;
      }
      if (yc == null) {
        default_ = failure;
      } else {
        default_ = yc.get("default");
      }
    } else {
      default_ = convert(bug.def(), failure);
    }
    if (default_ == failure) {
      throw new Error(String.format(
          "Unable to parse default value for %s: %s",
          name_, bug.def()));
    }
    value_ = default_;
    load();
  }

  public void load() {
    if (!config_.getStorage().isSet(name_)) {
      return;
    }
    switch(type_) {
      case Bool:
        set(config_.getStorage().getBoolean(name_, (Boolean)value_));
        break;
      case Int:
        set(config_.getStorage().getInt(name_, (Integer)value_));
        break;
      case Double:
        set(config_.getStorage().getDouble(name_, (Double)value_));
        break;
      case String:
        set(config_.getStorage().getString(name_, (String)value_));
        break;
      case Long:
    	set(config_.getStorage().getLong(name_, (Long)value_));
    	break;
      case String_List:
    	set(config_.getStorage().getStringList(name_));
    	break;
      case Object:
        set(config_.getStorage().get(name_));
      default:
        throw new Error("Unknown OptType");
    }
  }

  @SuppressWarnings("unchecked")
  public Object convert(Object value, Object defaultValue) {
    switch(type_) {
      case Bool:
        return (value instanceof Boolean ? value : 
            value instanceof String ? 
                (value.equals("1") || ((String)value).equalsIgnoreCase("true")) : false);
      case Int:
	    if (value instanceof Integer) {
          return value;
        } else if (value instanceof String) {
          if (((String) value).isEmpty()) {
            return -1;
          }
          try {
            return Integer.parseInt((String) value);
          } catch(Exception e) {
            return defaultValue;
          }
		} else {
          return defaultValue;
        }
      case Double:
        if (value instanceof Double) {
          return value;
        } else if (value instanceof String) {
          if (((String)value).isEmpty()) {
            return -1.00000001;
          }
          try {
            return Double.parseDouble((String)value);
          } catch(Exception e) {
            return defaultValue;
          }
        } else {
          return defaultValue;
        }
      case String:
        if (value == null || !(value instanceof String)) {
          return defaultValue;
        }
        return value;
      case Long:
        if (value instanceof Long) {
          return value;
        } else if (value instanceof String) {
    	  if (((String) value).isEmpty()) {
            return -1;
          }
          try {
            return Long.parseLong((String) value);
          } catch(Exception e) {
            return defaultValue;
          }
        } else {
          return defaultValue;
        }
      case String_List:
        List<String> list = new ArrayList<String>();
        if (value == null || !(value instanceof List<?> || value instanceof String[] || value instanceof String)) {
          return (List<String>) defaultValue;
        } else if (value instanceof List<?>) { // not a truly safe assumption...
          list.addAll((List<String>) value);
        } else if (value instanceof String[]) {
          for (String v: (String[]) value) {
            list.add(v);
          }
    	} else if (value instanceof String) {
          String[] parts = ((String) value).split("\\|");
          for (String x: parts) {
            list.add(x);
          }
    	} else {
          return defaultValue;
    	}
        return list;
      case Object:
        if (value == null) {
          return defaultValue;
        } else {
          return value;
        }
      default:
        throw new Error("Unknown OptType");
    }
  }

  public void set(Object value) {
    if (value instanceof String) {
      setString((String)value);
      return;
    }
    switch(type_) {
      case Bool:
        if (!(value instanceof Boolean)) {
          throw new Error(String.format(
              "Value set is not a Boolean for %s: %s",
              name_, value.toString()));
        }
        value_ = value;
        break;
      case Int:
        if (!(value instanceof Integer)) {
          throw new Error(String.format(
              "Value set is not a Integer for %s: %s",
              name_, value.toString()));
        }
        value_ = value;
        break;
      case Double:
        if (!(value instanceof Double)) {
          throw new Error(String.format(
              "Value set is not a Double for %s: %s",
              name_, value.toString()));
        }
        value_ = value;
        break;
      case Long:
        if (!(value instanceof Long)) {
          throw new Error(String.format(
              "Value set is not a Long for %s: %s",
              name_, value.toString()));
        }
        value_ = value;
        break;
      case String_List:
        if (!(value instanceof List)) {
          throw new Error(String.format(
              "Value set is not a List for %s: %s", name_, value.toString()));
        }
        value_ = value;
        break;
      case Object: // unchecked.
        value_ = value;
        break;
      case String: // ideally handled above already.
      default:
        throw new Error("Unknown OptType");
    }
    config_.getStorage().set(name_, value_);
  }

  public void setString(String value) {
    value_ = convert(value, value_);
    config_.getStorage().set(name_, value_);
  }

  public String getName() {
    return name_;
  }

  public CivConfigType getType() {
    return type_;
  }

  public Boolean getBool() {
    if (type_ != CivConfigType.Bool) {
      throw new Error(String.format(
          "Config option %s not of type Boolean", name_));
    }
    return (Boolean)value_;
  }

  public Integer getInt() {
    if (type_ != CivConfigType.Int) {
      throw new Error(String.format(
          "Config option %s not of type Integer", name_));
    }
    return (Integer)value_;
  }

  public Double getDouble() {
    if (type_ != CivConfigType.Double) {
      throw new Error(String.format(
          "Config option %s not of type Double", name_));
    }
    return (Double)value_;
  }
  
  public Long getLong() {
    if (type_ != CivConfigType.Long) {
      throw new Error(String.format(
          "Config option %s not of type Long", name_));
    }
    return (Long)value_;
  }
  
  @SuppressWarnings("unchecked")
  public List<String> getStringList() {
    if (type_ != CivConfigType.String_List) {
      throw new Error(String.format(
          "Config option %s not of type Long", name_));
    }
    return (List<String>) value_;
  }

  public Object getObject() {
    if (type_ != CivConfigType.Object) {
      throw new Error(String.format(
          "Config option %s not of type Object", name_));
    }
    return value_;
  }
  
  public Object get() {
    return value_;
  }
  
  public String getString() {
    return value_.toString();
  }
}
