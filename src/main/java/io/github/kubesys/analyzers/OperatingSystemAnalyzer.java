/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.analyzers;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.kubesys.Analyzer;

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

public class OperatingSystemAnalyzer extends Analyzer {

	
	public static String OS_CONFIG_FILE       = "etc/os-release";
	
	public static String KEY_OS_NAME          = "name";
	
	public static String KEY_OS_VERSION       = "version";
	
	public static String KEY_OS_URL           = "url";
	
	public static String KEY_OS_TOOLS         = "tools";
	
	public static String[] TOOLS_PATHS        = new String[] {"/usr/bin", "/usr/sbin", "/usr/local/bin", "/usr/local/sbin"};
	
	
	/**
	 * app info
	 */
	protected ObjectNode os = new ObjectMapper().createObjectNode();
	
	/**
	 * dependency info
	 */
	protected ObjectNode tools = new ObjectMapper().createObjectNode();
	
	public OperatingSystemAnalyzer(File file) {
		super(file);
	}

	@SuppressWarnings("deprecation")
	@Override
	public JsonNode analysis() throws Exception {
		File osinfo = new File(file, OS_CONFIG_FILE);
		Properties props = getProps(new FileInputStream(
							osinfo), "=");
		os.put(KEY_OS_NAME, props.getProperty("ID"));
		os.put(KEY_OS_VERSION, props.getProperty("VERSION_ID"));
		os.put(KEY_OS_URL, props.getProperty("HOME_URL"));
		
		for (String path : TOOLS_PATHS) {
			for (File f : new File(file, path).listFiles()) {
				tools.put(f.getName(), f.getName());
			}
		}
		
		os.put(KEY_OS_TOOLS, tools);
		
		return os;
	}

	public static void main(String[] args) throws Exception {
		OperatingSystemAnalyzer osa = new OperatingSystemAnalyzer(null);
		System.out.println(osa.analysis());
	}
}
