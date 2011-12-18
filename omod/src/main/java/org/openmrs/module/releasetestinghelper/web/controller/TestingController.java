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
package org.openmrs.module.releasetestinghelper.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.releasetestinghelper.SettingsForm;
import org.openmrs.module.releasetestinghelper.SettingsProperty;
import org.openmrs.module.releasetestinghelper.TestingConstants;
import org.openmrs.module.releasetestinghelper.api.TestingService;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The main controller.
 */
@Controller
public class TestingController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/releasetestinghelper/settings", method = RequestMethod.GET)
	public void showSettings() {
		
	}
	
	/**
	 * Generates a test data set which is packaged in a zip file.
	 * 
	 * @param response
	 * @throws APIException
	 * @throws IOException
	 */
	@RequestMapping(value = "/module/releasetestinghelper/generateTestDataSet", method = RequestMethod.POST)
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
	@RequestMapping(method = RequestMethod.POST, value = "/module/releasetestinghelper/getModules")
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
	private boolean authenticateAsSuperUser(String username, String password, HttpServletResponse response)
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
			sendErrorResponseWithDelay(response, HttpServletResponse.SC_UNAUTHORIZED,
			    "Unable to authenticate as a User with the System Developer role. Invalid username or password");
		}
		
		return false;
	}
	
	@ModelAttribute("settingsForm")
	public SettingsForm getSettingsForm() {
		SettingsForm settingsForm = new SettingsForm();
		
		List<SettingsProperty> settings = new ArrayList<SettingsProperty>();
		AdministrationService service = Context.getAdministrationService();
		
		addSetting(TestingConstants.GP_KEY_MAX_PATIENT_COUNT, settings, service);
		
		settingsForm.setSettings(settings);
		
		return settingsForm;
	}
	
	public void addSetting(String name, List<SettingsProperty> settings, AdministrationService service) {
		GlobalProperty globalProperty = service.getGlobalPropertyObject(name);
		if (globalProperty != null) {
			settings.add(new SettingsProperty(globalProperty));
		}
	}
	
	@RequestMapping(value = "/module/releasetestinghelper/settings", method = RequestMethod.POST)
	public void updateSettings(@ModelAttribute("settingsForm") SettingsForm settingsForm, Errors errors, HttpSession session) {
		
		if (errors.hasErrors()) {
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "releasetestinghelper.settings.not.saved");
		} else {
			AdministrationService service = Context.getAdministrationService();
			for (SettingsProperty property : settingsForm.getSettings()) {
				service.saveGlobalProperty(property.getGlobalProperty());
			}
			session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "releasetestinghelper.settings.saved");
		}
	}
	
	/**
	 * Processes requests to verify the username and password
	 * 
	 * @param username
	 * @param password
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/module/releasetestinghelper/verifycredentials", method = RequestMethod.POST)
	public void verifyCredentials(@RequestParam("username") String username, @RequestParam("password") String password,
	                              HttpServletResponse response) throws IOException {
		
		User user = null;
		try {
			Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
			user = Context.getUserService().getUserByUsername(new String(Base64.decode(username), Charset.forName("UTF-8")));
		}
		finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		}
		
		if (user != null) {
			String lockoutTimeString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP);
			Long lockoutTime = null;
			if (StringUtils.isNotBlank(lockoutTimeString) && !lockoutTimeString.equals("0"))
				lockoutTime = Long.valueOf(lockoutTimeString);
			
			// if they've been locked out,  wait 30min before processing their next request
			if (lockoutTime != null && System.currentTimeMillis() - lockoutTime < 1800000) {
				sendErrorResponseWithDelay(response, HttpServletResponse.SC_FORBIDDEN,
				    "You have been looked out by the system");
				return;
			}
			
			authenticateAsSuperUser(username, password, response);
		} else {
			//We don't know this username
			sendErrorResponseWithDelay(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
		}
	}
	
	/**
	 * Sends a response with the specified http code, message after a 5sec delay
	 * 
	 * @param response
	 * @param httpCode
	 * @param message
	 * @throws IOException
	 */
	private void sendErrorResponseWithDelay(HttpServletResponse response, int httpCode, String message) throws IOException {
		//delay the response by 5 seconds in case someone brute forces testing of usernames
		try {
			Thread.sleep(5000);
		}
		catch (InterruptedException e) {}
		
		response.sendError(httpCode, message);
	}
}
