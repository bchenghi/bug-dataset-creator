package dataset.bug.model.project;

import jmutation.model.TestCase;

import java.util.List;

abstract public class DatasetProject {
    protected String projectPath;
    public DatasetProject(String projectPath) {
        this.projectPath = projectPath;
    }
    abstract public List<TestCase> getFailingTests();
}
