package com.github.igotyou.FactoryMod.persistence;

import java.util.List;

import com.github.igotyou.FactoryMod.Factorys.IFactory;

public interface FactoryWriter<T extends IFactory> {
	
	public void write(List<T> factories);

}
