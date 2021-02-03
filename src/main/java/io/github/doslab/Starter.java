/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.doslab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
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
public class Starter {

	public static String WIN_ROOT                             = "D:\\data\\images\\docker\\registry\\v2";
	
	public static String REPO                                 = "/repositories/";
	
	public static String BLOB                                 = "/blobs/";
	
	public static String INDEX                                = "/current/link";
	
	public static String VERSION                              = "/_manifests/tags/";
	
	public static String DATA                                 = "/data";
	
	
	
	public static String UNIX_ROOT                            = "/tmp/";

	public static String ROOT                                 = System.getProperty("os.name").toLowerCase().startsWith("win") ? WIN_ROOT : UNIX_ROOT;
	
	public static String INFO                                 = "image.info";
	
	public static void main(String[] args) throws Exception {
		
		String image = "doslab/kubernetes-api-mapper:v1.6.0-amd64";
		
		String sha = getSha(image);
		
		System.out.println(sha);
		
		int idx = sha.indexOf(":");
		String path = WIN_ROOT + BLOB + sha.substring(0, idx) + "/"
						+ sha.substring(idx + 1, idx + 3) + "/"
						+ sha.substring(idx + 1) + DATA;
		
		JsonNode json = new ObjectMapper().readTree(new File(path));
		System.out.println(json.toPrettyString());

		
//		ObjectNode json = new ObjectMapper().createObjectNode();
//		String     name = args[0].replaceAll("/", "_");
//		String     vers = args[1];
//		
//		String dir = ROOT + name + "_" + vers;
//		
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

	public static String getSha(String image) throws Exception {
		int stx = image.indexOf("/");
		int etx = image.indexOf(":");
		
		String path = WIN_ROOT + REPO 
					+ (stx == -1 ? image.substring(0, etx) : image.substring(0, stx) + "/" + image.substring(stx + 1, etx)) 
					+ VERSION + image.substring(etx + 1) + INDEX;
		
		String sha = read(path);
		return sha;
	}

	public static String read(String path) throws Exception {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		return sb.toString();
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
