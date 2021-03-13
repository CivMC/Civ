package vg.civcraft.mc.civmodcore.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public final class Trie {

	private Map<Character, Trie> children;
	private String word;
	// instead of storing only the suffix and re-concatenating the original word for
	// every lookup we always store the full word and depth as a relative offset
	private int depth;
	//whether this non-leaf also represents the end of a word
	private boolean isEnd;

	public static Trie getNewTrie() {
		Trie trie = new Trie("", 0);
		trie.children = new HashMap<>();
		return trie;
	}
	
	private Trie(String word, int depth) {
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
				this.word = word.substring(0, depth);
			}
			else {
				isEnd = true;
			}
		}
		Trie targetNode = children.computeIfAbsent(wordToInsert.charAt(depth), c -> new Trie(wordToInsert, depth + 1));
		targetNode.insert(wordToInsert);
	}
	
	public List<String> match(String prefix) {
		List<String> result = new ArrayList<>();
		matchWord(prefix, result);
		return result;
	}
	
	public List<String> complete(String [] args) {
		String full = String.join(" ", args);
		List<String> matches = match(full);
		if (args.length < 2) {
			return matches;
		}
		int elementsToRemove = args.length - 1;
		for (int i = 0; i < matches.size(); i++) {
			String mod = matches.get(i);
			int startingSpot = StringUtils.ordinalIndexOf(mod, " ", elementsToRemove) + 1;
			matches.set(i, mod.substring(startingSpot));
		}
		return matches;
	}

	private void matchWord(String wordToMatch, List<String> result) {
		if (isLeaf()) {
			if (wordToMatch.length() <= this.word.length()) {
				result.add(word);
				return;
			}
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
			if (wordToMatch.length() <= depth) {
				//valid prefix from here on and deeper, so deep search and add everything below
				for(Trie subTrie : children.values()) {
					subTrie.matchWord(wordToMatch, result);
				}
				return;
			}
			Trie deeperNode = children.get(wordToMatch.charAt(depth));
			if (deeperNode != null) {
				deeperNode.matchWord(wordToMatch, result);
			}
		}
	}

}
