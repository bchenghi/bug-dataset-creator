package dataset.creator;

import dataset.TraceCollector;
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

import static jmutation.dataset.DatasetCreator.bugId;

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
            System.out.println(e);
        }
    }

    public void run() {
        MutationFrameworkConfig configuration = new MutationFrameworkConfig();
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setConfig(configuration);
        configuration.setProjectPath(projectPath);
        configuration.setMutator(new HeuristicMutator());
        StringBuilder mutatedProjPath = new StringBuilder(repositoryPath + File.separator + buggyProject.projectName() + File.separator);
        int currBugId = increaseAndGetBugId();
        mutatedProjPath.append(currBugId);
        mutatedProjPath.append(File.separator);
        int mutatedBugPathLen = mutatedProjPath.length();
        mutatedProjPath.append(MutationFrameworkDatasetCreator.BUGGY_PROJECT_DIR);
        configuration.setMutatedProjectPath(mutatedProjPath.toString());
        mutatedProjPath.delete(mutatedBugPathLen, mutatedBugPathLen + 3);
        configuration.setTestCase(buggyProject.testCase());
        try {
            MutationResult result = mutationFramework.mutate(buggyProject.command());
            if (result.getMutatedPrecheckExecutionResult().testCasePassed()) {
                try {
                    FileUtils.deleteDirectory(new File(mutatedProjPath.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            createFile(buggyProject.testCase().toString(), mutatedProjPath.toString(), "testcase.txt");
            createFile(buggyProject.command().toString(), mutatedProjPath.toString(), "rootcause.txt");
            runTraceCollection();
            writeToStorageFile();
        } catch (RuntimeException e) {
            System.out.println(e);
        }
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

    private void runTraceCollection() {
        // Get the path to buggy
        // Get names of the trace files
        PathConfiguration pathConfiguration = new MutationFrameworkPathConfiguration(repositoryPath);
        String buggyPath = pathConfiguration.getBuggyPath(buggyProject.projectName(), Integer.toString(bugId));
        String workingPath = pathConfiguration.getFixPath(buggyProject.projectName(), Integer.toString(bugId));
        String bugPath = pathConfiguration.getBugPath(buggyProject.projectName(), Integer.toString(bugId));
        // Get test case file
        DatasetProject project = new MutationFrameworkDatasetProject(buggyPath);
        TestCase testCase = project.getFailingTests().get(0);
//        return new TraceCollector(workingPath, testCase, bugPath + File.separator + "precheckWorking.exec",
//                bugPath + File.separator + "traceWorking.exec",
//                bugPath + File.separator + DumpFilePathConfig);
    }
}
