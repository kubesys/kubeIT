/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.doslab.analyzers;

import java.io.File;
import java.io.FileInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

public class ImageInfoAnalyzer extends Analyzer {

	public ImageInfoAnalyzer(File file) {
		super(file);
	}

	@Override
	public JsonNode analysis() throws Exception {
		return new ObjectMapper().readTree(new FileInputStream(file));
	}

	
	
}
