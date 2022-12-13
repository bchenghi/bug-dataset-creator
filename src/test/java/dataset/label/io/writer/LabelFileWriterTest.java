package dataset.label.io.writer;

import static dataset.label.io.TestConstants.IO_TEST_FILES_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dataset.label.io.reader.LabelFileReader;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;

class LabelFileWriterTest {
    private final static String PATH_TO_FILES = String.join(File.separator, IO_TEST_FILES_PATH, "writer");
    private final static String PATH_TO_LABEL_FILE = String.join(File.separator, PATH_TO_FILES, "label.json");

    @Test
    void write_tracesProvided_writesCorrectly() {
        assertFalse(new File(PATH_TO_LABEL_FILE).exists());
        // TODO: generate traces for sample project
        Trace workingTrace = RunningInfo.readFromFile(String.join(File.separator, PATH_TO_FILES, "buggy-trace.exec"))
                .getMainTrace();
        Trace buggyTrace = RunningInfo.readFromFile(String.join(File.separator, PATH_TO_FILES, "trace.exec"))
                .getMainTrace();
        String pathToWorkingProject = String.join(File.separator, PATH_TO_FILES, "buggyProject");
        String pathToBuggyProject = String.join(File.separator, PATH_TO_FILES, "workingProject");
        LabelFileWriter writer = new LabelFileWriter(PATH_TO_LABEL_FILE, workingTrace, buggyTrace, pathToWorkingProject,
                pathToBuggyProject, String.join(File.separator, "src", "main", "java"),
                String.join(File.separator, "src", "test", "java"));
        writer.write();
        LabelFileReader reader = new LabelFileReader(PATH_TO_LABEL_FILE);
        Map<String, Double> actualMap = reader.read();
        Map<String, Double> expectedMap = new HashMap<>();
        expectedMap.put("sample/Sample{6,9}i-2-9", 1.0);
        expectedMap.put("sample/Sample{6,9}b-2-10", 1.0);
        expectedMap.put("sample/Sample{6,9}i-2-7", 1.0);
        expectedMap.put("sample/Sample{6,9}b-2-12", 1.0);
        expectedMap.put("sample/Sample{3,3}this-2", -1.0);
        expectedMap.put("sample/SampleTest{7,7}this-0", -1.0);
        expectedMap.put("sample/SampleTest{11,13}sample-1", 1.0);
        expectedMap.put("CR", 1.0);
        expectedMap.put("sample/Sample{5,9}a-2", 1.0);
        expectedMap.put("sample/Sample{6,9}b-2", 1.0);
        expectedMap.put("sample/Sample{6,9}b-2-8", 1.0);
        expectedMap.put("vir_sample.Sample#method(I)I:14", 0.0);
        expectedMap.put("sample/SampleTest{12,13}actual-1", -1.0);
        expectedMap.put("sample/Sample{6,9}i-2", 1.0);
        expectedMap.put("sample/Sample{6,9}i-2-13", 1.0);
        expectedMap.put("sample/Sample{6,9}i-2-11", 1.0);
        assertEquals(expectedMap, actualMap);
        new File(PATH_TO_LABEL_FILE).delete();
    }
}
