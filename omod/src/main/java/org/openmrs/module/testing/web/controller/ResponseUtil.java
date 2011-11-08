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
package org.openmrs.module.testing.web.controller;

import javax.servlet.http.HttpServletResponse;

/**
 * Prepares response headers for writing different data.
 */
public class ResponseUtil {
	
	private ResponseUtil() {
	}
	
	public static void prepareZipResponse(HttpServletResponse response, String filename) {
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".zip");
		response.setHeader("Pragma", "No-cache");
		response.setDateHeader("Expires", 0);
		response.setHeader("Cache-Control", "no-cache");
	}
}
