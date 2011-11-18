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
package org.openmrs.module.testing.util;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.openmrs.api.APIException;

/**
 * Utility class that for encrypting/decrypting text
 */
public class Security {
	
	private static final Log log = LogFactory.getLog(Security.class);
	
	private static final String ALGORITHM = "PBEWithMD5AndDES";
	
	private static final String TRANSFORMATION = ALGORITHM + "/CBC/PKCS5Padding";
	
	/**
	 * Encrypts the specified string and returns a Base64 encoded string
	 * 
	 * @param textToEncrypt the text to encrypt
	 * @param salt the Base64 encoded string to use as salt when encrypting the string
	 * @param encryptionkey the secret token to use when encrypting the string
	 * @return the encrypted string
	 * @throws APIException
	 */
	public static String encryptString(String textToEncrypt, String saltString, String encryptionKey) throws APIException {
		if (log.isDebugEnabled())
			log.debug("Encrypting string....");
		try {
			PBEKeySpec keySpec = new PBEKeySpec(encryptionKey.toCharArray());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
			SecretKey secretKey = keyFactory.generateSecret(keySpec);
			PBEParameterSpec parameterSpec = new PBEParameterSpec(Base64.decode(saltString), 20);
			
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
			
			String cipherText = Base64.encode(cipher.doFinal(textToEncrypt.getBytes(Charset.forName("UTF-8"))));
			if (log.isDebugEnabled())
				log.debug("Done encrypting string....");
			
			return cipherText;
			
		}
		catch (GeneralSecurityException e) {
			throw new APIException("Failed to encrypt string:", e);
		}
	}
}
