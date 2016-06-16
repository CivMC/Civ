/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.types;

import org.bukkit.block.Block;

import com.aleksey.castlegates.DeprecatedMethods;
import com.aleksey.castlegates.database.ReinforcementInfo;

public class BlockState {
	public static final int BytesPerBlock = 3;
	
	public int id;
	public byte meta;
	public ReinforcementInfo reinforcement;
	
	public BlockState() {
	}
	
	public BlockState(Block block) {
		this.id = DeprecatedMethods.getTypeId(block);
		this.meta = DeprecatedMethods.getMeta(block);
	}
	
	public int serialize(byte[] data, int offset) {
		data[offset++] = (byte)(this.id >> 8);
		data[offset++] = (byte)this.id;
		data[offset++] = this.meta;
		
		return offset;
	}
	
	public static int deserialize(byte[] data, int offset, BlockState block) {
		block.id = ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
		block.meta = data[offset + 2];
		
		return offset + 3;
	}
}
