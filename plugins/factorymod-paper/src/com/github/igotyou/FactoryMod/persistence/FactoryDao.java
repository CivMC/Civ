package com.github.igotyou.FactoryMod.persistence;

import java.util.List;

import com.github.igotyou.FactoryMod.interfaces.Factory;

public class FactoryDao<T extends Factory> {

	private FactoryReader<T> mReader;
	private FactoryWriter<T> mWriter;
	
	FactoryDao(FactoryReader<T> reader, FactoryWriter<T> writer) {
		mReader = reader;
		mWriter = writer;
	}
	
	public List<T> readFactories() {
		return mReader.read();
	}

	public void writeFactories(List<T> factories) {
		mWriter.write(factories);
	}

}
