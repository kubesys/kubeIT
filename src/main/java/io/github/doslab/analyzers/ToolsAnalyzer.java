/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.doslab.analyzers;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.doslab.Analyzer;

/**
 * @author  wuheng09@gmail.com
 * 
 * {
	"os": "centos",
	"version": "8",
	"url": "https://www.centos.org/",
	"tools": {
			"ping": "ping"
	}
   } 
 **/

public class ToolsAnalyzer extends Analyzer {
	
	
	public static String[] TOOLS_PATHS        = new String[] {"/usr/bin", "/usr/sbin", "/usr/local/bin", "/usr/local/sbin"};
	
	
	public ToolsAnalyzer(File file) {
		super(file);
	}

	@Override
	public JsonNode analysis() throws Exception {
		
		ObjectNode tools = new ObjectMapper().createObjectNode();
		
		for (String path : TOOLS_PATHS) {
			try {
				for (File f : new File(file, path).listFiles()) {
					tools.put(f.getName(), path + "/" + f.getName());
				}
			} catch (Exception ex) {
			}
		}
		
		return tools;
	}

	public static void main(String[] args) throws Exception {
		ToolsAnalyzer osa = new ToolsAnalyzer(null);
		System.out.println(osa.analysis());
	}
}
