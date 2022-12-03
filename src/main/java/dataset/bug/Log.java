package dataset.bug;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Log {
	public static final String DEFAULT_LOG_FILE_PATTERN = "%t/dataset-creator%g.log";
	private static FileHandler fileHandler = null;
	private static int LOG_FILE_SIZE = 1 * (int) Math.pow(10, 9); // 1GB
	private static int NUM_LOG_FILES = 4;
	public static Logger createLogger(Class<?> someClass) {
		Logger logger = Logger.getLogger(someClass.getName());
		try {
			if (fileHandler == null) {
				fileHandler = new FileHandler(DEFAULT_LOG_FILE_PATTERN, LOG_FILE_SIZE, NUM_LOG_FILES);
			}
			logger.addHandler(fileHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logger;
	}
}
