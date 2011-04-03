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

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.mobid.codetraq.persistence.ServerDTO;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utilities.java
 *
 * This class hosts methods which can be useful in multiple places.
 * @author Ronald Kurniawan
 */
public class Utilities {

	/*
	 * Writes the content of a DOM Document into a configuration file.
	 * @param configFile - a <code>File</code> instance that points to the configuration file
	 * @param document - a DOM Document object
	 */
	private static void writeConfigToFile(File configFile, Document document) {
		try {
			Source source = new DOMSource(document);
			Result result = new StreamResult(configFile);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(source, result);
		} catch (TransformerConfigurationException ex) {
			Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
		} catch (TransformerException te) {
			Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, te);
		}
	}

	/**
	 * Creates a new empty configuration file.
	 * @param filename - configuration file name
	 */
	public static void createNewConfigFile(String filename) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement("codetraq");
			doc.appendChild(root);
			Element traq = doc.createElement("traq");
			traq.setAttribute("type", "null");
			traq.setAttribute("notificationid", "null");
			traq.setAttribute("password", "null");
			root.appendChild(traq);
			File file = new File(filename);
			writeConfigToFile(file, doc);
		} catch (ParserConfigurationException ex) {
			Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Returns a formatted time of the current time. The format is "yyyy-MM-dd at hh:mm AM/PM".
	 * @return a <code>String</code> that represents the current time
	 */
	public static String getFormattedTime() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd 'at' hh:mm aaa");
		return formatter.print(Calendar.getInstance().getTimeInMillis());
	}

	/**
	 * Returns a formatted time of the time specified by the timestamp. The format is
	 * "yyyy-MM-dd at hh:mm AM/PM".
	 * @param millis - timestamp that specified a certain time
	 * @return a <code>String</code> that represents the current time
	 */
	public static String getFormattedTime(long millis) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd 'at' hh:mm aaa");
		return formatter.print(millis);
	}

	/**
	 * Checks whether a <code>String</code> has a content or not. If not, it
	 * prints a message to the console.
	 * @param name - Text identifier of the checked <code>String</code>
	 * @param value - The <code>String</code> itself
	 * @return <code>true</code> if <code>String</code> has content, <code>false</code>
	 * otherwise
	 */
	public static boolean checkValue(String name, String value) {
		if (value.length() == 0) {
			System.out.printf("%s is empty%n", name);
			return false;
		}
		return true;
	}

	/**
	 * Checks whether a DOM Node has content or not. If not, it prints a message to
	 * the console.
	 * @param name - Text identifier of the checked <code>Node</code>
	 * @param node - The <code>Node</code> itself
	 * @return <code>true</code> if <code>Node</code> has content, <code>false</code>
	 * otherwise
	 */
	public static boolean checkNode(String name, Node node) {
		if (node == null) {
			System.out.printf("Cannot find attribute %s%n", name);
			return false;
		}
		return true;
	}

	/**
	 * Creates a directory to hold all GIT repositories for the application.
	 * @return <code>true</code> if directory is successfully created, <code>false</code>
	 * otherwise
	 */
	public static boolean createGitReposDir() {
		File gitReposDir = new File("gitrepos");
		if (!gitReposDir.exists()) {
			try {
				return gitReposDir.mkdir();
			} catch(SecurityException ex) {
				LogService.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
				LogService.writeLog(Level.SEVERE, ex);
			}
		}
		return false;
	}

	/**
	 * Creates a directory to hold a local GIT repository.
	 * @param name - The name of the local directory
	 */
	public static void createGitProjectDir(String name) {
		boolean gitReposStatus = createGitReposDir();
		File gitReposDir = new File("gitrepos");
		if (gitReposStatus || (!gitReposStatus && gitReposDir.exists())) {
			if (gitReposDir.isDirectory() && gitReposDir.canWrite()) {
				File gitProjectDir = new File("gitrepos/" + name);
				try {
					gitProjectDir.mkdir();
				} catch(SecurityException ex) {
					LogService.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
					LogService.writeLog(Level.SEVERE, ex);
				}
			}
		}
	}

	/**
	 * Checks whether a server's short name is unique throughout all other servers.
	 * @param servers - a <code>List</code> of <code>ServerDTO</code> objects
	 * @param sName - the server's short name to be checked
	 * @return <code>true</code> if short name is unique, <code>false</code> otherwise
	 */
	public static boolean checkServerShortName(List<ServerDTO> servers, String sName) {
		for (ServerDTO s : servers) {
			if (s.getShortName().equalsIgnoreCase(sName)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether a <code>String</code> is comprised of hexadecimal characters.
	 * @param value - <code>String</code> value to be checked
	 * @return <code>true</code> if all characters are hexadecimal characters,
	 * <code>false</code> otherwise
	 */
	public static boolean isHexString(String value) {
		boolean isHex = true;
		for (char c : value.toCharArray()) {
			if (!(c >= '0' && c <= '9') && !(c >= 'a' && c <= 'f') &&
				!(c >= 'A' && c <= 'F')) {
				isHex = false;
				break;
			}
		}
		return isHex;
	}

}
