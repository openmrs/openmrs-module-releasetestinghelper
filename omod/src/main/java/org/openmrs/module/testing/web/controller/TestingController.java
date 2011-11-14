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
package org.openmrs.module.testing.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.testing.api.TestingService;
import org.openmrs.web.WebConstants;
import org.openmrs.module.testing.SettingsForm;
import org.openmrs.module.testing.SettingsProperty;
import org.openmrs.module.testing.TestingConstants;
import org.openmrs.api.AdministrationService;
import org.openmrs.GlobalProperty;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * The main controller.
 */
@Controller
public class TestingController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/testing/settings", method = RequestMethod.GET)
	public void showSettings() {
		
	}
	
	/**
	 * Generates a test data set which is packaged in a zip file.
	 * 
	 * @param response
	 * @throws APIException
	 * @throws IOException
	 */
	@RequestMapping(value = "/module/testing/generateTestDataSet", method = RequestMethod.POST)
	public void generateTestDataSet(HttpServletResponse response, @RequestParam(value = "username") String username,
	                                @RequestParam(value = "password") String password) throws APIException, IOException {
		
		if (authenticateAsSuperUser(username, password, response)) {
			OutputStream out = null;
			try {
				ResponseUtil.prepareZipResponse(response, "testDataSet");
				out = response.getOutputStream();
				TestingService service = Context.getService(TestingService.class);
				service.generateTestDataSet(out);
				out.close();
			}
			finally {
				IOUtils.closeQuietly(out);
			}
		}
	}
	
	/**
	 * Processes the requests for installed modules and returns a zip file of all modules
	 * 
	 * @param response
	 * @throws APIException
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/module/testing/getModules")
	public void getModules(HttpServletResponse response, @RequestParam(value = "username") String username,
	                       @RequestParam(value = "password") String password) throws APIException, IOException {
		
		if (log.isDebugEnabled())
			log.debug("Getting started modules...");
		
		if (authenticateAsSuperUser(username, password, response)) {
			byte[] moduleZip = Context.getService(TestingService.class).generateModuleZipFile();
			ResponseUtil.prepareZipResponse(response, "modules");
			FileCopyUtils.copy(moduleZip, response.getOutputStream());
			response.flushBuffer();
			return;
		}
	}
	
	/**
	 * Processes authentication of a user as super user, if the authentication fails, it sends the
	 * response back to the client along with the an unauthorized error code, so you can't write to
	 * the response's outPutStream if the authentication fails.
	 * 
	 * @param username the Base64 encoded username
	 * @param password the Base64 encoded password
	 * @param response the httpResponse object to write to in case authentication fails.
	 * @return true if authentication was successful otherwise false
	 * @throws IOException
	 */
	public boolean authenticateAsSuperUser(String username, String password, HttpServletResponse response)
	    throws IOException {
		if (log.isDebugEnabled())
			log.debug("Authenticating user...");
		
		try {
			String decodedUsername = new String(Base64.decode(username), Charset.forName("UTF-8"));
			String decodedPassword = new String(Base64.decode(password), Charset.forName("UTF-8"));
			Context.authenticate(decodedUsername, decodedPassword);
			if (Context.isAuthenticated() && Context.getAuthenticatedUser().isSuperUser()) {
				if (log.isDebugEnabled())
					log.debug("Authenticated successfully...");
				return true;
			}
		}
		catch (ContextAuthenticationException e) {
			//do nothing
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
			    "Unable to authenticate as a User with the System Developer role. Invalid username or password");
		}
		
		return false;
	}
	
	@ModelAttribute("settingsForm")
	public SettingsForm getSettingsForm() {
		SettingsForm settingsForm = new SettingsForm();
		
		List<SettingsProperty> settings = new ArrayList<SettingsProperty>();
		AdministrationService service = Context.getAdministrationService();
		
		addSetting(TestingConstants.GP_KEY_ALLOWED_IP_ADDRESSES, settings, service);
		addSetting(TestingConstants.GP_KEY_RANDOMIZE_CRITERIA, settings, service);
		addSetting(TestingConstants.GP_KEY_MAX_PATIENT_COUNT, settings, service);
		addSetting(TestingConstants.GP_KEY_MAX_OBS_COUNT, settings, service);
		
		settingsForm.setSettings(settings);

		return settingsForm;
	}
	
	public void addSetting(String name, List<SettingsProperty> settings, AdministrationService service) {
		GlobalProperty globalProperty = service.getGlobalPropertyObject(name);
		if (globalProperty != null) {
			settings.add(new SettingsProperty(globalProperty));
		}
	}
	
	@RequestMapping(value = "/module/testing/settings", method = RequestMethod.POST)
	public void updateSettings(@ModelAttribute("settingsForm") SettingsForm settingsForm, Errors errors,
	        HttpSession session) {
				
		if (errors.hasErrors()) {
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "testing.settings.not.saved");
		} else {
			AdministrationService service = Context.getAdministrationService();
			for (SettingsProperty property : settingsForm.getSettings()) {
				service.saveGlobalProperty(property.getGlobalProperty());
			}
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "testing.settings.saved");
		}
	}
}
