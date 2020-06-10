/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author  wuheng09@gmail.com
 * 
 * 
 **/
public abstract class Analyzer {
	
	/**
	 * file
	 */
	protected final File file;

	/**
	 * @param file       file
	 */
	public Analyzer(File file) {
		super();
		this.file = file;
	}
	
	
	/**
	 * @return             json
	 * @throws Exception   exception
	 */
	public abstract JsonNode analysis() throws Exception;
	
}
