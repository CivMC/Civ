package com.github.maxopoly.essenceglue;

public class VotingSite {

	private final String votingUrl;
	private final String name;
	private final String internalKey;
	private final long votingCooldown;

	public VotingSite(String name, String votingUrl, String internalKey, long votingCooldown) {
		this.name = name;
		this.votingCooldown = votingCooldown;
		this.votingUrl = votingUrl;
		this.internalKey = internalKey;
	}

	public String getVotingUrl() {
		return votingUrl;
	}

	public String getName() {
		return name;
	}

	public String getInternalKey() {
		return internalKey;
	}

	public long getVotingCooldown() {
		return votingCooldown;
	}

}
