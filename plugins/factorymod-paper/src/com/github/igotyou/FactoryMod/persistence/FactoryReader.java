package com.github.igotyou.FactoryMod.persistence;

import java.util.List;

import com.github.igotyou.FactoryMod.interfaces.Factory;

public interface FactoryReader<T extends Factory> {
	
	public List<T> read();

}
