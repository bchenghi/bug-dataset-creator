package dataset.bug;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Log {
	public static final String DEFAULT_LOG_FILE_PATTERN = "%t/dataset-creator%g.log";
	private static FileHandler fileHandler = null;
	public static Logger createLogger(Class<?> someClass) {
		Logger logger = Logger.getLogger(someClass.getName());
		try {
			if (fileHandler == null) {
				fileHandler = new FileHandler(DEFAULT_LOG_FILE_PATTERN, true);
			}
			logger.addHandler(fileHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logger;
	}
}
