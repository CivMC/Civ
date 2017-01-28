/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.aleksey.castlegates.CastleGates;

public class ResourceHelper {
	public static ArrayList<String> readScriptList(String resourcePath) {
		InputStream stream = CastleGates.class.getResourceAsStream(resourcePath);

		if(stream == null) return null;

    	StringBuilder script = new StringBuilder("");
    	ArrayList<String> list = new ArrayList<String>();

    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    		String line;

            while ((line = reader.readLine()) != null) {
            	if(line.endsWith(";")) {
            		script.append(line.substring(0, line.length() - 1));
            		list.add(script.toString());
            		script.delete(0, script.length());
            	}
            	else {
                	script.append(line);
                	script.append("\n");
            	}
            }

            if(script.length() > 0) {
            	list.add(script.toString());
            }
    	} catch (IOException e) {
    		e.printStackTrace();
    	}

    	return list;
	}
}
