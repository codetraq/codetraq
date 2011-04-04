/*
 * Copyright 2011 Ronald Kurniawan.
 *
 * This file is part of CodeTraq.
 *
 * CodeTraq is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CodeTraq is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CodeTraq. If not, see <http://www.gnu.org/licenses/>.
 */
package net.mobid.codetraq.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.spec.KeySpec;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEParameterSpec;
import org.apache.commons.codec.binary.Base64;

/**
 * Encrypt and decrypt passwords. These functions are used heavily in connection
 * with configuration file processing. <b>NOTE:</b> You should definitely change
 * the <code>passPhrase</code> value and then recompile this project before using
 * it in your own server. This would make it difficult for any unauthorised person
 * to guess the passwords stored in your configuration files.
 *
 * @author Ronald Kurniawan
 * @version 0.1
 */
public class PasswordProcessor {

	// NOTE: CHANGE THESE VALUES FOR YOUR OWN SERVER!
	private static final String passPhrase = "Ch4ng3M32s0m3th1ng3Ls34ndm4k31tr34llyh4rdt0r34d";

	private static final byte[] salt =  {
		(byte)0xA9, (byte)0x9B, (byte)0xC8, (byte)0x32,
        (byte)0x56, (byte)0x34, (byte)0xE3, (byte)0x03
	};

	private static final int iterations = 32;

	/**
	 * Encrypts a text using the <code>passPhrase</code> above and an algorithm supported
	 * by your virtual machine implementation. You can change the default algorithm with
	 * another algorithm, but please make sure your virtual machine supports it.
	 * @param valueToEncrypt - text to encrypt
	 * @return an encrypted, Base64 encoded text
	 */
	public static String encryptString(String valueToEncrypt) {
		String output = null;
		try {
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterations);
			SecretKey secretKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
			Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterations);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
			// begin encrypting...
			byte[] byteToEncrypt = valueToEncrypt.getBytes("UTF8");
			byte[] encrypted = cipher.doFinal(byteToEncrypt);
			output = new Base64().encodeToString(encrypted);
		} catch (Exception ex) {
			Logger.getLogger(PasswordProcessor.class.getName()).log(Level.SEVERE, null, ex);
		}
		return output;
	}

	/**
	 * Decrypts a text using the <code>passPhrase</code> above and an algorithm supported
	 * by your virtual machine implementation. You can change the default algorithm with
	 * another algorithm, but please make sure your virtual machine supports it.
	 * @param valueToDecrypt - text to decrypt
	 * @return a plain text
	 */
	public static String decryptString(String valueToDecrypt) {
		String output = null;
		try {
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterations);
			SecretKey secretKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
			Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterations);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
			// begin decrypting...
			byte[] encrypted = new Base64().decode(valueToDecrypt);
			byte[] utf8 = cipher.doFinal(encrypted);
			output = new String(utf8, "UTF8");
		} catch(Exception ex) {
			Logger.getLogger(PasswordProcessor.class.getName()).log(Level.SEVERE, null, ex);
		}
		return output;
	}

}
