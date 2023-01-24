package dataset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class TestUtils {
    public static void dirsAreEqual(String expectedDirPath, String actualDirPath) throws IOException {
        Iterator<File> expectedFiles = FileUtils.iterateFiles(new File(expectedDirPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        Iterator<File> actualFiles = FileUtils.iterateFiles(new File(actualDirPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        Map<String, File> expectedFileNameToContents = getFileNameToContentsMap(expectedFiles);
        Map<String, File> actualFileNameToContents = getFileNameToContentsMap(actualFiles);
        assertEquals(expectedFileNameToContents.keySet(), actualFileNameToContents.keySet());
        for (String fileName : expectedFileNameToContents.keySet()) {
            assertTrue(FileUtils.contentEquals(expectedFileNameToContents.get(fileName),
                    actualFileNameToContents.get(fileName)));
        }
    }

    private static Map<String, File> getFileNameToContentsMap(Iterator<File> it) {
        Map<String, File> result = new HashMap<>();
        while (it.hasNext()) {
            File file = it.next();
            result.put(file.getName(), file);
        }
        return result;
    }

    
    public static void deleteIfExists(File fileToDelete) throws IOException {
        if (fileToDelete.exists()) {
            if (fileToDelete.isDirectory()) {
                FileUtils.deleteDirectory(fileToDelete);
                return;
            }
            fileToDelete.delete();
        }
    }

    public static boolean isRunningInGitHubActions() {
        return System.getenv("GITHUB_ACTIONS") != null;
    }
}
