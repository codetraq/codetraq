/*
 * LogService.java
 *
 * Custom logging class that also writes to a log file.
 */

package net.mobid.codetraq.utils;

import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author viper
 */
public class LogService extends Logger {

	private static FileHandler _logFile = null;

	private static Formatter _formatter = null;

	private final static String EOF = System.getProperty("line.separator");

	public static Formatter getFormatter() {
		if (_formatter == null) {
			_formatter = new CTExceptionFormatter();
		}
		return _formatter;
	}

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

	protected LogService() {
		super("LogService", null);
		addHandler(getLogFile());
	}

	public static void writeLog(Level level, Throwable t) {
		LogRecord lr = new LogRecord(level, t.getMessage());
		lr.setThrown(t);
		getLogFile().publish(lr);
	}

	public static void writeMessage(String message) {
		LogRecord lr = new LogRecord(Level.INFO, message);
		lr.setThrown(null);
		getLogFile().publish(lr);
	}

	static class CTExceptionFormatter extends Formatter {

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
