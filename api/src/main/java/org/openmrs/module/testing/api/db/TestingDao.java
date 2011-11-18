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
package org.openmrs.module.testing.api.db;

import java.io.OutputStream;

import org.openmrs.api.db.DAOException;
import org.openmrs.module.testing.api.TestingService;

/**
 * Database methods for {@link TestingService}.
 */
public interface TestingDao {
	
	/**
	 * @see TestingService#generateTestDataSet(OutputStream)
	 */
	void generateTestDataSet(OutputStream out) throws DAOException;
	
	/**
     * @see TestingService#getPatientWithMostEncounters()
     */
	public Integer getPatientWithMostEncounters();
	
	/**
     * @see TestingService#getPatientWithMostObs()
     */
	public Integer getPatientWithMostObs();
}
