package dataset.bug;

import dataset.bug.model.path.MutationFrameworkPathConfiguration;
import dataset.bug.model.path.PathConfiguration.InstrumentatorFile;
import dataset.utils.Zipper;

public class BugZipper {
    private final String projectName;
    private final MutationFrameworkPathConfiguration pathConfig;
    public BugZipper(String repositoryPath, String projectName) {
        this.projectName = projectName;
        pathConfig = new MutationFrameworkPathConfiguration(repositoryPath);
    }
    
    public void zip(int bugId) {
        String pathToBug = pathConfig.getBuggyPath(projectName, Integer.toString(bugId));
        Zipper.zip(pathToBug);
    }
    
    public void unzip(int bugId) {
        String pathToBug = pathConfig.getBuggyPath(projectName, Integer.toString(bugId));
        Zipper.unzip(pathToBug, pathToBug);
    }
}
