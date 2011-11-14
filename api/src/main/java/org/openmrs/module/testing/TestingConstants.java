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
package org.openmrs.module.testing;


/**
 * Constants used by the testing module.
 */
public class TestingConstants {

	/** Global property key for IP addresses that can make rest calls to the test module */
	public static final String GP_KEY_ALLOWED_IP_ADDRESSES = "testing.allowedIPAddresses";
	
	/** Global property key for criteria to use to randomize the generated data */
	public static final String GP_KEY_RANDOMIZE_CRITERIA = "testing.randomizeCriteria";
	
	/** Global property key for maximum number of patients to add to the test dataset */
	public static final String GP_KEY_MAX_PATIENT_COUNT = "testing.maxPatientCount";
	
	/** Global property key for maximum number of observations to add to the test dataset */
	public static final String GP_KEY_MAX_OBS_COUNT = "testing.maxObsCount";
}
