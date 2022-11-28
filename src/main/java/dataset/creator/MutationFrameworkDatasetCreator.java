package dataset.creator;

import dataset.model.BuggyProject;
import jmutation.MutationFramework;
import jmutation.model.TestCase;
import jmutation.model.mutation.MutationFrameworkConfig;
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

public class MutationFrameworkDatasetCreator {
    public static final String BUGGY_PROJECT_DIR = "bug";
    public static final String WORKING_PROJECT_DIR = "fix";
    private static final String CREATED_BUGGY_PROJECT_FILE = "createdBugs.json";
    public static int bugId = 0;
    private final MutationFramework mutationFramework;

    public MutationFrameworkDatasetCreator(MutationFramework mutationFramework) {
        this.mutationFramework = mutationFramework;
    }

    public MutationFrameworkDatasetCreator() {
        MutationFrameworkConfig mutationFrameworkConfig = new MutationFrameworkConfig();
        mutationFrameworkConfig.setMicrobatConfigPath(mutationFrameworkConfig.getMicrobatConfigPath()); // Can be removed aft update
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setConfig(mutationFrameworkConfig);
        this.mutationFramework = mutationFramework;
    }

    public static void main(String[] args) {
        String projectPath = String.join(File.separator, "sample", "math_70");
        String repoPath = "D:\\chenghin\\NUS";
        MutationFrameworkDatasetCreator datasetCreator = new MutationFrameworkDatasetCreator();
        datasetCreator.run(projectPath, repoPath);
    }

    public void run(String projectPath, String repositoryPath) {
        // Multi threading issues.
        // Rename precheck + instrumentation files with random values, and delete after reading.
        // Copy from microbat
        // Create a new thread for each test case + command pair
        String projectName = projectPath.substring(projectPath.lastIndexOf(File.separator) + 1);
        String datasetPath = String.join(File.separator, repositoryPath, projectName);
        generateWorkingProject(projectPath, String.join(File.separator, datasetPath, WORKING_PROJECT_DIR));
        int numOfCores = Runtime.getRuntime().availableProcessors() - 1;
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);
        mutationFramework.getConfiguration().setProjectPath(projectPath);
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        String createdBugsFilePath = String.join(File.separator, datasetPath, CREATED_BUGGY_PROJECT_FILE);
        JSONObject storedProjects = getStoredProjects(createdBugsFilePath);
        bugId = storedProjects.length();
        for (TestCase testCase : testCaseList) {
            mutationFramework.getConfiguration().setTestCase(testCase);
            List<MutationCommand> commands;
            try {
                commands = mutationFramework.analyse();
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
                        createdBugsFilePath));
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

    private static MutationFramework generateMutationFramework(String projectPath) {
        MutationFrameworkConfig mutationFrameworkConfig = new MutationFrameworkConfig();
        mutationFrameworkConfig.setProjectPath(projectPath);
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setConfig(mutationFrameworkConfig);
        return mutationFramework;
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
