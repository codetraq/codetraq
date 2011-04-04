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

import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Custom logging class for CodeTraq. It is also capable to write to a log file.
 *
 * @author Ronald Kurniawan
 * @version 0.1
 */
public class LogService extends Logger {

	private static FileHandler _logFile = null;

	private static Formatter _formatter = null;

	private final static String EOF = System.getProperty("line.separator");

	/**
	 * Returns a static <code>Formatter</code> object to be used for logging purposes.
	 * @return a <code>Formatter</code> object
	 */
	public static Formatter getFormatter() {
		if (_formatter == null) {
			_formatter = new CTExceptionFormatter();
		}
		return _formatter;
	}

	/**
	 * Returns a <code>FileHandler</code> to the log file.
	 * @return a <code>FileHandler</code> object
	 */
	public static FileHandler getLogFile() {
		if (_logFile == null) {
			try {
				_logFile = new FileHandler("codetraq.log", true);
				_logFile.setFormatter(getFormatter());
			} catch (Exception ex) {
				System.out.printf("Failed to initialise log file: %s%n", ex.getMessage());
			}
		}
		return _logFile;
	}

	/**
	 * Creates a new LogService.
	 */
	protected LogService() {
		super("LogService", null);
		addHandler(getLogFile());
	}

	/**
	 * Records an <code>Exception</code> to the log file. The message is formatted
	 * to include the time and the cause of the <code>Exception</code>.
	 * @param level - Exception Level
	 * @param t - the Exception itself
	 */
	public static void writeLog(Level level, Throwable t) {
		LogRecord lr = new LogRecord(level, t.getMessage());
		lr.setThrown(t);
		getLogFile().publish(lr);
	}

	/**
	 * Records a message to the log file. The message is formatted to include the
	 * time.
	 * @param message - text to write into the log file
	 */
	public static void writeMessage(String message) {
		LogRecord lr = new LogRecord(Level.INFO, message);
		lr.setThrown(null);
		getLogFile().publish(lr);
	}

	/**
	 * A <code>Formatter</code> class to format exception messages and ordinary
	 * short messages into log file.
	 */
	static class CTExceptionFormatter extends Formatter {

		/**
		 * Formats a <code>LogRecord</code> object into a <code>String</code>
		 * object ready to be written into the log file.
		 * @param record - <code>LogRecord</code> object
		 * @return a log message
		 */
		@Override
		public String format(LogRecord record) {
			StringBuilder sb = new StringBuilder();
			if (record.getThrown() != null) {
				sb.append("NEW EXCEPTION recorded at: ").append(Utilities.getFormattedTime(record.getMillis()));
				sb.append(EOF);
				sb.append("EXCEPTION sequence: ").append(record.getSequenceNumber());
				sb.append(EOF);
				sb.append("EXCEPTION level: ").append(record.getLevel().toString());
				sb.append(EOF);
				sb.append("EXCEPTION message: ").append(record.getMessage());
				sb.append(EOF);
				for (StackTraceElement ste : record.getThrown().getStackTrace()) {
					sb.append("EXCEPTION Caused by: ").append(ste.getClassName()).append(".")
						.append(ste.getMethodName()).append("():").append(ste.getLineNumber());
					sb.append(EOF);
				}
				sb.append(EOF);
				return sb.toString();
			}
			// just log a message
			sb.append(Utilities.getFormattedTime()).append(" ").append(record.getMessage());
			sb.append(EOF);
			return sb.toString();
		}

	}

}
