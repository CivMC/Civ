package vg.civcraft.mc.civmodcore.scoreboard.side;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ScoreBoardAPI {
	
	private static Map<UUID, Integer> openScores = new TreeMap<>();
	private static Map<String, CivScoreBoard> boards = new HashMap<>();
	private static final Map<UUID, String> HEADERS = new HashMap<>();
	private static String DEFAULT_HEADER = "  Info  ";
	
	public static CivScoreBoard createBoard(String key) {
		CivScoreBoard board = new CivScoreBoard(key);
		boards.put(key, board);
		return board;
	}
	
	public static CivScoreBoard getBoard(String key) {
		return boards.get(key);
	}
	
	public static void deleteBoard(CivScoreBoard board) {
		boards.remove(board.getName());
		board.tearDown();
	}
	
	public static void setScoreBoardHeader(Player player, String header) {
		if (header == null) {
			setScoreBoardHeader(player, DEFAULT_HEADER);
			return;
		}
		HEADERS.put(player.getUniqueId(), header);
		updateAllBoards(player);
	}
	
	public static void setDefaultHeader(String header) {
		Preconditions.checkNotNull(header);
		DEFAULT_HEADER = header;
	}
	
	static String getBoardHeader(UUID uuid) {
		return HEADERS.getOrDefault(uuid, DEFAULT_HEADER);
	}
	
	static void updateAllBoards(Player p) {
		for(CivScoreBoard board : boards.values()) {
			board.set(p, board.get(p));
		}
	}
	
	static void purge(Player p) {
		for(CivScoreBoard board : boards.values()) {
			board.purge(p);
		}
		openScores.remove(p.getUniqueId());
	}
	
	static void adjustScore(UUID uuid, int amount) {
		Integer current = openScores.get(uuid);
		if (current == null) {
			current = 0;
		}
		current += amount;
		if (current <= 0) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) {
				resetPlayer(p);
			}
			openScores.remove(uuid);
		}
		else {
			openScores.put(uuid, current);
		}
	}
	
	static void resetPlayer(Player p) {
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

}
