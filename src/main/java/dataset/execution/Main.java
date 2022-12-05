package dataset.execution;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dataset.execution.handler.BugCheckHandler;

public class Main {
    private static final int INSTRUMENTATION_TIMEOUT = 5;
    public static void main(String[] args) throws InterruptedException {
        final String projectName = "math_70";
        final String repoPath = "D:\\chenghin\\NUS";
        int startId = 1;
        int endId = 18000;
        List<Integer> bugIds = new ArrayList<>();
        for (int bugId = startId; bugId <= endId; bugId++) {
            bugIds.add(bugId);
        }
        //      	checkDone(repoPath, projectName, bugIds);
        runTraceAndLabelCollection(repoPath, projectName, bugIds);
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

    private static void runTraceAndLabelCollection(String repoPath, String projectName, List<Integer> bugIds) throws InterruptedException {
//        int numOfCores = Runtime.getRuntime().availableProcessors() - 1;
        int numOfCores = 1;
        ExecutorService executor = Executors.newFixedThreadPool(numOfCores);
        Collections.shuffle(bugIds);
        System.out.println("Done with shuffling");
        for (int bugId : bugIds) {
            if (new File(repoPath + File.separator + projectName + File.separator + bugId).exists()) {
                executor.submit(new Runner(repoPath, projectName, bugId, INSTRUMENTATION_TIMEOUT));
            }
        }
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}