package dataset.label.io.reader;

import org.junit.jupiter.api.Test;

import static dataset.label.io.TestConstants.IO_TEST_FILES_PATH;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class LabelFileReaderTest {

    @Test
    void read_validLabelFile_readsCorrectly() {
    	String filePath = String.join(File.separator, IO_TEST_FILES_PATH, "reader", "label.json");
    	LabelFileReader reader  = new LabelFileReader(filePath);
    	Map<String, Double> actualLabels = reader.read();
    	Map<String, Double> expectedLabels = new HashMap<>();
    	expectedLabels.put("org/apache/commons/math/analysis/BinaryFunction{30,30}this-3", -1.0);
    	expectedLabels.put("org/apache/commons/math/analysis/BinaryFunction$4{60,60}this-2", 0.5);
    	assertEquals(expectedLabels, actualLabels);
    }
}