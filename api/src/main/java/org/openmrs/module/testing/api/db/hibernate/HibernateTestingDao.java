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
package org.openmrs.module.testing.api.db.hibernate;

import java.io.InputStream;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.SessionFactory;
import org.openmrs.module.testing.api.db.TestingDao;

/**
 * Hibernate specific implementation for {@link TestingDao}.
 */
public class HibernateTestingDao implements TestingDao {
	
	private SessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public InputStream getTestDataSet() {
		throw new NotImplementedException();
	}
}
