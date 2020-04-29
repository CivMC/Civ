package vg.civcraft.mc.civmodcore.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Trie {
	private Map<Character, Trie> children;
	private String word;
	// instead of storing only the suffix and re-concatenating the original word for
	// every lookup we always store the full word and depth as a relative offset
	private int depth;
	//whether this non-leaf also represents the end of a word
	private boolean isEnd;

	Trie(String word, int depth) {
		this.word = word;
		this.depth = depth;
		this.isEnd = false;
	}

	public boolean isLeaf() {
		return children == null;
	}

	public void insert(String wordToInsert) {
		if (isLeaf()) {
			if (word.equals(wordToInsert)) {
				return;
			}
			children = new HashMap<>();
			// insert current suffix
			if (word.length() > depth) {
				children.put(word.charAt(depth), new Trie(word, depth + 1));
			}
			else {
				isEnd = true;
			}
		}
		Trie targetNode = children.computeIfAbsent(wordToInsert.charAt(depth), c -> new Trie(wordToInsert, depth + 1));
		targetNode.insert(wordToInsert);
	}

	public void matchWord(String wordToMatch, List<String> result) {
		if (isLeaf()) {
			if (wordToMatch.length() > this.word.length()) {
				//we can not be a prefix if we are shorter
				return;
			}
			for(int i = depth; i < this.word.length(); i++) {
				if (wordToMatch.charAt(i) != this.word.charAt(i)) {
					return;
				}
			}
			result.add(this.word);
		} else {
			if (isEnd) {
				result.add(this.word);
			}
			Trie deeperNode = children.get(wordToMatch.charAt(depth));
			if (deeperNode != null) {
				matchWord(wordToMatch, result);
			}
		}
	}
}
