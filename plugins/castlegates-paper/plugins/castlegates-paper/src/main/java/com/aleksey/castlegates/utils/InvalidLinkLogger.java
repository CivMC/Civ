/**
 * Created by Aleksey on 28.06.2017.
 */

package com.aleksey.castlegates.utils;

import com.aleksey.castlegates.database.LinkInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InvalidLinkLogger {
    private static final DateFormat fileDateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
    private static final String folderName = "castlegates_logs";

    private String filePath;

    public String write(List<LinkInfo> links) {
        try {
            PrintWriter writer = getWriter();

            try {
                for(LinkInfo link : links) {
                    writeLink(link, writer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.filePath;
    }

    private static void writeLink(LinkInfo link, PrintWriter writer) {
        writer.print("link_id=");
        writer.print(Integer.toString(link.link_id));

        writer.print(",gearblock1_id=");
        writer.print(link.gearblock1_id != null ? link.gearblock1_id.toString() : "none");

        writer.print(",gearblock2_id=");
        writer.print(link.gearblock2_id != null ? link.gearblock2_id.toString() : "none");

        writer.println();
    }

    private PrintWriter getWriter() throws FileNotFoundException {
        File folder = new File(folderName);

        if(!folder.exists())
            folder.mkdirs();

        this.filePath = folder + "/InvalidLinks_" + fileDateFormat.format(new Date()) + ".txt";

        File file = new File(this.filePath);

        PrintWriter writer = file.exists()
                ? new PrintWriter(new FileOutputStream(file, true))
                : new PrintWriter(this.filePath);

        return writer;
    }
}
