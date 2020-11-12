/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.doslab.analyzers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Properties;

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

public class OSAnalyzer extends Analyzer {

	
	public static String OS_CONFIG_FILE                       = "etc/os-release";
	
	public static String KEY_OS_NAME                          = "name";
	
	public static String KEY_OS_VERSION                       = "version";
	
	public static String KEY_OS_URL                           = "url";
	
	public OSAnalyzer(File file) {
		super(file);
	}

	@Override
	public JsonNode analysis() throws Exception {
		
		ObjectNode osInfo = new ObjectMapper().createObjectNode();
		
		Properties props = getProps(new FileInputStream(getOSConfig()), "=");
		osInfo.put(KEY_OS_NAME, props.getProperty("ID"));
		osInfo.put(KEY_OS_VERSION, props.getProperty("VERSION_ID"));
		osInfo.put(KEY_OS_URL, props.getProperty("HOME_URL"));
		
		return osInfo;
	}

	@SuppressWarnings("resource")
	protected File getOSConfig() throws Exception {
		File osinfo = new File(file, OS_CONFIG_FILE);
		Properties props = getProps(new FileInputStream(osinfo), "=");
		
		if (props.size() == 0) {
			BufferedReader br = new BufferedReader(new FileReader(osinfo));
			osinfo = osinfo.getParentFile();
			String line = null;
			while ((line = br.readLine()) != null) {
				while (line.startsWith("../")) {
					osinfo = osinfo.getParentFile();
					line = line.substring("../".length());
				}
				return new File(osinfo, line);
			}
			br.close();
		} 
		
		return osinfo;
	}
}
