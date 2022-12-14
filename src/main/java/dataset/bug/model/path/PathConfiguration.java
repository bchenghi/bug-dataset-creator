package dataset.bug.model.path;

import jmutation.model.mutation.DumpFilePathConfig;

public abstract class PathConfiguration {
    protected String repoPath;

    protected PathConfiguration(String repoPath) {
        this.repoPath = repoPath;
    }

    public abstract String getBugPath(String projectName, String bugId);

    public abstract String getFixPath(String projectName, String bugId);

    public abstract String getRelativeFixPath(String projectName, String bugId);

    public abstract String getBuggyPath(String projectName, String bugId);

    public abstract String getRelativeBuggyPath(String projectName, String bugId);

    public abstract String getRestorationInstructionsPath(String projectName, String bugId);

    public abstract String getRelativeRestorationInstructionsPath(String projectName, String bugId);

    public abstract String getMetadataPath(String projectName, String bugId);

    public abstract String getRelativeMetadataPath(String projectName, String bugId);

    public abstract String getLabelPath(String projectName, String bugId);

    public abstract String getRelativeLabelPath(String projectName, String bugId);

    public abstract String getStoragePath(String projectName);

    public abstract String getInstrumentatorFilePath(String projectName, String bugId, InstrumentatorFile fileType);

    public enum InstrumentatorFile {
        TRACE, BUGGY_TRACE, PRECHECK, BUGGY_PRECHECK, TRACE_W_ASSERTS, BUGGY_TRACE_W_ASSERTS;

        protected static String getFileName(InstrumentatorFile fileType) {
            switch (fileType) {
            case TRACE:
                return DumpFilePathConfig.DEFAULT_TRACE_FILE;
            case BUGGY_PRECHECK:
                return DumpFilePathConfig.DEFAULT_BUGGY_PRECHECK_FILE;
            case BUGGY_TRACE:
                return DumpFilePathConfig.DEFAULT_BUGGY_TRACE_FILE;
            case BUGGY_TRACE_W_ASSERTS:
                return DumpFilePathConfig.DEFAULT_BUGGY_TRACE_W_ASSERTS_FILE;
            case PRECHECK:
                return DumpFilePathConfig.DEFAULT_PRECHECK_FILE;
            case TRACE_W_ASSERTS:
                return DumpFilePathConfig.DEFAULT_TRACE_W_ASSERTS_FILE;
            default:
                return "";
            }
        }
    }
}
