/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.doslab;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.doslab.analyzers.ImageAnalyzer;
import io.github.doslab.analyzers.OSAnalyzer;
import io.github.doslab.analyzers.ToolsAnalyzer;
import io.github.doslab.applications.JarAnalyzer;


/**
 * @author wuheng09@gmail.com
 * 
 * 
 **/
public class DockerImage {

	public static String WIN_ROOT                             = "D:\\data\\tmp\\";
	
	public static String UNIX_ROOT                            = "/tmp/";
	
	public static String DIR                                  = System.getProperty("os.name").toLowerCase().startsWith("win") ? WIN_ROOT : UNIX_ROOT;
	
	public static String IMAGE_MANIFEST                       = "manifest.json";
	
	public static String IMAGE_REPO                           = "repositories";

	public static String INFO                                 = "image.info";
	
	public static String DATA                                 = "data";
	
	public static void main(String[] args) throws Exception {

		ObjectNode json = new ObjectMapper().createObjectNode();
		
		String name = args[0].replaceAll("/", "_");
		String vers = args[1];
		
		String root = DIR + name + "_" + vers;
		
		File repo = new File(root, IMAGE_REPO);
		
		String layerId = new ObjectMapper().readTree(repo).get(args[0]).get(args[1]).asText();
		System.out.println(layerId);
		
		
		
//		File rootFS = new File(dir, DATA);
//		
//		json.set("os", new OSAnalyzer(rootFS).analysis());
//		json.set("tools", new ToolsAnalyzer(rootFS).analysis());
//		json.set("image", new ImageAnalyzer(new File(dir, INFO)).analysis());
//		
//		String workbase = json.get("image").get("config").get("WorkingDir").asText();
//		if (workbase == null || workbase.length() == 0) {
//			return;
//		}
//		
//		ArrayNode depends = new ObjectMapper().createArrayNode();
//		extracted(depends, new File(rootFS, workbase));
//		json.set("depends", depends);
//		
//		
//		System.out.println(json.toPrettyString());
	}

	protected static void extracted(ArrayNode depends, File workbase) throws Exception {
		for (File f : workbase.listFiles()) {
			if (f.isDirectory()) {
				extracted(depends, f);
			} else {
				if (f.getName().endsWith("jar")) {
					depends.add(new JarAnalyzer(f).analysis());
				}
			}
		}
	}

}