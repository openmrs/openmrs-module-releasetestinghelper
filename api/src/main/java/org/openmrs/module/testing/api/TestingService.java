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
package org.openmrs.module.testing.api;

import java.io.InputStream;

/**
 * Contains methods exposing the core functionality.
 * <p>
 * Usage example:<br>
 * <code>
 * Context.getService(TestingService.class).getTestDataSet();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
public interface TestingService {
	
	/**
	 * Generates an SQL dump with test data and metadata.
	 * 
	 * @return the sql dump
	 * @should return test data set
	 */
	InputStream getTestDataSet();
}
