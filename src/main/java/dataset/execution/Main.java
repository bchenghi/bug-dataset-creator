package dataset.execution;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dataset.bug.model.BuggyProject;
import dataset.bug.model.path.MutationFrameworkPathConfiguration;
import dataset.execution.handler.BugCheckHandler;
import jmutation.MutationFramework;
import jmutation.MutationFramework.MutationFrameworkBuilder;
import jmutation.model.PrecheckExecutionResult;
import jmutation.model.TestCase;
import jmutation.model.mutation.MutationFrameworkConfig;
import jmutation.model.mutation.MutationFrameworkConfig.MutationFrameworkConfigBuilder;
import jmutation.mutation.MutationCommand;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import static dataset.bug.creator.BuggyProjectCreator.getStoredProjects;
import static dataset.constants.FileNames.WORKING_PROJECT_DIR;


public class Main {
    private static final int INSTRUMENTATION_TIMEOUT = 5;
    public static void main(String[] args) throws InterruptedException {
        final String projectName = "commons-pool";
        final String repoPath = "D:\\chenghin\\NUS";
        String originalProjectPath = "D:\\chenghin\\commons-pool";
        runBugDataCollection(repoPath, projectName, originalProjectPath);
    }

    private static void checkDone(String repoPath, String projectName, List<Integer> bugIds) {
        int count = 0;
        for (int bugId : bugIds) {
            if (new File(repoPath + File.separator + projectName + File.separator + bugId).exists()) {
                BugCheckHandler handler = new BugCheckHandler(repoPath, projectName, bugId);
                if (!handler.individualHandler(new Request(true))) {
                    System.out.println(count);
                    count++;
                }
            }
        }
        System.out.println(count);
    }

    private static void runBugDataCollection(String repoPath, String projectName, String originalProjectPath) {
        MutationFrameworkPathConfiguration pathConfiguration = new MutationFrameworkPathConfiguration(repoPath);
        int numOfCores = 2;
        MutationFrameworkConfigBuilder configBuilder = new MutationFrameworkConfigBuilder();
        configBuilder.setProjectPath(originalProjectPath);
        MutationFrameworkConfig mutationFrameworkConfig = configBuilder.build();
        MutationFrameworkBuilder mutationFrameworkBuilder = new MutationFrameworkBuilder(mutationFrameworkConfig);
        MutationFramework mutationFramework = mutationFrameworkBuilder.build();
        String datasetPath = String.join(File.separator, repoPath, projectName);
        generateWorkingProject(originalProjectPath, String.join(File.separator, datasetPath, WORKING_PROJECT_DIR));
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        JSONObject storedProjects = getStoredProjects(pathConfiguration.getStoragePath(projectName));
        int bugId = 466;
        for (TestCase testCase : testCaseList) {
            mutationFramework.setTestCase(testCase);
            List<MutationCommand> commands;
            try {
                PrecheckExecutionResult precheckExecutionResult = mutationFramework.runPrecheck();
                if (!precheckExecutionResult.testCasePassed()) continue;
                commands = mutationFramework.analyse(precheckExecutionResult.getCoverage());
            } catch (RuntimeException e) {
                e.printStackTrace();
                continue;
            }
            for (int i = 0; i < commands.size(); i++) {
                MutationCommand command = commands.get(i);
                BuggyProject buggyProject = new BuggyProject(testCase, command, projectName);
                if (checkBuggyProjectAlreadyCloned(storedProjects, buggyProject)) {
                    continue;
                }
                executorService.submit(new DataSetCreationRunner(repoPath, projectName, bugId, INSTRUMENTATION_TIMEOUT, buggyProject,
                        pathConfiguration.getBugPath(projectName, String.valueOf(bugId)), originalProjectPath));
                bugId++;
            }
        }
        executorService.shutdown();
    }

    private static boolean checkBuggyProjectAlreadyCloned(JSONObject createdProjects, BuggyProject project) {
        try {
            createdProjects.get(project.key());
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private static void generateWorkingProject(String projectPath, String fixPath) {
        File workingDir = new File(fixPath);
        try {
            FileUtils.deleteDirectory(workingDir);
            FileUtils.copyDirectory(new File(projectPath), workingDir);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to clone project to " + workingDir.getAbsolutePath());
        }
    }
}