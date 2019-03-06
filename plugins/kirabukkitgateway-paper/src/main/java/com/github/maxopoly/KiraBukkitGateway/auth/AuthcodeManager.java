package com.github.maxopoly.KiraBukkitGateway.auth;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

public class AuthcodeManager {

	private static final String validCharacters = "abcdefghijklmnopqrstuvwxyz0123456789";
	private static final int maxRecursionDepth = 20;

	private Set<String> currentCodes;
	private SecureRandom rng;
	private int codeLength;


	public AuthcodeManager(int codeLength) {
		currentCodes = new HashSet<>();
		rng = new SecureRandom();
		this.codeLength = codeLength;
	}

	public String getNewCode() {
		return getNewCode(0);
	}

	private String getNewCode(int recursion) {
		if (recursion >= maxRecursionDepth) {
			return null;
		}
		StringBuilder sb = new StringBuilder(codeLength);
		for(int i = 0; i < 10;i++) {
			int index = rng.nextInt(validCharacters.length());
			sb.append(validCharacters.charAt(index));
		}
		String result = sb.toString();
		if (currentCodes.contains(result)) {
			return getNewCode(recursion + 1);
		}
		currentCodes.add(result);
		return result;
	}
}
