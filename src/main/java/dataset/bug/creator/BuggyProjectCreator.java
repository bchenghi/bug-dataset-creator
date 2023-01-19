package dataset.bug.creator;

import dataset.bug.Log;
import dataset.bug.minimize.ProjectMinimizer;
import dataset.bug.model.BuggyProject;
import dataset.bug.model.path.MutationFrameworkPathConfiguration;
import dataset.bug.model.path.PathConfiguration;
import jmutation.MutationFramework;
import jmutation.model.mutation.DumpFilePathConfig;
import jmutation.model.mutation.MutationFrameworkConfig;
import jmutation.model.mutation.MutationResult;
import jmutation.model.mutation.MutationFrameworkConfig.MutationFrameworkConfigBuilder;
import jmutation.mutation.heuristic.HeuristicMutator;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dataset.bug.creator.MutationFrameworkDatasetCreator.bugId;

public class BuggyProjectCreator implements Runnable {
    private static final Logger logger = Log.createLogger(BuggyProjectCreator.class);
    private static final Object lock = new Object();
    private static final Object storageFileLock = new Object();
    private final String repositoryPath;
    private final String projectPath;
    private final String storageFilePath;
    private final BuggyProject buggyProject;
    public static final String ROOTCAUSE_FILE_NAME = "rootcause.txt";
    public static final String TESTCASE_FILE_NAME = "testcase.txt";

    public BuggyProjectCreator(String repositoryPath, String projectPath, BuggyProject buggyProject, String storageFilePath) {
        this.repositoryPath = repositoryPath;
        this.projectPath = projectPath;
        this.buggyProject = buggyProject;
        this.storageFilePath = storageFilePath;
    }

    private static void createFile(String contents, String pathToFile, String fileName) {
        File file = new File(pathToFile);
        file.mkdirs();
        try (FileWriter writer = new FileWriter(new File(file, fileName))) {
            writer.write(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        StringBuilder mutatedProjPath = new StringBuilder(repositoryPath + File.separator +
                buggyProject.projectName() + File.separator);
        int currBugId = increaseAndGetBugId();
        mutatedProjPath.append(currBugId);
        mutatedProjPath.append(File.separator);
        
        MutationFrameworkConfigBuilder configBuilder = new MutationFrameworkConfigBuilder();        
        configBuilder.setProjectPath(projectPath);
        configBuilder.setMutator(new HeuristicMutator());
        DumpFilePathConfig dumpFilePathConfig = new DumpFilePathConfig();
        dumpFilePathConfig.setMutatedPrecheckFilePath(mutatedProjPath +
                DumpFilePathConfig.DEFAULT_BUGGY_PRECHECK_FILE);
        dumpFilePathConfig.setMutatedTraceFilePath(mutatedProjPath +
                DumpFilePathConfig.DEFAULT_BUGGY_TRACE_FILE);
        int mutatedBugPathLen = mutatedProjPath.length();
        mutatedProjPath.append(MutationFrameworkDatasetCreator.BUGGY_PROJECT_DIR);
        configBuilder.setMutatedProjectPath(mutatedProjPath.toString());
        mutatedProjPath.delete(mutatedBugPathLen, mutatedBugPathLen + 3);
        configBuilder.setTestCase(buggyProject.testCase());
        MutationFramework mutationFramework = new MutationFramework(configBuilder.build());

        try {
            log(currBugId, "Start mutating");
            MutationResult result = mutationFramework.mutate(buggyProject.command());
            log(currBugId, "Finish mutating");
            if (result.getMutatedPrecheckExecutionResult() == null || result.getMutatedPrecheckExecutionResult().testCasePassed()) {
                try {
                    log(currBugId, "Test case passed or precheck is null, deleting", Level.WARNING);
                    FileUtils.deleteDirectory(new File(mutatedProjPath.toString()));
                    log(currBugId, "Test case passed or precheck is null, deleted", Level.WARNING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            log(currBugId, "Test case failed, creating testcase and rootcause files");
            createFile(buggyProject.testCase().toString(), mutatedProjPath.toString(), TESTCASE_FILE_NAME);
            createFile(buggyProject.command().toString(), mutatedProjPath.toString(), ROOTCAUSE_FILE_NAME);
            log(currBugId, "Created testcase and rootcause files. Minimizing.");
            if (!minimize(mutatedProjPath.toString(), currBugId)) {
                try {
                    log(currBugId, "Minimize failed. Deleting.", Level.WARNING);
                    FileUtils.deleteDirectory(new File(mutatedProjPath.toString()));
                    log(currBugId, "Minimize failed. Deleted.", Level.WARNING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            log(currBugId, "Finish minimizing, writing to storage file");
            writeToStorageFile();
            log(currBugId, "Written to storage file");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        log(currBugId, "Done");
    }

    private int increaseAndGetBugId() {
        synchronized (lock) {
            logger.log(Level.INFO, Thread.currentThread().getName() + " : " + bugId);
            bugId++;
            return bugId;
        }
    }

    private void writeToStorageFile() {
        synchronized (storageFileLock) {
            logger.log(Level.INFO, Thread.currentThread().getName() + " : writing to json file");
            JSONObject storedJSON = MutationFrameworkDatasetCreator.getStoredProjects(storageFilePath);
            storedJSON.put(buggyProject.key(), buggyProject.createJSONObject());
            try (FileWriter writer = new FileWriter(storageFilePath)) {
                writer.write(storedJSON.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean minimize(String buggyProjectPath, int bugId) {
        ProjectMinimizer minimizer = createMinimizer(buggyProjectPath, bugId);
        return minimizer.minimize();
    }

    private ProjectMinimizer createMinimizer(String buggyProjectPath, int bugId) {
        PathConfiguration pathConfiguration = new MutationFrameworkPathConfiguration(repositoryPath);
        String projectName = projectPath.substring(projectPath.lastIndexOf(File.separator) + 1);
        String metadataPath = pathConfiguration.getRelativeMetadataPath(projectName, Integer.toString(bugId));
        buggyProjectPath.substring(repositoryPath.length());
        return new ProjectMinimizer(repositoryPath, String.join(File.separator,
                buggyProject.projectName(), Integer.toString(bugId), MutationFrameworkDatasetCreator.BUGGY_PROJECT_DIR),
                String.join(File.separator, buggyProject.projectName(), MutationFrameworkDatasetCreator.WORKING_PROJECT_DIR),
                metadataPath);
    }

    private void log(int bugId, String msg) {
        logger.info(bugId + ": " + msg);
    }    

    private void log(int bugId, String msg, Level level) {
        logger.log(level, bugId + ": " + msg);
    }
}
