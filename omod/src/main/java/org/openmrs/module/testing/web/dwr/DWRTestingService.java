package org.openmrs.module.testing.web.dwr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contains methods that process DWR calls for the module.
 */
@Controller
public class DWRTestingService {
	
	private static final Log log = LogFactory.getLog(DWRTestingService.class);
	
	/**
	 * Processes user authentication
	 * 
	 * @param username the username of the user
	 * @param password the password of the user
	 * @return true if the user is successfully authenticated otherwise false
	 */
	public boolean authenticateAsSuperUser(@RequestParam(value = "username") String username,
	                                       @RequestParam(value = "password") String password) {
		if (log.isDebugEnabled())
			log.debug("Authenticating user...");
		
		try {
			Context.authenticate(username, password);
			if (Context.isAuthenticated() && Context.getAuthenticatedUser().isSuperUser()) {
				if (log.isDebugEnabled())
					log.debug("Authenticated successfully...");
				return true;
			}
		}
		catch (ContextAuthenticationException e) {
			//do nothing
		}
		
		return false;
	}
	
}
