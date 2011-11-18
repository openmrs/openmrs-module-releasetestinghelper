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

import java.io.OutputStream;

import org.hibernate.exception.SQLGrammarException;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Role;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;

/**
 * Functional tests for {@link TestingService}.
 */
public class TestingServiceTest extends BaseModuleContextSensitiveTest {
	
	private TestingService service;
	
	@Before
	public void before() {
		service = Context.getService(TestingService.class);
	}
	
	@Test
	public void init() {
		
	}
	
	/**
	 * @see {@link TestingService#generateTestDataSet(OutputStream)}
	 */
	@Test(expected = APIAuthenticationException.class)
	@Verifies(value = "should fail if the authenticated user is not a super user", method = "generateTestDataSet(OutputStream)")
	public void generateTestDataSet_shouldFailIfTheAuthenticatedUserIsNotASuperUser() throws Exception {
		Context.getAuthenticatedUser().removeRole(new Role(OpenmrsConstants.SUPERUSER_ROLE));
		service.generateTestDataSet(null);
	}
	
	/**
	 * @see {@link TestingService#generateTestDataSet(OutputStream)}
	 */
	@Test(expected = SQLGrammarException.class)
	@Verifies(value = "should pass if the authenticated user is a super user", method = "generateTestDataSet(OutputStream)")
	public void generateTestDataSet_shouldPassIfTheAuthenticatedUserIsASuperUser() throws Exception {
		Context.authenticate("admin", "test");
		//expect SQLGrammarException.class since only mySql is supported for now
		//but we got past the service layer
		service.generateTestDataSet(null);
	}
}
