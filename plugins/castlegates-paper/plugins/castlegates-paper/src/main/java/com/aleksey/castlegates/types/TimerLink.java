package com.aleksey.castlegates.types;

public class TimerLink {
	private GearblockLink link;
	private boolean mustDraw;

	public GearblockLink getLink() {
		return this.link;
	}

	public boolean isMustDraw() {
		return this.mustDraw;
	}

	public TimerLink(GearblockLink link, boolean mustDraw) {
		this.link = link;
		this.mustDraw = mustDraw;
	}
}
