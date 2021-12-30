package com.devotedmc.ExilePearl.core;

import com.devotedmc.ExilePearl.util.Clock;

final class CoreClock implements Clock {

	@Override
	public long getCurrentTime() {
		return System.currentTimeMillis();
	}
}
