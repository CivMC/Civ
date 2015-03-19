package com.github.igotyou.FactoryMod.persistence;

import java.util.List;

import com.github.igotyou.FactoryMod.interfaces.Factory;

public interface FactoryWriter<T extends Factory> {
	
	public void write(List<T> factories);

}
