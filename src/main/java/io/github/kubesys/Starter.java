/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.kubesys.analyzers.OperatingSystemAnalyzer;

/**
 * @author wuheng09@gmail.com
 * 
 * 
 **/
public class Starter {

	static String root                             = "E:/data/images";
	
//	static String root                             = "/var/lib/docker";

	static String serviceRegistry                  = "/image/overlay2/repositories.json";

	static String imageDatabase                    = "/image/overlay2/imagedb/content/";

	static String layerDatabase                    = "/image/overlay2/layerdb/";

	static String systemRoot                       = "/overlay2/cid/diff/";

	public static void main(String[] args) throws Exception {

		String name = args[0];
		String version = args[1];

		String imageId = getImageId(name, version);
		
		System.out.println("imageId: " + imageId);

		JsonNode imageDB = getImageDB(imageId);

		JsonNode imageLayers = getImageLayers(imageDB);
		
		for (int i = 0; i < imageLayers.size(); i++) {
			String layerId = imageLayers.get(i).asText();
			System.out.println("\t- layerId: " + layerId);
			BufferedReader br = new BufferedReader(new FileReader(
					new File(root + layerDatabase + layerId.replace(":", "/") + "/cache-id")));
			String cid = br.readLine();
			br.close();
			File file = new File(root + systemRoot.replace("cid", cid));
			System.out.println("\t- layerPath: " + file.getAbsolutePath());
			System.out.println("---------------------------------------");
//			extracted(file);
			OperatingSystemAnalyzer osa = new OperatingSystemAnalyzer(file);
			System.out.println(osa.analysis().toPrettyString());
			System.out.println("---------------------------------------");
		}
	}

	protected static JsonNode getImageLayers(JsonNode imageDb) {
		return imageDb.get("rootfs").get("diff_ids");
	}

	protected static JsonNode getImageDB(String imageId) throws Exception {
		return new ObjectMapper().readTree(new FileInputStream(
				new File(root + imageDatabase + imageId.replace(":", "/"))));
	}

	protected static String getImageId(String name, String version) throws Exception {
		JsonNode registry = new ObjectMapper().readTree(
				new FileInputStream(new File(root + serviceRegistry)));
		return registry.get("Repositories").get(name)
							.get(name + ":" + version).asText();
	}

	protected static void extracted(File file) {
		for (File f : file.listFiles()) {
			if (!f.isDirectory()) {
				System.out.println("\t" + f.getAbsolutePath());
			} else {
				extracted(f);
			}
		}
	}

}
