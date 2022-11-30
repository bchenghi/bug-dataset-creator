package dataset.model.path;

import jmutation.model.mutation.DumpFilePathConfig;

abstract public class PathConfiguration {
    protected String repoPath;
    public PathConfiguration(String repoPath) {
        this.repoPath = repoPath;
    }

    abstract public String getBugPath(String projectName, String bugId);
    
    abstract public String getFixPath(String projectName, String bugId);
    abstract public String getRelativeFixPath(String projectName, String bugId);
    
    abstract public String getBuggyPath(String projectName, String bugId);
    abstract public String getRelativeBuggyPath(String projectName, String bugId);
    
    abstract public String getRestorationInstructionsPath(String projectName, String bugId);
    abstract public String getRelativeRestorationInstructionsPath(String projectName, String bugId);
    
    abstract public String getMetadataPath(String projectName, String bugId);
    abstract public String getRelativeMetadataPath(String projectName, String bugId);

    abstract public String getLabelPath(String projectName, String bugId);
    abstract public String getRelativeLabelPath(String projectName, String bugId);
    
    abstract public String getStoragePath(String projectName);
    
    abstract public String getInstrumentatorFilePath(String projectName, String bugId, InstrumentatorFile fileType);
    
    public static enum InstrumentatorFile {
    	TRACE, BUGGY_TRACE, PRECHECK, BUGGY_PRECHECK, TRACE_W_ASSERTS, BUGGY_TRACE_W_ASSERTS;
    	
    	protected static String getFileName(InstrumentatorFile fileType) {
    		switch(fileType) {
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
