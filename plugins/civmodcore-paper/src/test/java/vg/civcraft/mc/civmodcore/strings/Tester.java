package vg.civcraft.mc.civmodcore.strings;

import java.security.SecureRandom;
import java.util.Random;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class Tester {

	private static final Logger LOGGER = LoggerFactory.getLogger(Tester.class.getSimpleName());
	private static final int LOOPS = 10_000_000;
	// To stave off accusations of unfairness, this is re-used
	private static final String FORMAT = "%s%s%s%s%s%s%s%s";
	private static final Random RANDOM = new SecureRandom();

	@Test
	public void stringConcatTester() {
		String holder = null;

		// Setup
		final long concatBeforeTime = System.currentTimeMillis();
		// Process
		for (int i = 0; i < LOOPS; i++) {
			holder = "" + RANDOM.nextDouble() + "hello" + i + RANDOM.nextInt() +
					"aJZg2dQTr0jGM9UFxznn" + RANDOM.nextFloat() + "zdpu0Du3UUcrGONfMWba";
		}
		// Results
		final long concatAfterTime = System.currentTimeMillis();
		final long concatTimeDelta = concatAfterTime - concatBeforeTime;
		LOGGER.info("String concat took: " + TextUtil.formatDuration(concatTimeDelta) +
				" (" + concatTimeDelta + "ms)");


		// Setup
		final long formatBeforeTime = System.currentTimeMillis();
		// Process
		for (int i = 0; i < LOOPS; i++) {
			holder = String.format(FORMAT,
					"", RANDOM.nextDouble(), "hello", i, RANDOM.nextInt(),
					"aJZg2dQTr0jGM9UFxznn", RANDOM.nextFloat(), "zdpu0Du3UUcrGONfMWba");
		}
		// Results
		final long formatAfterTime = System.currentTimeMillis();
		final long formatTimeDelta = formatAfterTime - formatBeforeTime;
		LOGGER.info("String.format() took: " + TextUtil.formatDuration(formatTimeDelta) +
				" (" + formatTimeDelta + "ms)");


		// Setup
		final long builderBeforeTime = System.currentTimeMillis();
		// Process
		for (int i = 0; i < LOOPS; i++) {
			holder = new StringBuilder(1000)
					.append("")
					.append(RANDOM.nextDouble())
					.append("hello")
					.append(i)
					.append(RANDOM.nextInt())
					.append("aJZg2dQTr0jGM9UFxznn")
					.append(RANDOM.nextFloat())
					.append("zdpu0Du3UUcrGONfMWba")
					.toString();
		}
		// Results
		final long builderAfterTime = System.currentTimeMillis();
		final long builderTimeDelta = builderAfterTime - builderBeforeTime;
		LOGGER.info("StringBuilder took: " + TextUtil.formatDuration(builderTimeDelta) +
				" (" + builderTimeDelta + "ms)");
	}

}
