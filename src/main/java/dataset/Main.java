package dataset;

import dataset.minimize.ProjectMinimizer;
import dataset.model.path.MutationFrameworkPathConfiguration;
import dataset.model.path.PathConfiguration;
import dataset.model.project.DatasetProject;
import dataset.model.project.MutationFrameworkDatasetProject;
import jmutation.model.TestCase;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        String repoPath = String.join(File.separator ,"C:", "Users", "bchenghi", "Desktop");
        String projectName = "minimize";
        int bugId = 1;
        minimize(repoPath, projectName, bugId);
        maximise(repoPath, projectName, bugId);

//        String repoPath = "C:\\Users\\bchenghi\\Desktop";
//        String projectName = "math_70";
//        int numOfCores = Runtime.getRuntime().availableProcessors() - 1;
//        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);
//        for (int bugId = 2; bugId <= 812; bugId++) {
//            executorService.submit(updateExisting1(repoPath, projectName, bugId));
//            minimize(repoPath, projectName, bugId);
//            maximise(repoPath, projectName, bugId);
//        }
//        executorService.shutdown();
        // Create buggy version + trace collection + minimize
    }

    private static TraceCollector updateExisting(String repoPath, String projectName, int bugId) {
        // Create for loop to go through each project in dir, and create trace file in corresponding directory using Trace Collector
        // TODO: Refactor dataset creator to use path configuration, and try it out
        // Then work on git diff stuff to minimize size
        PathConfiguration pathConfiguration = new MutationFrameworkPathConfiguration(repoPath);
        String buggyPath = pathConfiguration.getBuggyPath(projectName, Integer.toString(bugId));
        String bugPath = pathConfiguration.getBugPath(projectName, Integer.toString(bugId));
        // Get test case file
        DatasetProject project = new MutationFrameworkDatasetProject(buggyPath);
        TestCase testCase = project.getFailingTests().get(0);
        return new TraceCollector(buggyPath, testCase, bugPath + File.separator + "precheck",
                bugPath + File.separator + "trace.exec",
                bugPath + File.separator + "traceWAsserts.exec");
    }

    private static TraceCollector updateExisting1(String repoPath, String projectName, int bugId) {
        // Create for loop to go through each project in dir, and create trace file in corresponding directory using Trace Collector
        // TODO: Refactor dataset creator to use path configuration, and try it out
        // Then work on git diff stuff to minimize size
        PathConfiguration pathConfiguration = new MutationFrameworkPathConfiguration(repoPath);
        String buggyPath = pathConfiguration.getBuggyPath(projectName, Integer.toString(bugId));
        String workingPath = pathConfiguration.getFixPath(projectName, Integer.toString(bugId));
        String bugPath = pathConfiguration.getBugPath(projectName, Integer.toString(bugId));
        // Get test case file
        DatasetProject project = new MutationFrameworkDatasetProject(buggyPath);
        TestCase testCase = project.getFailingTests().get(0);
        return new TraceCollector(workingPath, testCase, bugPath + File.separator + "precheckWorking.exec",
                bugPath + File.separator + "traceWorking.exec",
                bugPath + File.separator + "traceWorkingWAssserts.exec");
    }

    private static void createAndMinimize() {
        // Create a single fix.
        // Inside a loop, create a buggy.
        // Apply project minimizer on buggy.
        // Add debug point.
        // Apply project revert on buggy.
        // Add debug point.
    }

    private static void minimize(String repoPath, String projectName, int bugId) {
        // Copy paste fix and buggy to some location, and specify their paths here
        // Apply project minimizer on buggy.
        // Add debug point.
        // Apply project revert on buggy.
        // Add debug point.

        // Given repo path, project name and bug id, it must minimise a project
        PathConfiguration pathConfiguration = new MutationFrameworkPathConfiguration(repoPath);
        String buggyPath = pathConfiguration.getRelativeBuggyPath(projectName, Integer.toString(bugId));
        String workingPath = pathConfiguration.getRelativeFixPath(projectName, Integer.toString(bugId));
        String metadataPath = pathConfiguration.getRelativeMetadataPath(projectName, Integer.toString(bugId));
        ProjectMinimizer minimizer = new ProjectMinimizer(repoPath, buggyPath, workingPath,
                metadataPath);
        minimizer.minimize();
    }

    private static void maximise(String repoPath, String projectName, int bugId) {
        // Given repo path, project name and bug id, it must maximise a project
        PathConfiguration pathConfiguration = new MutationFrameworkPathConfiguration(repoPath);
        // Create path to bug
        String buggyPath = pathConfiguration.getRelativeBuggyPath(projectName, Integer.toString(bugId));
        String workingPath = pathConfiguration.getRelativeFixPath(projectName, Integer.toString(bugId));
        String metadataPath = pathConfiguration.getRelativeMetadataPath(projectName, Integer.toString(bugId));
        ProjectMinimizer minimizer = new ProjectMinimizer(repoPath, buggyPath, workingPath,
                metadataPath);
        minimizer.maximise();
    }
}
