package dataset.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ZipperTest {
    private static final String FILE_DIRECTORY = String.join(File.separator, "src", "test", "files", "dataset",
            "utils");
    // expected name of the zipped file, check its existence after zipping.
    private static final String NEWLY_ZIPPED_TRACE_FILE_PATH = FILE_DIRECTORY + File.separator + "trace-to-zip.zip";
    private static final String TRACE_FILE_TO_ZIP_FILE_PATH = FILE_DIRECTORY + File.separator + "trace-to-zip.exec";

    // expected name of the zipped file, check its contents with the expected
    // contents.
    private static final String NEWLY_UNZIPPED_TRACE_FILE_PATH = FILE_DIRECTORY + File.separator
            + "trace-to-unzip.exec";
    private static final String EXPECTED_UNZIPPED_TRACE_FILE_PATH = TRACE_FILE_TO_ZIP_FILE_PATH;
    private static final String TRACE_FILE_TO_UNZIP_PATH = FILE_DIRECTORY + File.separator + "trace-to-unzip.zip";
    

    private static final String DIR_TO_ZIP_PATH = FILE_DIRECTORY + File.separator + "dir-to-zip";
    private static final String NEWLY_ZIPPED_DIR_PATH = DIR_TO_ZIP_PATH + ".zip";
    
    
    private static final String NEWLY_UNZIPPED_DIR_PATH = FILE_DIRECTORY + File.separator + "dir-to-unzip";
    private static final String DIR_TO_UNZIP_PATH = NEWLY_UNZIPPED_DIR_PATH + ".zip";

    @Test
    void zip_largeTraceFile_zipsCorrectly() {
        Zipper.zip(TRACE_FILE_TO_ZIP_FILE_PATH);
        // Each zip creates different content (last modified time), so we just check for
        // existence here.
        assertTrue(new File(NEWLY_ZIPPED_TRACE_FILE_PATH).exists());
    }

    @Test
    void unzip_largeTraceFile_unzipsCorrectly() throws IOException {
        Zipper.unzip(TRACE_FILE_TO_UNZIP_PATH, FILE_DIRECTORY);
        contentsAreEqual(EXPECTED_UNZIPPED_TRACE_FILE_PATH, NEWLY_UNZIPPED_TRACE_FILE_PATH);
    }
    
    @Test
    void zip_directory_zipsCorrectly() {
        Zipper.zip(DIR_TO_ZIP_PATH);
        assertTrue(new File(NEWLY_ZIPPED_DIR_PATH).exists());
    }
    
    @Test
    void unzip_directory_unzipsCorrectly() throws IOException {
        Zipper.unzip(DIR_TO_UNZIP_PATH, NEWLY_UNZIPPED_DIR_PATH);
        dirsAreEqual(DIR_TO_ZIP_PATH, NEWLY_UNZIPPED_DIR_PATH);
    }

    @AfterEach
    void afterEach() throws IOException {
        deleteIfExists(new File(NEWLY_ZIPPED_TRACE_FILE_PATH));
        deleteIfExists(new File(NEWLY_UNZIPPED_TRACE_FILE_PATH));
        deleteIfExists(new File(NEWLY_UNZIPPED_DIR_PATH));
        deleteIfExists(new File(NEWLY_ZIPPED_DIR_PATH));
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
    
    private void dirsAreEqual(String expectedDirPath, String actualDirPath) throws IOException {
        Iterator<File> expectedFiles = FileUtils.iterateFiles(new File(expectedDirPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        Iterator<File> actualFiles = FileUtils.iterateFiles(new File(actualDirPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        while (expectedFiles.hasNext() && actualFiles.hasNext()) {
            File expectedFile = expectedFiles.next();
            File actualFile = actualFiles.next();
            assertTrue(FileUtils.contentEquals(expectedFile, actualFile));
        }
        assertFalse(expectedFiles.hasNext() && actualFiles.hasNext());
    }
    
    private void deleteIfExists(File fileToDelete) throws IOException {
        if (fileToDelete.exists()) {
            if (fileToDelete.isDirectory()) {
                FileUtils.deleteDirectory(fileToDelete);
                return;
            }
            fileToDelete.delete();
        }
    }
}
