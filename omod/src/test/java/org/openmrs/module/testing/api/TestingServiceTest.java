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

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Functional tests for {@link TestingService}.
 */
public class TestingServiceTest extends BaseModuleContextSensitiveTest {
	
	private TestingService service;
	
	@Before
	public void before() {
		service = Context.getService(TestingService.class);
	}
	
	/**
	 * @see TestingService#getTestDataSet()
	 * @verifies return test data set
	 */
	@Test
	public void getTestDataSet_shouldReturnTestDataSet() throws Exception {
		InputStream testDataSet = service.getTestDataSet();
		
		assertNotNull(testDataSet);
	}
}
