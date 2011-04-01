/*
 * Utilities.java
 *
 * This class hosts methods which can be useful in multiple places.
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
 *
 * @author viper
 */
public class Utilities {

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

	public static String getFormattedTime() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd 'at' hh:mm aaa");
		return formatter.print(Calendar.getInstance().getTimeInMillis());
	}

	public static String getFormattedTime(long millis) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd 'at' hh:mm aaa");
		return formatter.print(millis);
	}

	public static boolean checkValue(String name, String value) {
		if (value.length() == 0) {
			System.out.printf("%s is empty%n", name);
			return false;
		}
		return true;
	}

	public static boolean checkNode(String name, Node node) {
		if (node == null) {
			System.out.printf("Cannot find attribute %s%n", name);
			return false;
		}
		return true;
	}

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

	public static boolean checkServerShortName(List<ServerDTO> servers, String sName) {
		for (ServerDTO s : servers) {
			if (s.getShortName().equalsIgnoreCase(sName)) {
				return false;
			}
		}
		return true;
	}

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
