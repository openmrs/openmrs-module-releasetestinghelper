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
package org.openmrs.module.testing.api.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.testing.api.TestingService;
import org.openmrs.module.testing.api.db.TestingDao;
import org.openmrs.module.testing.util.TestingUtil;
import org.openmrs.util.OpenmrsUtil;

/**
 * Implements {@link TestingService}.
 */
public class TestingServiceImpl implements TestingService {
	
	private TestingDao dao;
	
	/**
	 * @param dao the dao to set
	 */
	public void setDao(TestingDao dao) {
		this.dao = dao;
	}
	
	public void generateTestDataSet(OutputStream out) throws APIException {
		dao.generateTestDataSet(out);
	}
	
	/**
	 * @see org.openmrs.module.testing.TestingService#getPatientWithMostEncounters()
	 */
	public Patient getPatientWithMostEncounters() {
		Integer patientId = dao.getPatientWithMostEncounters();
		if (patientId != null) {
			return Context.getPatientService().getPatient(patientId);
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.module.testing.TestingService#getPatientWithMostObs()
	 */
	public Patient getPatientWithMostObs() {
		Integer patientId = dao.getPatientWithMostEncounters();
		if (patientId != null) {
			return Context.getPatientService().getPatient(patientId);
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.module.testing.TestingService#generateModuleZipFile()
	 */
	public byte[] generateModuleZipFile() throws IOException {
		File moduleRepository = OpenmrsUtil
		        .getDirectoryInApplicationDataDirectory(ModuleConstants.REPOSITORY_FOLDER_PROPERTY_DEFAULT);
		return TestingUtil.zipDirectory(moduleRepository);
	}
}
