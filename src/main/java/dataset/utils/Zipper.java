package dataset.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;

import dataset.bug.Log;

public class Zipper {
    private final static Logger logger = Log.createLogger(Zipper.class);
    public static boolean zip(String path) {
        File file = new File(path);
        if (!file.exists()) {
            logger.warning(String.format("File at %s not found", path));
            return false;
        }
        
        try (FileOutputStream fos = new FileOutputStream(file.getParent() + File.separator + FilenameUtils.removeExtension(file.getName()) + ".zip"); 
                ZipOutputStream zipOut = new ZipOutputStream(fos); 
                FileInputStream fis = new FileInputStream(file)) {
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }

        return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean unzip(String path, String newPath) {
        File destDir = new File(newPath);

        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(path));) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
    
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        // write file content
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
