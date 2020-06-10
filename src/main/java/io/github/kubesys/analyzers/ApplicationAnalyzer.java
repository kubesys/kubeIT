/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.analyzers;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.kubesys.Analyzer;

/**
 * @author  wuheng09@gmail.com
 * 
 * {
	"language": "Java",
	"name": "kubeext-http-apiserver-1.8.3.jar",
	"type": "SpringBoot 2.2.6.RELEASE",
	"buildJdk": "1.8",
	"dependsOn": {
		"artifactId_value": {
			"groupId": "groupId_value",
			"artifactId": "artifactId_value",
			"version": "version_value"
		}
	}
   } 
 **/

public abstract class ApplicationAnalyzer extends Analyzer {

	
	public static String KEY_APP_LANGUAGE            = "language";
	
	public static String KEY_APP_NAME                = "name";
	
	public static String KEY_GROUPID                 = "groupId";
	
	public static String KEY_ARTIFACTID              = "artifactId";
	
	public static String KEY_VERSION                 = "version";
	
	/**
	 * app info
	 */
	protected ObjectNode app = new ObjectMapper().createObjectNode();
	
	/**
	 * dependency info
	 */
	protected ObjectNode dependsOn = new ObjectMapper().createObjectNode();
	
	public ApplicationAnalyzer(File file) {
		super(file);
	}

}
