package dataset.bug.creator;

import dataset.bug.model.BuggyProject;
import dataset.utils.Counter;
import jmutation.MutationFramework;
import jmutation.MutationFramework.MutationFrameworkBuilder;
import jmutation.model.PrecheckExecutionResult;
import jmutation.model.TestCase;
import jmutation.model.mutation.MutationFrameworkConfig;
import jmutation.model.mutation.MutationFrameworkConfig.MutationFrameworkConfigBuilder;
import jmutation.mutation.MutationCommand;
import jmutation.utils.JSONWrapper;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO: We can delete this class if Runner can handle calling BuggyProjectCreator and the handler works
 */
public class MutationFrameworkDatasetCreator {
    public static final String BUGGY_PROJECT_DIR = "bug";
    public static final String WORKING_PROJECT_DIR = "fix";
    public static final String CREATED_BUGGY_PROJECT_FILE = "createdBugs.json";
    private final MutationFramework mutationFramework;

    public MutationFrameworkDatasetCreator(MutationFramework mutationFramework) {
        this.mutationFramework = mutationFramework;
    }

    public MutationFrameworkDatasetCreator(String projectPath) {
        MutationFrameworkConfigBuilder configBuilder = new MutationFrameworkConfigBuilder();
        configBuilder.setProjectPath(projectPath);
        MutationFrameworkConfig mutationFrameworkConfig = configBuilder.build();
        MutationFrameworkBuilder mutationFrameworkBuilder = new MutationFrameworkBuilder(mutationFrameworkConfig);
        this.mutationFramework = mutationFrameworkBuilder.build();
    }

    public static void main(String[] args) {
        String projectPath = String.join(File.separator, "sample", "math_70");
        String repoPath = "D:\\chenghin\\NUS";
        MutationFrameworkDatasetCreator datasetCreator = new MutationFrameworkDatasetCreator(projectPath);
        datasetCreator.run(repoPath);
    }

    public void run(String repositoryPath) {
        // Multi threading issues.
        // Rename precheck + instrumentation files with random values, and delete after reading.
        // Copy from microbat
        // Create a new thread for each test case + command pair
        String projectPath = mutationFramework.getConfiguration().getProjectPath();
        String projectName = projectPath.substring(projectPath.lastIndexOf(File.separator) + 1);
        String datasetPath = String.join(File.separator, repositoryPath, projectName);
        generateWorkingProject(projectPath, String.join(File.separator, datasetPath, WORKING_PROJECT_DIR));
        int numOfCores = Runtime.getRuntime().availableProcessors() - 1;
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        String createdBugsFilePath = String.join(File.separator, datasetPath, CREATED_BUGGY_PROJECT_FILE);
        JSONObject storedProjects = getStoredProjects(createdBugsFilePath);
        int bugId = storedProjects.length() + 1;
        for (TestCase testCase : testCaseList) {
            mutationFramework.setTestCase(testCase);
            List<MutationCommand> commands;
            try {
                PrecheckExecutionResult precheckExecutionResult = mutationFramework.runPrecheck();
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
                executorService.submit(new BuggyProjectCreator(repositoryPath, projectPath, buggyProject,
                        createdBugsFilePath, bugId));
                bugId++;
            }
        }
        executorService.shutdown();
    }

    public static JSONObject getStoredProjects(String pathToFile) {
        try {
            return JSONWrapper.getJSONObjectFromFile(pathToFile);
        } catch (RuntimeException e) {
            return new JSONObject();
        }
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
