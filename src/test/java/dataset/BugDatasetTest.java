package dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import dataset.BugDataset.BugData;
import tregression.empiricalstudy.TestCase;

class BugDatasetTest {
    private static final String FILE_ROOT = String.join(File.separator, "src", "test", "files", "dataset");
    private static final String REPO_PATH = String.join(File.separator, FILE_ROOT, "sample-bug-repo");
    private static final String PROJECT_NAME = "math_70";
    private static final String UNZIPPED_BUG_1 = String.join(File.separator, REPO_PATH, PROJECT_NAME, "1");
    private static final String ZIPPED_BUG_1 = UNZIPPED_BUG_1 + ".zip";
    private static final String UNZIPPED_BUG_2 = String.join(File.separator, REPO_PATH, PROJECT_NAME, "2");
    private static final String ZIPPED_BUG_2 = UNZIPPED_BUG_2 + ".zip";
    private static final String EXPECTED_BUG_2 = String.join(File.separator, REPO_PATH, PROJECT_NAME, "expected-2");
    
    @Test
    void zip_bugDirProvided_zipsCorrectly() throws IOException {
        BugDataset bugDataset = new BugDataset(REPO_PATH, PROJECT_NAME);
        bugDataset.zip(1);
        assertTrue(new File(ZIPPED_BUG_1).exists());
        assertFalse(new File(UNZIPPED_BUG_1).exists());
    }
    
    @Test
    void unzip_zippedBugDirProvided_unzipsCorrectly() throws IOException {
        BugDataset bugDataset = new BugDataset(REPO_PATH, PROJECT_NAME);
        bugDataset.unzip(2);
        TestUtils.dirsAreEqual(EXPECTED_BUG_2, UNZIPPED_BUG_2);
        assertFalse(new File(ZIPPED_BUG_2).exists());
    }
    
    @Test
    void getData_bugDirProvided_getsCorrectData() throws IOException {
        BugDataset bugDataset = new BugDataset(REPO_PATH, PROJECT_NAME);
        BugData data = bugDataset.getData(1);
        assertTrue(data.getBuggyTrace().size() > 0);
        assertTrue(data.getWorkingTrace().size() > 0);
        assertEquals(3, data.getRootCauseNode());
        assertEquals(new TestCase("org.apache.commons.math.analysis.BinaryFunctionTest", "testAdd").getName(), data.getTestCase().getName());
    }
    
    @AfterEach
    void afterEach() throws IOException {
        TestUtils.deleteIfExists(new File(UNZIPPED_BUG_2));
        TestUtils.deleteIfExists(new File(ZIPPED_BUG_1));
        restoreOriginalProjects();
    }

    void restoreOriginalProjects() {
        // clone original into sample-repo
        final String originalPath = String.join(File.separator, FILE_ROOT, "sample-bug-repo-original");
        try {
            FileUtils.deleteDirectory(new File(REPO_PATH));
            FileUtils.copyDirectory(new File(originalPath), new File(REPO_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
