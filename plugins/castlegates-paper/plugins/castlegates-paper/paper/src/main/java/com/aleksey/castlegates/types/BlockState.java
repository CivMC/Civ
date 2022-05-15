/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.types;

import org.bukkit.block.Block;

import com.aleksey.castlegates.database.ReinforcementInfo;

public class BlockState {
	public static final char Separator = '\n';

	private String _blockData;
	public String getBlockData() { return _blockData; }
	public int getBlockDataLen() { return _blockData.length() + 1; }

	private ReinforcementInfo _reinforcement;
	public void setReinforcement(ReinforcementInfo reinforcement) { _reinforcement = reinforcement; }
	public ReinforcementInfo getReinforcement() { return _reinforcement; }

	public BlockState(Block block, ReinforcementInfo reinforcement) {
		_blockData = block.getBlockData().getAsString();
		_reinforcement = reinforcement;
	}

	private BlockState(String blockData) {
		_blockData = blockData;
	}
	public void serialize(StringBuilder data) {
		data.append(_blockData);
		data.append(Separator);
	}

	public static BlockState deserialize(String blockData) {
		return new BlockState(blockData);
	}
}
