package dataset.label;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import dataset.bug.model.path.MutationFrameworkPathConfiguration;
import jmutation.utils.JSONWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

class LabelRunnerTest {
    private static final String TEST_FILES_PATH = String.join(File.separator, "src", "test", "files", "dataset", "label");
    private static final String PROJECT_NAME = "sample-project";
    private static final String REPO_PATH = String.join(File.separator, TEST_FILES_PATH, "sample-repo");
    private static final String ACTUAL_LABEL_FILE_PATH = (new MutationFrameworkPathConfiguration(REPO_PATH)).getLabelPath(PROJECT_NAME, Integer.toString(1));

    @AfterEach
    public void afterEach() {
        File file = new File(ACTUAL_LABEL_FILE_PATH);
        file.delete();
    }

    @Test
    void run_correctState_writesToSpecifiedLocation() {
        LabelRunner runner = new LabelRunner(REPO_PATH, PROJECT_NAME, 1);
        runner.run();
        JSONObject actualJSON = JSONWrapper.getJSONObjectFromFile(ACTUAL_LABEL_FILE_PATH);
        JSONObject expectedJSON = new JSONObject();
        expectedJSON.put("vir_sample.Sample#method()I:6", -1);
        expectedJSON.put("sample/SampleTest{11,12}sample-1", -1);
        expectedJSON.put("sample/Sample{3,3}this-2", -1);
        expectedJSON.put("sample/SampleTest{7,7}this-0", -1);
        assertEquals(expectedJSON.toMap(), actualJSON.toMap());
    }
}
