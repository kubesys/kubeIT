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

public class OperatingSystemAnalyzer extends Analyzer {

	
	public static String OS_CONFIG_FILE       = "etc/os-release";
	
	public static String KEY_OS_NAME          = "name";
	
	public static String KEY_OS_VERSION       = "version";
	
	public static String KEY_OS_URL           = "url";
	
	public static String KEY_OS               = "os";
	
	public static String KEY_SYSTEM           = "system";
	
	public static String[] TOOLS_PATHS        = new String[] {"/usr/bin", "/usr/sbin", "/usr/local/bin", "/usr/local/sbin"};
	
	
	/**
	 * app info
	 */
	protected ObjectNode os = new ObjectMapper().createObjectNode();
	
	/**
	 * dependency info
	 */
	protected ObjectNode system = new ObjectMapper().createObjectNode();
	
	public OperatingSystemAnalyzer(File file) {
		super(file);
	}

	@SuppressWarnings("deprecation")
	@Override
	public JsonNode analysis() throws Exception {
		File osinfo = new File(file, OS_CONFIG_FILE);
		Properties props = getProps(new FileInputStream(
							osinfo), "=");
		
		
		ObjectNode osInfo = new ObjectMapper().createObjectNode();
		
		if (props.size() == 0) {
			BufferedReader br = new BufferedReader(new FileReader(osinfo));
			osinfo = osinfo.getParentFile();
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				while (line.startsWith("../")) {
					osinfo = osinfo.getParentFile();
					line = line.substring("../".length());
				}
				
				osinfo = new File(osinfo, line);
				props = getProps(new FileInputStream(
						osinfo), "=");
				osInfo.put(KEY_OS_NAME, props.getProperty("ID"));
				osInfo.put(KEY_OS_VERSION, props.getProperty("VERSION_ID"));
				osInfo.put(KEY_OS_URL, props.getProperty("HOME_URL"));
			}
		} else {
			osInfo.put(KEY_OS_NAME, props.getProperty("ID"));
			osInfo.put(KEY_OS_VERSION, props.getProperty("VERSION_ID"));
			osInfo.put(KEY_OS_URL, props.getProperty("HOME_URL"));
		}
		os.set(KEY_OS, osInfo);
		
		for (String path : TOOLS_PATHS) {
			try {
				for (File f : new File(file, path).listFiles()) {
					system.put(f.getName(), f.getName());
				}
			} catch (Exception ex) {
				// ignore here
			}
		}
		
		os.set(KEY_SYSTEM, system);
		
		return os;
	}

	public static void main(String[] args) throws Exception {
		OperatingSystemAnalyzer osa = new OperatingSystemAnalyzer(null);
		System.out.println(osa.analysis());
	}
}
