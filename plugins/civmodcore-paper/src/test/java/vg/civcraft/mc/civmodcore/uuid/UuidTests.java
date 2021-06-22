package vg.civcraft.mc.civmodcore.uuid;

import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import vg.civcraft.mc.civmodcore.utilities.UuidUtils;

public class UuidTests {

	@Test
	public void testUuidSerialization() {
		// Setup
		final UUID testUuid = UUID.randomUUID();
		// Process
		final byte[] uuidBytes = UuidUtils.uuidToBytes(testUuid);
		final UUID rebuildUuid = UuidUtils.bytesToUUID(uuidBytes);
		// Check
		Assert.assertEquals(testUuid, rebuildUuid);
	}

}
