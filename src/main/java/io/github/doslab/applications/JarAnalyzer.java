/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.doslab.applications;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.doslab.analyzers.AppAnalyzer;

/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
/**
 * @author henry
 *
 */
public class JarAnalyzer extends AppAnalyzer {

	public static String VALUE_APP_LANGUAGE               = "Java";
	
	public static String FILE_JAR_METADATA                = "META-INF/MANIFEST.MF";
	
	public static String KEY_SPRINGBOOT_VERSION           = "Spring-Boot-Version";
	
	public static String KEY_APPTYPE                      = "type";
	
	public static String KEY_DEPENDS                      = "dependsOn";
	
	public static String KEY_BUILDJDK                     = "buildJdk";
	
	public static String VALUE_SPRINGBOOT_BUILDJDK        = "Build-Jdk-Spec";
	
	public static String VALUE_MAVEN_APPTYPE              = "Created-By";
	
	public static String VALUE_MAVEN_BUILDJDK             = "Build-Jdk";
	
	public static String VALUE_SPRINGBOOT_DEPDEND         = "Spring-Boot-Lib";
	
	public static String VALUE_MAVEN_DEPDEND              = "META-INF";
	
	public static String POM_XML                          = "pom.xml";
	
	public static String JAR_FILE                          = "jar";
	
	public static String POM_PARENT                       = "parent";
	
	public JarAnalyzer(File file) {
		super(file);
	}

	@Override
	public JsonNode analysis() throws Exception {


		app.put(KEY_APP_LANGUAGE, VALUE_APP_LANGUAGE);
		app.put(KEY_APP_NAME, file.getName());
		
		File dir = unzipTo(file);
		
		// https://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
		Properties props = getProps(new FileInputStream(
				new File(dir.getAbsolutePath(), FILE_JAR_METADATA)), ":");
		
		app.put(KEY_APPTYPE,  getAppType(props));
		app.put(KEY_BUILDJDK, getBuildJDK(props));
		dependsOnAnalysis(dependsOn, new File(
					dir, getTargetFile(props)));
		app.set(KEY_DEPENDS, dependsOn);

		deleteDir(dir);

		return app;
	}

	
	/******************************************************
	 * 
	 * 
	 *                  Props
	 *   Now we just support Maven and SpringBoot
	 * 
	 ******************************************************/
	
	/**
	 * @param props                        props
	 * @return                             appType
	 */
	protected String getAppType(Properties props) {
		if (props.get(KEY_SPRINGBOOT_VERSION) != null) {
			return props.get(KEY_SPRINGBOOT_VERSION).toString();
		} else {
			return props.get(VALUE_MAVEN_APPTYPE).toString();
		}
	}
	
	/**
	 * @param props                        props
	 * @return                             buildjdk
	 */
	protected String getBuildJDK(Properties props) {
		if (props.get(KEY_SPRINGBOOT_VERSION) != null) {
			return props.get(VALUE_SPRINGBOOT_BUILDJDK).toString();
		} else if (props.get(VALUE_MAVEN_BUILDJDK) != null) {
			return props.get(VALUE_MAVEN_BUILDJDK).toString();
		} else {
			return "unknown";
		}
	}
	
	/**
	 * @param props                        props
	 * @return                             buildjdk
	 */
	protected String getTargetFile(Properties props) {
		if (props.get(KEY_SPRINGBOOT_VERSION) != null) {
			return props.get(VALUE_SPRINGBOOT_DEPDEND).toString();
		} else {
			return VALUE_MAVEN_DEPDEND;
		}
	}
	
	/******************************************************
	 * 
	 *                  File related
	 * 
	 ******************************************************/

	/**
	 * @param parent                         parent
	 * @return                               file
	 */
	protected File mkdir(File parent) {
		File dir = new File(parent, String.valueOf(
				(Math.random() * 9 + 1) * 100000));
		dir.mkdirs();
		return dir;
	}

	/**
	 * @param file                           file
	 * @return                               dir
	 * @throws Exception                     exception
	 */
	protected File unzipTo(File file) throws Exception {
		
		File dir = mkdir(file.getParentFile());
		
		String cmd = "unzip -o " + file.getAbsolutePath() + " -d " + dir.getAbsolutePath();

		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line = null;
		while ((line = br.readLine()) != null) {
		}
		
		br.close();
		return dir;
	}
	
	/**
	 * @param dir                              dir
	 * @return                                 file
	 */
	protected boolean deleteDir(File dir) {
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
	 * @param tempdir                     dir
	 * @return                            groupId
	 */
	protected String getGroupId(File tempdir) {
		String groupId = "";
		for (File tf : tempdir.listFiles()) {
			if (tf.isDirectory() && 
					!tf.getName().endsWith("INF")) {
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
	@SuppressWarnings("deprecation")
	protected void dependsOnAnalysis(ObjectNode deps, File dir) throws Exception {

		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				dependsOnAnalysis(deps, f);
			} else if (f.getAbsolutePath().endsWith(JAR_FILE)) {
				try {
					
					JarAnalyzer analyzer = new JarAnalyzer(f.getAbsoluteFile());
					/** when calling 'analyzer.analysis().get(KEY_DEPENDS)',
					 *  the result is:
					 *  
					 * "annotations": {
					 *		"groupId": "org",
					 *		"artifactId": "annotations",
					 *		"version": "15.0"
					 *	},
					 *
					 * while calling 'analyzer.analysis().get(KEY_DEPENDS).iterator().next()'
					 * the result is
					 * 
					 * {
					 *		"groupId": "org",
					 *		"artifactId": "annotations",
					 *		"version": "15.0"
					 *	},
					 */
					JsonNode json = analyzer.analysis().get(KEY_DEPENDS).iterator().next();
					
					ObjectNode item = new ObjectMapper().createObjectNode();
					item.put(KEY_GROUPID, json.get(KEY_GROUPID).asText());
					item.put(KEY_ARTIFACTID, json.get(KEY_ARTIFACTID).asText());
					item.put(KEY_VERSION, json.get(KEY_VERSION).asText());
					
					deps.put(json.get(KEY_ARTIFACTID).asText(), item);
				} catch (Exception e) {
					
					/**
					 * 
					 * some jars do not contain pom.xml.
					 * 
					 * Now the jar name is like annotations-1.5.0,
					 * and we assume 
					 * 
					 * 1. the artifactId is annotations
					 * 2. the version id is 1.5.0
					 * 3. the groupid is the package name
					 * 
					 * This simple rule can work well on most jars
					 */
					
					System.err.println("not maven, may be bunlde: " + f.getName());
					
					int idx = f.getName().lastIndexOf("-");
					int edx = f.getName().lastIndexOf(".");
					String artifactId = f.getName().substring(0, idx);
					String version = f.getName().substring(idx + 1, edx);

					File tempdir = unzipTo(f);
					String groupId = getGroupId(tempdir.getAbsoluteFile());
					groupId = (groupId.length() == 0) ? artifactId : groupId;
					groupId = groupId.endsWith(".") ? groupId.substring(0, groupId.length() - 1) : groupId;

					ObjectNode item = new ObjectMapper().createObjectNode();
					item.put(KEY_GROUPID, groupId);
					item.put(KEY_ARTIFACTID, artifactId);
					item.put(KEY_VERSION, version);
					deleteDir(tempdir);
					deps.put(artifactId, item);
				}
			} else if (f.getAbsolutePath().endsWith(POM_XML)) {
				
				// Two cases should be considered
				Element root = getDocument(f);
				NodeList list = root.getElementsByTagName(POM_PARENT);

				if (list.getLength() != 0) {
					/**
					 * <project>
					 *     <parent>
					 * 		<groupID></groupId>
					 *      <artifactId></artifactId>
					 *      <version></version>
					 *     </parent>
					 * </project>
					 */
					
					Element parent = (Element) list.item(0);
					ObjectNode item = new ObjectMapper().createObjectNode();
					item.put(KEY_GROUPID, parent.getElementsByTagName(KEY_GROUPID).item(0).getTextContent());
					item.put(KEY_ARTIFACTID, parent.getElementsByTagName(KEY_ARTIFACTID).item(0).getTextContent());
					item.put(KEY_VERSION, parent.getElementsByTagName(KEY_VERSION).item(0).getTextContent());
					deps.put(parent.getElementsByTagName(KEY_ARTIFACTID).item(0).getTextContent(), item);
				} else {
					
					/**
					 * <project>
					 * 		<groupID></groupId>
					 *      <artifactId></artifactId>
					 *      <version></version>
					 * </project>
					 */
					ObjectNode item = new ObjectMapper().createObjectNode();
					item.put(KEY_GROUPID, root.getElementsByTagName(KEY_GROUPID).item(0).getTextContent());
					item.put(KEY_ARTIFACTID, root.getElementsByTagName(KEY_ARTIFACTID).item(0).getTextContent());
					item.put(KEY_VERSION, root.getElementsByTagName(KEY_VERSION).item(0).getTextContent());
					deps.put(root.getElementsByTagName(KEY_ARTIFACTID).item(0).getTextContent(), item);
				}
			}

		}
	}

	/**
	 * @param file                          file
	 * @return                              element
	 * @throws Exception                    exception
	 */
	private Element getDocument(File file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		return document.getDocumentElement();
	}



	public static void main(String[] args) throws Exception {
//		ApplicationAnalyzer aa = new ApplicationAnalyzer(new File("kubernetes-client-0.4-jar-with-dependencies.jar"));
		JarAnalyzer aa = new JarAnalyzer(new File("kubeext-http-apiserver-1.8.3.jar"));
		System.out.println(aa.analysis());
//		aa.addMavenDep(null, new File("E:/codes/Prototype/kubeIT/kubeext-http-apiserver-1.8.3/jkubefrk-1.8.3"));

	}
}
