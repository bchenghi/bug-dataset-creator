package dataset.model.path;

abstract public class PathConfiguration {
    protected String repoPath;
    public PathConfiguration(String repoPath) {
        this.repoPath = repoPath;
    }

    abstract public String getBugPath(String projectName, String bugId);
    abstract public String getBuggyPath(String projectName, String bugId);
    abstract public String getFixPath(String projectName, String bugId);
    abstract public String getRelativeBuggyPath(String projectName, String bugId);
    abstract public String getRelativeFixPath(String projectName, String bugId);
    abstract public String getRestorationInstructionsPath(String projectName, String bugId);
    abstract public String getRelativeRestorationInstructionsPath(String projectName, String bugId);
    abstract public String getMetadataPath(String projectName, String bugId);
    abstract public String getRelativeMetadataPath(String projectName, String bugId);
    abstract public String getStoragePath(String projectName);
}
