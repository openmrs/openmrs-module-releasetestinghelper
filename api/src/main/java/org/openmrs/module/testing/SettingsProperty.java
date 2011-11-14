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

import org.apache.commons.lang.StringUtils;
import org.openmrs.GlobalProperty;

/**
 * Wrapper for {@link GlobalProperty} used in {@link SettingsForm}.
 */
public class SettingsProperty {

	private GlobalProperty globalProperty;
	
	public SettingsProperty(GlobalProperty globalProperty) {
		this.globalProperty = globalProperty;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		String name = globalProperty.getProperty();
		int sectionEnd = globalProperty.getProperty().indexOf(".");
		if (sectionEnd > 0) {
			name = globalProperty.getProperty().substring(sectionEnd + 1);
		}
		
		name = beautify(name);
		
		return name;
	}
	
	/**
	 * @return the globalProperty
	 */
	public GlobalProperty getGlobalProperty() {
		return globalProperty;
	}
	
	/**
	 * @param globalProperty the globalProperty to set
	 */
	public void setGlobalProperty(GlobalProperty globalProperty) {
		this.globalProperty = globalProperty;
	}
	
	/**
	 * Beautifies string
	 * 
	 * @param section
	 * @return
	 */
	private String beautify(String section) {
		section = section.replace("_", " ");
		section = section.replace(".", " ");
		
		String[] sections = StringUtils.splitByCharacterTypeCamelCase(section);
		section = StringUtils.join(sections, " ");
		
		sections = StringUtils.split(section);
		for (int i = 0; i < sections.length; i++) {
			sections[i] = StringUtils.capitalize(sections[i]);
		}
		section = StringUtils.join(sections, " ");
		
		return section;
	}
}
