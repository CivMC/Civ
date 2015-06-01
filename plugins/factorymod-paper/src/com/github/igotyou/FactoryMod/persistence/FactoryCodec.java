package com.github.igotyou.FactoryMod.persistence;

import com.github.igotyou.FactoryMod.Factorys.IFactory;

public interface FactoryCodec<T extends IFactory> {
	
	public void write(T out);
	
	public T read();

}
