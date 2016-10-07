package com.devotedmc.ExilePearl.config;

public class DataPair {
	
    private final String key;
    private final Object value;

	public DataPair(String key, Object value) {
		this.key = key;
		this.value = value;
	}

    /**
     * Gets the key for this pair.
     * @return key for this pair
     */
    public String getKey() { return key; }

    /**
     * Gets the value for this pair.
     * @return value for this pair
     */
    public Object getValue() { return value; }
	
    @Override
    public String toString() {
        return key + "=" + value;
    }
    
    @Override
    public int hashCode() {
        return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof DataPair) {
        	DataPair pair = (DataPair) o;
            if (key != null ? !key.equals(pair.key) : pair.key != null) return false;
            if (value != null ? !value.equals(pair.value) : pair.value != null) return false;
            return true;
        }
        return false;
    }

}
