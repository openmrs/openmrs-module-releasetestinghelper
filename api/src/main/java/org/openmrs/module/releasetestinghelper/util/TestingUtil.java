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
package org.openmrs.module.releasetestinghelper.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;

/**
 * Utility methods for the testing module.
 */
public class TestingUtil {
	
	/**
	 * Zips all started modules.
	 * 
	 * @return an array of bytes for the zip contents.
	 */
	public static byte[] zipStartedModules() throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		FileInputStream fis = null;
	
		try{
			Collection<Module> startedModules = ModuleFactory.getStartedModules();
			for (Module module : startedModules) {
	
				File file = module.getFile();
					
				// Stream to read file
				fis = new FileInputStream(file);
				
				// Make a ZipEntry
				ZipEntry entry = new ZipEntry(file.getName());
				zos.putNextEntry(entry);
				
				IOUtils.copy(fis, zos);
				
				fis.close();
			}
		}
		finally{
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(zos);
		}
		
		return baos.toByteArray();
	}
}