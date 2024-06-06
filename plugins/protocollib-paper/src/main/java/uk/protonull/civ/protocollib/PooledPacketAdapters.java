package uk.protonull.civ.protocollib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class PooledPacketAdapters {
	private final List<PacketAdapter> adapters;

	public PooledPacketAdapters() {
		this.adapters = new ArrayList<>();
	}

	public void addAdapter(
		final @NotNull PacketAdapter adapter
	) {
		Preconditions.checkNotNull(adapter, "'adapter' cannot be null!");
		ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
		this.adapters.add(adapter);
	}

	public void removeAllAdapters() {
		this.adapters.forEach(ProtocolLibrary.getProtocolManager()::removePacketListener);
		this.adapters.clear();
	}
}
