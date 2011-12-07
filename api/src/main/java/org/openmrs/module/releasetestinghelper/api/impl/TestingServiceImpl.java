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
package org.openmrs.module.releasetestinghelper.api.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.openmrs.Patient;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.releasetestinghelper.api.TestingService;
import org.openmrs.module.releasetestinghelper.api.db.TestingDao;
import org.openmrs.module.releasetestinghelper.util.TestingUtil;

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
	
	/**
	 * @see org.openmrs.module.releasetestinghelper.api.TestingService#generateTestDataSet(OutputStream)
	 */
	public void generateTestDataSet(OutputStream out) throws APIException {
		if (!Context.isAuthenticated() || !Context.getAuthenticatedUser().isSuperUser())
			throw new APIAuthenticationException("Only users with the System Developer role can generate test data");
		dao.generateTestDataSet(out);
		
	}
	
	/**
	 * @see org.openmrs.module.releasetestinghelper.TestingService#getPatientWithMostEncounters()
	 */
	public Patient getPatientWithMostEncounters() {
		Integer patientId = dao.getPatientWithMostEncounters();
		if (patientId != null) {
			return Context.getPatientService().getPatient(patientId);
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.module.releasetestinghelper.TestingService#getPatientWithMostObs()
	 */
	public Patient getPatientWithMostObs() {
		Integer patientId = dao.getPatientWithMostEncounters();
		if (patientId != null) {
			return Context.getPatientService().getPatient(patientId);
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.module.releasetestinghelper.TestingService#generateModuleZipFile()
	 */
	public byte[] generateModuleZipFile() throws APIException {
		try {
			return TestingUtil.zipStartedModules();
		}
		catch (IOException ex) {
			throw new APIException(ex);
		}
	}
}
