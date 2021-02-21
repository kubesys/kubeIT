/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.doslab.poseidon.analyzers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

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
	
	/**
	 * @param is                           is
	 * @return                             props
	 * @throws Exception                   exception
	 */
	protected Properties getProps(InputStream is, String split) throws Exception {

		BufferedReader br = new BufferedReader(
				new InputStreamReader(is));

		Properties props = new Properties();
		
		String line = null;

		while ((line = br.readLine()) != null) {
			String[] strs = line.split(split);
			try {
				props.put(strs[0].replaceAll("\"", "").trim(), 
							strs[1].replaceAll("\"", "").trim());
			} catch (Exception ex) {
				// ignore here
			}
		}

		br.close();
		return props;
	}
	
}
