package org.openmrs.module.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The Controller for processing requests for installed modules
 */
@Controller
public class GetModulesController {
	
	private static final Log log = LogFactory.getLog(GetModulesController.class);
	
	/**
	 * Processes the requests for installed modules and returns a zip file of all modules
	 * 
	 * @param response
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/module/testing/zipModules")
	public void getModules(HttpServletResponse response) {
		if (log.isDebugEnabled())
			log.debug("Getting installed modules...");
		
		//TODO Get the zipFile from the service layer
		//See https://tickets.openmrs.org/browse/TRUNK-2828
		File file = null;
		//File file = Context.getService(TestingService.class).generateModuleZipFile();
		
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
		try {
			FileCopyUtils.copy(new FileInputStream(file), response.getOutputStream());
			response.flushBuffer();
			return;
		}
		catch (IOException e) {
			//there is nothing to download
			response.setContentType("text/html");
			response.setHeader("Content-Disposition", "");
			throw new APIException("Failed to write the zip file to the response outputStream:" + e.getMessage());
		}
	}
}
