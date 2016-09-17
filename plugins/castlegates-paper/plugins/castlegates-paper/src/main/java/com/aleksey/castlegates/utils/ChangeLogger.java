package com.aleksey.castlegates.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.aleksey.castlegates.database.GearblockInfo;
import com.aleksey.castlegates.database.ReinforcementInfo;
import com.aleksey.castlegates.types.BlockState;
import com.aleksey.castlegates.types.GearblockForUpdate;
import com.aleksey.castlegates.types.LinkForUpdate;

public class ChangeLogger {
	private static final DateFormat lineDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final DateFormat fileDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH");
	private static final String folderName = "castlegates_logs";
	
	private File folder;
	private String fileName;
	private PrintWriter writer;
	
	public void flush() {
		try {
			if(this.writer != null) {
				this.writer.flush();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void write(GearblockForUpdate gearForUpdate) {
		try {
			PrintWriter writer = getWriter();
			
			String lineDate = lineDateFormat.format(new Date()) + " - ";
			
			writer.print(lineDate);

			if(gearForUpdate.info != null) {
				GearblockInfo info = gearForUpdate.info;
				
				if(info.gearblock_id > 0) {
					writer.print("GEARBLOCK_UPDATE:id=");
					writer.print(info.gearblock_id);
					writer.print(",");
				} else {
					writer.print("GEARBLOCK_INSERT:");
				}
				
				writer.print("worlduid=");
				writer.print(info.location_worlduid);
				writer.print(",x=");
				writer.print(info.location_x);
				writer.print(",y=");
				writer.print(info.location_y);
				writer.print(",z=");
				writer.print(info.location_z);
			} else {
				writer.print("GEARBLOCK_DELETE:id=");
				writer.print(gearForUpdate.original.getId());
			}
			
			writer.println();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void write(LinkForUpdate linkForUpdate) {
		try {
			PrintWriter writer = getWriter();
			
			String lineDate = lineDateFormat.format(new Date()) + " - ";
			
			writer.print(lineDate);
		
			if(!linkForUpdate.original.isRemoved()) {
				int linkId = linkForUpdate.original.getId();
				String prefix;
				
				if(linkId > 0) {
					writer.print("LINK_UPDATE:id=");
					writer.print(linkId);
					writer.print(",");
					
					prefix = "LINK_UPDATE";
				} else {
					writer.print("LINK_INSERT:");
					
					prefix = "LINK_INSERT";
				}
				
				writeLinkGearblocks(linkForUpdate, writer);
				writeLinkBlocks(linkForUpdate, writer, lineDate, prefix);
				writeLinkReinforcements(linkForUpdate, writer, lineDate, prefix);
			} else {
				writer.print("LINK_DELETE:id=");
				writer.println(linkForUpdate.original.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	private static void writeLinkGearblocks(LinkForUpdate linkForUpdate, PrintWriter writer) {
		writer.print("gearblock1=");
		
		if(linkForUpdate.gearblock1 != null) {
			writer.print(linkForUpdate.gearblock1.getId());
		} else {
			writer.print("NONE");
		}

		writer.print(",gearblock2=");
		
		if(linkForUpdate.gearblock2 != null) {
			writer.print(linkForUpdate.gearblock2.getId());
		} else {
			writer.print("NONE");
		}
		
		writer.println();
	}
	
	private static void writeLinkBlocks(LinkForUpdate linkForUpdate, PrintWriter writer, String lineDate, String prefix) {
		if(linkForUpdate.blocks == null || linkForUpdate.blocks.length == 0) {
			return;
		}
		
		List<BlockState> blocks = DataWorker.deserializeBlocks(linkForUpdate.blocks);
		
		writer.print(lineDate);
		writer.print(prefix + ".BLOCKS=");
		
		for(int i = 0; i < blocks.size(); i++) {
			if(i > 0) {
				writer.print(",");
			}
			
			BlockState block = blocks.get(i);
			
			writer.print(block.id + ":" + block.meta);
		}
		
		writer.println();
	}
	
	private static void writeLinkReinforcements(LinkForUpdate linkForUpdate, PrintWriter writer, String lineDate, String prefix) {
		if(linkForUpdate.reinforcements == null || linkForUpdate.reinforcements.size() == 0) {
			return;
		}
		
		for(ReinforcementInfo rein : linkForUpdate.reinforcements) {
			writer.print(lineDate);
			writer.print(prefix + ".REIN:");
			writer.print("block_no=" + rein.block_no);
			writer.print(",material_id=" + rein.material_id);
			writer.print(",durability=" + rein.durability);
			writer.print(",insecure=" + rein.insecure);
			writer.print(",group_id=" + rein.group_id);
			writer.print(",maturation_time=" + rein.maturation_time);
			writer.print(",lore=" + rein.lore);
			writer.print(",acid_time=" + rein.acid_time);
			writer.println();
		}
	}
	
	public void close() {
		if(this.writer != null) {
			this.writer.close();
			this.writer = null;
		}
	}
	
	private PrintWriter getWriter() throws FileNotFoundException {
		if(this.folder == null) {
			this.folder = new File(folderName);
			
			if(!this.folder.exists())
				this.folder.mkdirs();
		}
		
		String fileName = fileDateFormat.format(new Date()) + ".txt";
		
		if(this.writer == null || !this.fileName.equalsIgnoreCase(fileName)) {
			if(this.writer != null) {
				this.writer.close();
			}
			
			this.fileName = fileName;
			
			File file = new File(this.folder + "/" + this.fileName);
			
			this.writer = file.exists()
					? new PrintWriter(new FileOutputStream(file, true))
					: new PrintWriter(this.folder + "/" + this.fileName);
		}
		
		return this.writer;
	}
}
