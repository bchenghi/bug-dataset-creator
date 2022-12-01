package dataset.bug.creator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import dataset.bug.model.path.MutationFrameworkPathConfiguration;
import dataset.bug.model.path.PathConfiguration;
import dataset.bug.model.path.PathConfiguration.InstrumentatorFile;
import dataset.trace.TraceCreator;

public class TraceCreatorTest {
	public static final String TEST_FILES_PATH = String.join(File.separator, "src", "test", "files", "dataset", "creator");
	private static final String PROJECT_NAME = "sample-project";
	private static final String REPO_NAME = "sample-repo";
	private static final String REPO_PATH = String.join(File.separator, TEST_FILES_PATH, REPO_NAME);
	private static final List<String> TRACE_FILE_PATHS = new ArrayList<>();
	
	static {
		PathConfiguration pathConfig = new MutationFrameworkPathConfiguration(REPO_PATH);
    	for (InstrumentatorFile fileType : InstrumentatorFile.values()) {
    		if (fileType == InstrumentatorFile.BUGGY_PRECHECK || fileType == InstrumentatorFile.PRECHECK || 
    				fileType == InstrumentatorFile.BUGGY_TRACE_W_ASSERTS || fileType == InstrumentatorFile.TRACE_W_ASSERTS) continue;
    		TRACE_FILE_PATHS.add(pathConfig.getInstrumentatorFilePath(PROJECT_NAME, Integer.toString(1), fileType));
    	}
	}
	
	@AfterEach
	public void afterEach() {
    	for (String filePath : TRACE_FILE_PATHS) {
    		File file = new File(filePath);
    		file.delete();
    	}
	}
	
	@Test
	public void run_correctState_createsTraces() throws IOException {
		TraceCreator creator = new TraceCreator(new File(REPO_PATH).getCanonicalPath(), PROJECT_NAME, 1);
		creator.run();
    	for (String filePath : TRACE_FILE_PATHS) {
    		File file = new File(filePath);
    		assertTrue(file.exists());
    	}
	}
	
	@Test
	public void isDone_tracesArePresent_returnsTrue() throws IOException {
		TraceCreator creator = new TraceCreator(new File(REPO_PATH).getCanonicalPath(), PROJECT_NAME, 2);
		assertTrue(creator.isDone());
	}
	
	@Test
	public void isDone_tracesNotPresent_returnsFalse() throws IOException {
		TraceCreator creator = new TraceCreator(new File(REPO_PATH).getCanonicalPath(), PROJECT_NAME, 1);
		assertFalse(creator.isDone());
	}
}
