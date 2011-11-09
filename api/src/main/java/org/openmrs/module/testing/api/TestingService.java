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

import java.io.IOException;
import java.io.OutputStream;

import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

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
	 * @param out the target
	 * @throws APIException
	 */
	void generateTestDataSet(OutputStream out) throws APIException;
	
	/**
	 * Gets the patient with the biggest number of encounters.
	 * 
	 * @return the patient with the biggest number of encounters.
	 * @should get patient with most encounters.
	 */
	@Transactional(readOnly = true)
	public Patient getPatientWithMostEncounters();
	
	/**
	 * Gets the patient with the biggest number of observations.
	 * 
	 * @return the patient with the biggest number of observations.
	 * @should get patient with most obs.
	 */
	@Transactional(readOnly = true)
	public Patient getPatientWithMostObs();
	
	/**
	 * Creates a zip file with modules.
	 * 
	 * @return an array of bytes for the zip contents.
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_MANAGE_MODULES)
	public byte[] generateModuleZipFile() throws IOException;
}
