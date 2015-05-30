package com.github.igotyou.FactoryMod.persistence;

import com.github.igotyou.FactoryMod.interfaces.Factory;

public interface FactoryCodec<T extends Factory> {
	
	public void write(T out);
	
	public T read();

}
