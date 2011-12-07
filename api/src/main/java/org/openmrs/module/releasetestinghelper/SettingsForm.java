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
package org.openmrs.module.releasetestinghelper;

import java.util.List;

/**
 * Form used by {@link TestingController} for the settings page.
 */
public class SettingsForm {
	
	private List<SettingsProperty> settings;
	
	/**
	 * @return the settings
	 */
	public List<SettingsProperty> getSettings() {
		return settings;
	}
	
	/**
	 * @param settings the settings to set
	 */
	public void setSettings(List<SettingsProperty> settings) {
		this.settings = settings;
	}
}
