package dataset.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ZipperTest {
    private static final String FILE_DIRECTORY = String.join(File.separator, "src", "test", "files", "dataset", "utils");
    private static final String TRACE_TO_ZIP_FILE_PATH = FILE_DIRECTORY + File.separator + "trace-to-zip.exec"; // zip and compare with CORRECTLY_ZIPPED_TRACE_FILE_PATH
    private static final String NEWLY_ZIPPED_TRACE_FILE_PATH = FILE_DIRECTORY + File.separator + "trace-to-zip.zip"; // expected name of the zipped file, check its existence after zipping.
    
    private static final String EXPECTED_UNZIPPED_TRACE_FILE_PATH = TRACE_TO_ZIP_FILE_PATH; // zip and compare with CORRECTLY_ZIPPED_TRACE_FILE_PATH
    private static final String NEWLY_UNZIPPED_TRACE_FILE_PATH = FILE_DIRECTORY + File.separator + "trace-to-unzip.exec"; // expected name of the zipped file, check its existence after zipping.
    private static final String TRACE_FILE_TO_UNZIP_PATH = FILE_DIRECTORY + File.separator + "trace-to-unzip.zip"; // unzip and compare with TRACE_FILE_PATH
    
    @Test
    void zip_largeTraceFile_zipsCorrectly() throws IOException {
        Zipper.zip(TRACE_TO_ZIP_FILE_PATH);
        // Each zip creates different content (last modified time), so we just check for existence here.
        assertTrue(new File(NEWLY_ZIPPED_TRACE_FILE_PATH).exists());
    }
    
    @Test
    void unzip_largeTraceFile_unzipsCorrectly() throws IOException {
        Zipper.unzip(TRACE_FILE_TO_UNZIP_PATH, FILE_DIRECTORY);
        // TODO: compare with original trace file contents
        contentsAreEqual(EXPECTED_UNZIPPED_TRACE_FILE_PATH, NEWLY_UNZIPPED_TRACE_FILE_PATH);
    }
    
    @AfterEach
    void afterEach() {
        File fileToDelete = new File(NEWLY_ZIPPED_TRACE_FILE_PATH);
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }

        fileToDelete = new File(NEWLY_UNZIPPED_TRACE_FILE_PATH);
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }
    
    private void contentsAreEqual(String expectedFilePath, String actualFilePath) {
        try (FileInputStream fis1 = new FileInputStream(expectedFilePath);
                FileInputStream fis2 = new FileInputStream(actualFilePath);) {
            byte[] file1Data = new byte[fis1.available()];
            fis1.read(file1Data);

            byte[] file2Data = new byte[fis2.available()];
            fis2.read(file2Data);
            assertTrue(Arrays.equals(file1Data, file2Data));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

    }
}
