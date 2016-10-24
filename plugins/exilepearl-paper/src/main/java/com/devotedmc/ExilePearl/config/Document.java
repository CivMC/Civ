package com.devotedmc.ExilePearl.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class Document implements Map<String, Object> {
    private final Map<String, Object> documentAsMap;

    /**
     * Creates an empty Document instance.
     */
    public Document() {
        documentAsMap = new LinkedHashMap<String, Object>();
    }

    /**
     * Create a Document instance initialized with the given key/value pair.
     *
     * @param key   key
     * @param value value
     */
    public Document(final String key, final Object value) {
        documentAsMap = new LinkedHashMap<String, Object>();
        append(key, value);
    }

    /**
     * Creates a Document instance initialized with the given map.
     *
     * @param map initial map
     */
    public Document(final Map<String, Object> map) {
        documentAsMap = new LinkedHashMap<String, Object>(map);
    }
    
    public Document(final ConfigurationSection configSection) {
        documentAsMap = parseConfigurationSection(configSection);
    }

    /**
     * Put the given key/value pair into this Document and return this.  Useful for chaining puts in a single expression, e.g.
     * <pre>
     * doc.append("a", 1).append("b", 2)}
     * </pre>
     * @param key   key
     * @param value value
     * @return this
     */
    public Document append(final String key, Object value) {
    	String[] keys = key.split("\\.");
    	
    	if (value instanceof Location) {
    		value = serializeLocation((Location) value);
    	}
    	
    	if (value instanceof UUID) {
    		value = value.toString();
    	}
    	
    	if (keys.length == 1) {
    		documentAsMap.put(key, value);
    		return this;
    	}
    	
    	Document doc = this;
    	for(int i = 0; i < keys.length - 1; i++) {
    		doc = doc.getDocument(keys[i]);
    	}
    	
    	doc = doc.append(keys[keys.length - 1], value);
    	return this;
    }

    /**
     * Gets the value of the given key as an Integer.
     *
     * @param key the key
     * @return the value as an integer, which may be null
     * @throws java.lang.ClassCastException if the value is not an integer
     */
    public Integer getInteger(final String key) {
        return (Integer) get(key);
    }

    /**
     * Gets the value of the given key as a primitive int.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as an integer, which may be null
     * @throws java.lang.ClassCastException if the value is not an integer
     */
    public int getInteger(final String key, final int defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : (Integer) value;
    }

    /**
     * Gets the value of the given key as a Long.
     *
     * @param key the key
     * @return the value as a long, which may be null
     * @throws java.lang.ClassCastException if the value is not an long
     */
    public Long getLong(final String key) {
        return (Long) get(key);
    }

    /**
     * Gets the value of the given key as a primitive long.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a long, which may be null
     * @throws java.lang.ClassCastException if the value is not a long
     */
    public Long getLong(final String key, final long defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : (Long) value;
    }

    /**
     * Gets the value of the given key as a Double.
     *
     * @param key the key
     * @return the value as a double, which may be null
     * @throws java.lang.ClassCastException if the value is not an double
     */
    public Double getDouble(final String key) {
        return (Double) get(key);
    }
    
    public Double getDouble(final String key, final double defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : (Double) value;
    }

    /**
     * Gets the value of the given key as a String.
     *
     * @param key the key
     * @return the value as a String, which may be null
     * @throws java.lang.ClassCastException if the value is not a String
     */
    public String getString(final String key) {
        return (String) get(key);
    }

    /**
     * Gets the value of the given key as a String.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a String, which may be null
     * @throws java.lang.ClassCastException if the value is not a String
     */
    public String getString(final String key, final String defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : (String) value;
    }

    /**
     * Gets the value of the given key as a Boolean.
     *
     * @param key the key
     * @return the value as a double, which may be null
     * @throws java.lang.ClassCastException if the value is not an double
     */
    public Boolean getBoolean(final String key) {
        return (Boolean) get(key);
    }

    /**
     * Gets the value of the given key as a primitive boolean.
     *
     * @param key          the key
     * @param defaultValue what to return if the value is null
     * @return the value as a double, which may be null
     * @throws java.lang.ClassCastException if the value is not an double
     */
    public boolean getBoolean(final String key, final boolean defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : (Boolean) value;
    }

    /**
     * Gets the value of the given key as a Date.
     *
     * @param key the key
     * @return the value as a Date, which may be null
     * @throws java.lang.ClassCastException if the value is not a Date
     */
    public Date getDate(final String key) {
        return (Date) get(key);
    }
    
    public Date getDate(final String key, final Date defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : (Date) value;
    }
    
    @SuppressWarnings("unchecked")
	public List<String> getStringList(final String key) {
        Object o = get(key);
        
        if (o == null) {
        	return new ArrayList<String>();
        }
        List<String> list;
        
        try {
        	list = (List<String>)o;
        } catch(Exception ex) {
        	list = new ArrayList<String>();
        }
        return list;
    }
    
    public Location getLocation(final String key) {
    	return deserializeLocation(getDocument(key));
    }
    
    
    public boolean containsField(final String key) {
    	Object value = get(key);
    	return value != null;
    }
    
    public boolean containsDocument(final String key) {
    	Object value = get(key);
    	return value != null && value instanceof Document;
    }
    
	public Document getDocument(final String key) {
    	return (Document)get(key);
    }
	
	public UUID getUUID(final String key) {
    	return UUID.fromString(getString(key));
    }

    // Vanilla Map methods delegate to map field

    @Override
    public int size() {
        return documentAsMap.size();
    }

    @Override
    public boolean isEmpty() {
        return documentAsMap.isEmpty();
    }

    @Override
    public boolean containsValue(final Object value) {
        return documentAsMap.containsValue(value);
    }

    @Override
    public boolean containsKey(final Object key) {
    	return get(key) != null;
    }

    @Override
    public Object get(final Object key) {
    	if (!(key instanceof String)) {
    		return documentAsMap.get(key);
    	}
    	
    	String sKey = (String)key;
    	String[] keys = sKey.split("\\.");
    	
    	if (keys.length == 1) {
    		return documentAsMap.get(key);
    	}
    	
    	Document doc = this;
    	for(int i = 0; i < keys.length - 1 && doc != null; i++) {
    		doc = doc.getDocument(keys[i]);
    	}
    	
    	if (doc == null) {
    		return null;
    	}
    	
        return doc.get(keys[keys.length - 1]);
    }

    @Override
    public Object put(final String key, final Object value) {
    	append(key, value);
    	return null;
    }

    @Override
    public Object remove(final Object key) {
        return documentAsMap.remove(key);
    }

    @Override
    public void putAll(final Map<? extends String, ?> map) {
    	for(java.util.Map.Entry<? extends String, ?> e : map.entrySet()) {
    		append(e.getKey(), e.getValue());
    	}
    }

    @Override
    public void clear() {
        documentAsMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return documentAsMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return documentAsMap.values();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return documentAsMap.entrySet();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Document document = (Document) o;

        if (!documentAsMap.equals(document.documentAsMap)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return documentAsMap.hashCode();
    }

    @Override
    public String toString() {
        return "Document{"
               + documentAsMap
               + '}';
    }
    
    
    public void savetoConfig(ConfigurationSection mem) {
    	documentToConfigurationSection(mem, this);
    }
    
	/**
	 * Recursively converts Bukkit configuration sections into documents
	 * @param mem The bukkit configuration section
	 * @return A document containing all the configuration data
	 */
    private static Map<String, Object> parseConfigurationSection(ConfigurationSection mem) {
    	Document doc = new Document();
		
		for(Entry<String, Object> e : mem.getValues(false).entrySet()) {
			String k = e.getKey();
			Object o = e.getValue();
			if (o instanceof ConfigurationSection) {
				doc.put(k, parseConfigurationSection((ConfigurationSection)o));
			}
			else {
				doc.put(k, o);
			}
		}
		
		return doc;
	}
	
	/**
	 * Recursively adds all document data to a Bukkit configuration section
	 * @param mem The bukkit configuration section
	 * @param doc The document object
	 * @return The resulting configuration section
	 */
	private static ConfigurationSection documentToConfigurationSection(ConfigurationSection mem, Document doc) {
		for(Entry<String, Object> e : doc.entrySet()) {
			String k = e.getKey();
			Object o = e.getValue();
			if (o instanceof Document) {
				documentToConfigurationSection(mem.createSection(k), (Document)o);
			}
			else {
				mem.set(k, o);
			}
		}
		
		return mem;
	}
	
	private static Document serializeLocation(Location l) {
		Document doc = new Document("world", l.getWorld().getName())
		.append("x", l.getBlockX())
		.append("y", l.getBlockY())
		.append("z", l.getBlockZ());
		
		return doc;
	}
    
	
	private static Location deserializeLocation(Document doc) {
		try {
			String worldName = doc.getString("world");
			int x = doc.getInteger("x");
			int y = doc.getInteger("y");
			int z = doc.getInteger("z");
			return new Location(Bukkit.getWorld(worldName), x, y, z);
		} catch(Exception ex) {
			return null;
		}
	}
}
