package com.aleksey.castlegates.types;

public class TimerLink {
	private final GearblockLink _link;
	private final boolean _mustDraw;

	public GearblockLink getLink() {
		return _link;
	}

	public boolean isMustDraw() {
		return _mustDraw;
	}

	public TimerLink(GearblockLink link, boolean mustDraw) {
		_link = link;
		_mustDraw = mustDraw;
	}
}
