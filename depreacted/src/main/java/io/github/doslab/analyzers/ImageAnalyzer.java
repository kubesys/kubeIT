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

public class ImageAnalyzer extends Analyzer {

	
	public ImageAnalyzer(File file) {
		super(file);
	}

	public JsonNode analysis() throws Exception {
		JsonNode origin = new ObjectMapper().readTree(file);
		
		ObjectNode json = new ObjectMapper().createObjectNode();
		json.set("architecture", origin.get("architecture"));
		json.set("config", origin.get("config"));
		return json;
	}

}
