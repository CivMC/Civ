package com.programmerdan.minecraft.simpleadminhacks.framework.utilities;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;

public final class PacketManager {

	private final ProtocolManager manager;
	private final List<PacketAdapter> adapters;

	public PacketManager() {
		this.manager = ProtocolLibrary.getProtocolManager();
		this.adapters = new ArrayList<>();
	}

	public void addAdapter(final PacketAdapter adapter) {
		Preconditions.checkNotNull(adapter, "Adapter cannot be null!");
		this.manager.addPacketListener(adapter);
		this.adapters.add(adapter);
	}

	public void removeAllAdapters() {
		this.adapters.forEach(this.manager::removePacketListener);
		this.adapters.clear();
	}

}
