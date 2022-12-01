package dataset.execution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		final String projectName = "math_70";
		final String repoPath = "D:\\chenghin\\NUS";
		int startId = 1;
		int endId = 18000;
		int numOfCores = Runtime.getRuntime().availableProcessors() - 1;
      	ExecutorService executor = Executors.newFixedThreadPool(numOfCores);
		for (int bugId = startId; bugId <= endId; bugId++) {
			executor.submit(new Runner(repoPath, projectName, bugId));
		}
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}
}
