package dataset.bug;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Log {
	public static final String DEFAULT_LOG_FILE_PATTERN = "%t/dataset-creator%g.log";
	public static Logger createLogger(Class<?> someClass) {
		Logger logger = Logger.getLogger(someClass.getName());
		try {
			logger.addHandler(new FileHandler(DEFAULT_LOG_FILE_PATTERN, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logger;
	}
}
