/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.doslab.poseidon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultClientConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author wuheng09@gmail.com
 * 
 * 
 **/
public class DockerHub {

	public final static String REPO_URL = "https://hub.docker.com/v2/repositories/library/#NAME#/tags/?page_size=1000&page=1&ordering=last_updated";

	public final static String IMG_URL  = "https://hub.docker.com/v2/repositories/library/#NAME#/tags/#TAG#/images";

	public final static String KEY_NAME = "#NAME#";
	
	public final static String KEY_TAG = "#TAG#";
	
	public final static ArrayNode node = new ObjectMapper().createArrayNode();
	
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File("repos/dockerhub")));
		String line = null;
		while ((line = br.readLine()) != null) {
			JsonNode imagesWithAllTags = getResult(REPO_URL.replace(KEY_NAME, line)).get("results");
			for (int i = 0; i < imagesWithAllTags.size(); i++) {
				
				String tag = imagesWithAllTags.get(i).get("name").asText();
				String detailUrl = IMG_URL.replace(KEY_NAME, line)
								.replace(KEY_TAG, tag);
				JsonNode imagesWithAllDetails = getResult(detailUrl);
				
				JsonNode imagesWithAllArch = imagesWithAllTags.get(i).get("images");
				for (int j = 0; j < imagesWithAllArch.size(); j++) {
					JsonNode summary = imagesWithAllArch.get(j);
					String arch = summary.get("architecture").asText();
					
					for (int k = 0; k < imagesWithAllDetails.size(); k++) {
						JsonNode detail = imagesWithAllDetails.get(k);
						if (detail.get("architecture").asText().equals(arch)) {
							ObjectNode json = new ObjectMapper().createObjectNode();
							json.put("apiVersion", "doslab.io/v1");
							json.put("kind", "Knowledge");
							json.put("type", "Dockerhub");
							{
								ObjectNode meta = new ObjectMapper().createObjectNode();
								meta.put("name", line + "-" + summary.get("architecture").asText());
								json.set("metadata", meta);
							}
							{
								ObjectNode spec = new ObjectMapper().createObjectNode();
								spec.put("image", line + ":" + tag);
								spec.put("arch", summary.get("architecture").asText());
								spec.put("os", summary.get("os").asText());
								spec.put("digest", summary.get("digest").asText());
								spec.put("size", summary.get("size").asText());
								spec.put("status", summary.get("status").asText());
								spec.put("last_pushed", summary.get("last_pushed").asText());
								spec.put("detail", detailUrl);
								spec.put("hashcode", hashCode(detail));
								json.set("spec", spec);
							}
							System.out.println(json.toPrettyString());
							
							File root = new File("values", line);
							if (!root.exists()) {
								root.mkdirs();
							}
							
							FileWriter fw = new FileWriter(new File(root, 
									line + "-" + tag 
									+ "-" + summary.get("architecture").asText()));
							fw.write(json.toPrettyString());
							fw.close();
						}
					}
				}
			}
		}
		br.close();
	}

	public static JsonNode getResult(String url) throws IOException, ClientProtocolException {
		CloseableHttpClient http = createDefaultHttpClient();
		JsonNode json = new ObjectMapper().readTree(http.execute(
				new HttpGet(url)).getEntity().getContent());
		http.close();
		return json;
	}

	protected static CloseableHttpClient createDefaultHttpClient() {

		SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setSoTimeout(0).setSoReuseAddress(true)
				.build();

		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(0).setConnectionRequestTimeout(0)
				.setSocketTimeout(0).build();

		return createDefaultHttpClientBuilder().setConnectionTimeToLive(0, TimeUnit.SECONDS)
				.setDefaultSocketConfig(socketConfig).setDefaultRequestConfig(requestConfig)
				.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
				.setConnectionReuseStrategy(new DefaultClientConnectionReuseStrategy())
				.setServiceUnavailableRetryStrategy(new DefaultServiceUnavailableRetryStrategy()).build();
	}
	
	protected static HttpClientBuilder createDefaultHttpClientBuilder() {
		return HttpClients.custom();
	}
	
	protected static String hashCode(JsonNode json) {
		StringBuilder sb = new StringBuilder();
		JsonNode layers = json.get("layers");
		for (int i = 0; i < layers.size(); i ++) {
			sb.append(layers.get(i).get("size").asInt() + "+");
		}
		return sb.substring(0, sb.length() - 1);
	}
}
