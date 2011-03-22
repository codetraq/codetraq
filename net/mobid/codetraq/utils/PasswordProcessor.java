/*
 * PasswordProcessor.java
 *
 * Encrypt and decrypt passwords.
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
 *
 * @author viper
 */
public class PasswordProcessor {

	// NOTE: CHANGE THESE VALUES FOR YOUR OWN SERVER!
	private static final String passPhrase = "Ch4ng3M32s0m3th1ng3Ls34ndm4k31tr34llyh4rdt0r34d";

	private static final byte[] salt =  {
		(byte)0xA9, (byte)0x9B, (byte)0xC8, (byte)0x32,
        (byte)0x56, (byte)0x34, (byte)0xE3, (byte)0x03
	};

	private static final int iterations = 32;

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
