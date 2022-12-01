package dataset.creator;

import dataset.TraceCollector;
import dataset.minimize.ProjectMinimizer;
import dataset.model.BuggyProject;
import dataset.model.path.MutationFrameworkPathConfiguration;
import dataset.model.path.PathConfiguration;
import dataset.model.project.DatasetProject;
import dataset.model.project.MutationFrameworkDatasetProject;
import jmutation.MutationFramework;
import jmutation.model.TestCase;
import jmutation.model.mutation.DumpFilePathConfig;
import jmutation.model.mutation.MutationFrameworkConfig;
import jmutation.model.mutation.MutationResult;
import jmutation.mutation.heuristic.HeuristicMutator;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static dataset.creator.MutationFrameworkDatasetCreator.bugId;

public class BuggyProjectCreator implements Runnable {
    private static final Object lock = new Object();
    private static final Object storageFileLock = new Object();
    private final String repositoryPath;
    private final String projectPath;
    private final String storageFilePath;
    private final BuggyProject buggyProject;

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
        MutationFrameworkConfig configuration = new MutationFrameworkConfig();
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setConfig(configuration);
        configuration.setProjectPath(projectPath);
        configuration.setMutator(new HeuristicMutator());

        StringBuilder mutatedProjPath = new StringBuilder(repositoryPath + File.separator +
                buggyProject.projectName() + File.separator);
        int currBugId = increaseAndGetBugId();
        mutatedProjPath.append(currBugId);
        mutatedProjPath.append(File.separator);
        configuration.getDumpFilePathConfig().setMutatedPrecheckFilePath(mutatedProjPath +
                DumpFilePathConfig.DEFAULT_BUGGY_PRECHECK_FILE);
        configuration.getDumpFilePathConfig().setMutatedTraceFilePath(mutatedProjPath +
                DumpFilePathConfig.DEFAULT_BUGGY_TRACE_FILE);
        int mutatedBugPathLen = mutatedProjPath.length();
        mutatedProjPath.append(MutationFrameworkDatasetCreator.BUGGY_PROJECT_DIR);
        configuration.setMutatedProjectPath(mutatedProjPath.toString());
        mutatedProjPath.delete(mutatedBugPathLen, mutatedBugPathLen + 3);
        configuration.setTestCase(buggyProject.testCase());
        try {
        	print(currBugId, "Start mutating");
            MutationResult result = mutationFramework.mutate(buggyProject.command());
        	print(currBugId, "Finish mutating");
            if (result.getMutatedPrecheckExecutionResult() == null || result.getMutatedPrecheckExecutionResult().testCasePassed()) {
                try {
                	print(currBugId, "Test case passed or precheck is null, deleting");
                    FileUtils.deleteDirectory(new File(mutatedProjPath.toString()));
                	print(currBugId, "Test case passed or precheck is null, deleted");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        	print(currBugId, "Test case failed, creating testcase and rootcause files");
            createFile(buggyProject.testCase().toString(), mutatedProjPath.toString(), "testcase.txt");
            createFile(buggyProject.command().toString(), mutatedProjPath.toString(), "rootcause.txt");
        	print(currBugId, "Created testcase and rootcause files. Minimizing.");
            if (!minimize(mutatedProjPath.toString(), currBugId)) {
                try {
                	print(currBugId, "Minimize failed. Deleting.");
                    FileUtils.deleteDirectory(new File(mutatedProjPath.toString()));
                	print(currBugId, "Minimize failed. Deleted.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        	print(currBugId, "Finish minimizing, writing to storage file");
            writeToStorageFile();
        	print(currBugId, "Written to storage file");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    	print(currBugId, "Done");
    }

    private int increaseAndGetBugId() {
        synchronized (lock) {
            System.out.println(Thread.currentThread().getName() + " : " + bugId);
            bugId++;
            return bugId;
        }
    }

    private void writeToStorageFile() {
        synchronized (storageFileLock) {
            System.out.println(Thread.currentThread().getName() + " : writing to json file");
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
    
    private void print(int bugId, String msg) {
    	System.out.println(bugId + ": " + msg);
    }
}
