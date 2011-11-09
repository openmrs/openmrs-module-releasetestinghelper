/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.testing.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility methods for the testing module.
 */
public class TestingUtil {

	/**
	 * Zips a directory.
	 * 
	 * @param directory the directory to zip.
	 * @return an array of bytes for the zip contents.
	 */
	public static byte[] zipDirectory(File directory) throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		
		// Create a buffer for copying
		byte[] buffer = new byte[8192];
		
		File[] files = directory.listFiles();
		int bytesRead = 0;
		for (int i = 0; i < files.length; i++) {
			
			File file = files[i];
			
			//Ignore directories
			if (file.isDirectory()) {
				continue;
			} 
			
			//Ignore non module files.
			if (!file.getAbsolutePath().endsWith(".omod"))
				continue;
				
			// Stream to read file
			FileInputStream in = new FileInputStream(file);
			
			// Make a ZipEntry
			ZipEntry entry = new ZipEntry(file.getPath().substring(directory.getPath().length() + 1));
			zos.putNextEntry(entry);
			
			while (-1 != (bytesRead = in.read(buffer))) {
				zos.write(buffer, 0, bytesRead);
			}
			
			in.close();
		}
		
		zos.close();
		
		return baos.toByteArray();
	}
}