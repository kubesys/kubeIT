/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.doslab.poseidon;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.doslab.poseidon.analyzers.JarAnalyzer;
import io.github.doslab.poseidon.analyzers.OSAnalyzer;
import io.github.doslab.poseidon.analyzers.ToolsAnalyzer;

/**
 * @author wuheng09@gmail.com
 * 
 * 
 **/
public class DockerRegistry {

	public static String WIN_ROOT = "D:\\data\\tmp\\";

	public static String UNIX_ROOT = "/tmp/";

	public static String DIR = System.getProperty("os.name").toLowerCase().startsWith("win") ? WIN_ROOT : UNIX_ROOT;

	public static String IMAGE_MANIFEST = "manifest.json";

	public static String IMAGE_REPO = "repositories";

	public static String LAYER_JSON = "json";

	public static String DATA = "data";

	public static void main(String[] args) throws Exception {

		ObjectNode node = new ObjectMapper().createObjectNode();

		String name = args[0].replaceAll("/", "_");
		String vers = args[1];

		String root = DIR + name + "_" + vers;

		for (File file : new File(root).listFiles()) {
			if (file.getName().endsWith("json") && !file.getName().equals("manifest.json")) {

				node.put("apiVersion", "doslab.io/v1");
				node.put("kind", "Image");

				node.put("type", "Container");

				ObjectNode meta = new ObjectMapper().createObjectNode();
				meta.put("name", args[0].replaceAll("/", "-") + "-" + args[1]);

				node.set("metadata", meta);

				ObjectNode spec = new ObjectMapper().createObjectNode();

				spec.put("id", file.getName().substring(0, file.getName().length() - 5));
				spec.put("name", args[0] + ":" + args[1]);
				JsonNode json = new ObjectMapper().readTree(file);
				spec.put("arch", json.get("architecture").asText());
				spec.set("config", json.get("config"));
				spec.put("created", json.get("created").asText());
				spec.put("docker_version", json.get("docker_version").asText());
				spec.set("history", json.get("history"));
				node.set("spec", spec);

				String fullInfo = json.get("history").get(0).get("created_by").asText();
				spec.put("parent", fullInfo.split("\\s+")[4].substring(5));
				System.out.println(node.toPrettyString());
			}
		}

		File repo = new File(root, IMAGE_REPO);
		String layerId = new ObjectMapper().readTree(repo).get(args[0]).get(args[1]).asText();

		scanLayer(node, root, layerId);

		System.out.println(node.toPrettyString());

	}

	public static void scanLayer(ObjectNode node, String root, String layerId) throws Exception {
		File layer = new File(root, layerId);
		File desc = new File(layer, LAYER_JSON);
		JsonNode json = new ObjectMapper().readTree(desc);

		try {
			node.set("osInfo", new OSAnalyzer(layer).analysis());
			node.set("sysTools", new ToolsAnalyzer(layer).analysis());
		} catch (Exception ex) {
			// ignore here
		}

		if (json.has("parent")) {
			scanLayer(node, root, json.get("parent").asText());
		}
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
