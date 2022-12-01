package dataset.execution.handler;

import java.io.File;

import dataset.bug.creator.MutationFrameworkDatasetCreator;
import dataset.bug.minimize.ProjectMinimizer;
import dataset.bug.model.path.MutationFrameworkPathConfiguration;
import dataset.bug.model.path.PathConfiguration;
import dataset.execution.Request;

public class MaximizeHandler extends Handler {
    private final ProjectMinimizer minimizer;
    
    public MaximizeHandler(Handler next, String repositoryPath, String projectName, int bugId) {
    	super(next);
        minimizer = createMinimizer(repositoryPath, projectName, bugId);
    }
    
	@Override
	public boolean individualHandler(Request request) {
		return minimizer.maximise();
	}    
	
    static ProjectMinimizer createMinimizer(String repositoryPath, String projectName, int bugId) {
        PathConfiguration pathConfiguration = new MutationFrameworkPathConfiguration(repositoryPath);
        String metadataPath = pathConfiguration.getRelativeMetadataPath(projectName, Integer.toString(bugId));
        pathConfiguration.getBuggyPath(projectName, Integer.toString(bugId)).substring(repositoryPath.length());
        return new ProjectMinimizer(repositoryPath, String.join(File.separator,
                projectName, Integer.toString(bugId), MutationFrameworkDatasetCreator.BUGGY_PROJECT_DIR),
                String.join(File.separator, projectName, MutationFrameworkDatasetCreator.WORKING_PROJECT_DIR),
                metadataPath);
    }
}
