/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.analyzers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.kubesys.Analyzer;

/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
/**
 * @author henry
 *
 */
public class ApplicationAnalyzer extends Analyzer {

	public ApplicationAnalyzer(File file) {
		super(file);
	}

	@Override
	public JsonNode analysis() throws Exception {

		ObjectNode node = new ObjectMapper().createObjectNode();
		ArrayNode deps = new ObjectMapper().createArrayNode();

		if (file.getName().endsWith("jar")) {
			node.put("language", "Java");
			node.put("name", file.getName());

			File dir = mkdir(file.getParentFile());
			unzipTo(file.getAbsolutePath(), dir);

			Properties props = getJarProp(new FileInputStream(new File(dir.getAbsolutePath(), "META-INF/MANIFEST.MF")));

			if (props.get("Spring-Boot-Version") != null) {
				node.put("appType", "SpringBoot " + props.get("Spring-Boot-Version").toString());
				node.put("buildJDK", props.get("Build-Jdk-Spec").toString());
				addSpringBootDep(deps, new File(dir, props.get("Spring-Boot-Lib").toString()));
			} else {
				node.put("appType", props.get("Created-By").toString());
				node.put("buildJDK", props.get("Build-Jdk").toString());
				addMavenDep(deps, new File(dir, "META-INF"));
			}

			node.set("dependOn", deps);

			deleteDir(dir);
		}

		return node;
	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	/****************************************************************************
	 * 
	 * 
	 * Java related
	 * 
	 ****************************************************************************/

	/**
	 * @param deps deps
	 * @param dir  dir
	 * @throws Exception exception
	 */
	protected void addSpringBootDep(ArrayNode deps, File dir) throws Exception {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				addSpringBootDep(deps, f);
			} else if (f.getAbsolutePath().endsWith("jar")) {
				try {
					System.out.println("Now is " + f.getAbsolutePath());
					ApplicationAnalyzer aa = new ApplicationAnalyzer(f.getAbsoluteFile());
					JsonNode jn = aa.analysis().get("dependOn").get(0);
					ObjectNode item = new ObjectMapper().createObjectNode();
					item.put("groupId", jn.get("groupId").asText());
					item.put("artifactId", jn.get("artifactId").asText());
					item.put("version", jn.get("version").asText());
					deps.add(item);
				} catch (Exception e) {
					System.err.println("not maven, may be bunlde: " + f.getName());
					ObjectNode item = new ObjectMapper().createObjectNode();
					int idx = f.getName().lastIndexOf("-");
					int edx = f.getName().lastIndexOf(".");
					String artifactId = f.getName().substring(0, idx);
					String version = f.getName().substring(idx + 1, edx);

					File tempdir = mkdir(f.getParentFile());
					unzipTo(f.getAbsolutePath(), tempdir);

					String groupId = getGroupId(tempdir.getAbsoluteFile());

					groupId = (groupId.length() == 0) ? artifactId : groupId;

					groupId = groupId.endsWith(".") ? groupId.substring(0, groupId.length() - 1) : groupId;

					item.put("groupId", groupId);
					item.put("artifactId", artifactId);
					item.put("version", version);
					deleteDir(tempdir);
					deps.add(item);
				}
			}

		}
	}

	protected String getGroupId(File tempdir) {
		String groupId = "";
		for (File tf : tempdir.listFiles()) {
			if (tf.isDirectory() && !tf.getName().endsWith("INF")) {
				if (tf.list().length == 1) {
					return tf.getName() + "." + getGroupId(tf.getAbsoluteFile());
				} else {
					return tf.getName();
				}
			}
		}
		return groupId;
	}

	/**
	 * @param deps deps
	 * @param dir  dir
	 * @throws Exception exception
	 */
	protected void addMavenDep(ArrayNode deps, File dir) throws Exception {

		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				addMavenDep(deps, f);
			} else if (f.getAbsolutePath().endsWith("pom.xml")) {
				Element root = getDocument(f);
				NodeList nl = root.getElementsByTagName("parent");

				if (nl.getLength() != 0) {
					for (int i = 0; i < nl.getLength(); i++) {
						NodeList nn = nl.item(i).getChildNodes();
						ObjectNode item = new ObjectMapper().createObjectNode();
						for (int j = 0; j < nn.getLength(); j++) {

							if (nn.item(j) == null) {
								continue;
							}

							String key = nn.item(j).getNodeName();
							if ("groupId".equals(key)) {
								item.put("groupId", nn.item(j).getTextContent());
							} else if ("artifactId".equals(key)) {
								item.put("artifactId", nn.item(j).getTextContent());
							} else if ("version".equals(key)) {
								item.put("version", nn.item(j).getTextContent());
							}
						}
						deps.add(item);
					}
				} else {
					ObjectNode item = new ObjectMapper().createObjectNode();
					item.put("groupId", root.getElementsByTagName("groupId").item(0).getTextContent());
					item.put("artifactId", root.getElementsByTagName("artifactId").item(0).getTextContent());
					item.put("version", root.getElementsByTagName("version").item(0).getTextContent());
					deps.add(item);
				}
			}

		}
	}

	private Element getDocument(File file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		return document.getDocumentElement();
	}

	/**
	 * @return file
	 */
	protected File mkdir(File parent) {
		int uuid = (int) ((Math.random() * 9 + 1) * 100000);
		File dir = new File(parent, String.valueOf(uuid));
		dir.mkdirs();
		return dir;
	}

	/**
	 * @param dir dir
	 * @throws Exception exception
	 */
	protected void unzipTo(String src, File dir) throws Exception {

		String cmd = "unzip " + src + " -d " + dir.getAbsolutePath();

		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line = null;
		while ((line = br.readLine()) != null) {
//		    System.out.println(line);  
		}
	}

	/**
	 * @param is is
	 * @return props
	 * @throws Exception exception
	 */
	protected Properties getJarProp(InputStream is) throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		Properties props = new Properties();
		String line = null;

		while ((line = br.readLine()) != null) {
			String[] strs = line.split(":");
			try {
				props.put(strs[0].trim(), strs[1].trim());
			} catch (Exception ex) {
				// ignore here
			}
		}

		br.close();
		return props;
	}

	public static void main(String[] args) throws Exception {
//		ApplicationAnalyzer aa = new ApplicationAnalyzer(new File("kubernetes-client-0.4-jar-with-dependencies.jar"));
		ApplicationAnalyzer aa = new ApplicationAnalyzer(new File("kubeext-http-apiserver-1.8.3.jar"));
		System.out.println(aa.analysis());
//		aa.addMavenDep(null, new File("E:/codes/Prototype/kubeIT/kubeext-http-apiserver-1.8.3/jkubefrk-1.8.3"));

	}
}
