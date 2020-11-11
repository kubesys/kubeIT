/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.doslab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.doslab.analyzers.ImageInfoAnalyzer;
import io.github.doslab.analyzers.OperatingSystemAnalyzer;

/**
 * @author wuheng09@gmail.com
 * 
 * 
 **/
public class Starter {

	public static String WIN_ROOT                             = "E:/data/images";
	
	public static String UNIX_ROOT                            = "/var/lib/docker";

	public static String ROOT                                 = System.getProperty("os.name").toLowerCase().startsWith("win") ? WIN_ROOT : UNIX_ROOT;
	
	public static String serviceRegistry                      = "/image/overlay2/repositories.json";

	public static String imageDatabase                        = "/image/overlay2/imagedb/content/";

	public static String layerInfo                            = "/image/overlay2/distribution/v2metadata-by-diffid/";
	
	public static String layerDigest                          = "/image/overlay2/distribution/diffid-by-digest/";
	
	public static String layerDatabase                        = "/image/overlay2/layerdb/";

	public static String systemRoot                           = "/overlay2/cid/diff/";

	public static void main(String[] args) throws Exception {

		ObjectNode json = new ObjectMapper().createObjectNode();
		
		String name = args[0];
		String version = args[1];

		String imageId = getImageId(name, version);
		System.out.println("imageId: " + imageId);
		
		File imageFile = getImageFile(imageId);
		ImageInfoAnalyzer imageAnalyzer = new ImageInfoAnalyzer(imageFile);
		
		JsonNode imageDB = imageAnalyzer.analysis();
		
		json.set("metadata", imageDB);
		
		System.out.println(imageDB.toPrettyString());
		
		JsonNode imageLayers = getImageLayers(imageDB);
		
		for (int i = 0; i < imageLayers.size(); i++) {
			String uuid = imageLayers.get(i).asText();
			JsonNode layer = new ObjectMapper().readTree(new File(ROOT + layerInfo, uuid.replace(":", "/")));
			String digest = layer.get(0).get("Digest").asText();

			String layerId = null;
			
			{
				BufferedReader br = new BufferedReader(new FileReader(new File(ROOT + layerDigest, digest.replace(":", "/"))));
				String line = null;
				while ((line = br.readLine()) != null) {
					layerId = line;
					break;
				}
				
			}
					
			System.out.println("\t- layerId: " + layerId);
			BufferedReader br = new BufferedReader(new FileReader(
					new File(ROOT + layerDatabase + layerId.replace(":", "/") + "/cache-id")));
			String cid = br.readLine();
			br.close();
			File file = new File(ROOT + systemRoot.replace("cid", cid));
			System.out.println("\t- layerPath: " + file.getAbsolutePath());
			System.out.println("---------------------------------------");
			OperatingSystemAnalyzer osa = new OperatingSystemAnalyzer(file);
			System.out.println(osa.analysis().toPrettyString());
			System.out.println("---------------------------------------");
		}
	}

	protected static String getImageId(String name, String version) throws Exception {
		JsonNode registry = new ObjectMapper().readTree(
				new FileInputStream(new File(ROOT + serviceRegistry)));
		return registry.get("Repositories").get(name)
							.get(name + ":" + version).asText();
	}
	
	protected static File getImageFile(String imageId) {
		return new File(ROOT + imageDatabase + imageId.replace(":", "/"));
	}

	protected static JsonNode getImageLayers(JsonNode imageDb) {
		return imageDb.get("rootfs").get("diff_ids");
	}

}
