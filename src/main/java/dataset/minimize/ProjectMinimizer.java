package dataset.minimize;

import dataset.minimize.diff.DiffParser;
import dataset.minimize.diff.GitWrapper;
import dataset.minimize.instruction.Instruction;
import dataset.minimize.instruction.Instruction.InstructionType;
import dataset.minimize.instruction.Metadata;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProjectMinimizer {
    public static final String METADATA_FILE_NAME = "metadata.json";
    public static final String METADATA_DIR = "metadata";
    private final String buggyProject;
    private final String relativeBuggyProject;
    private final String workingProject;
    private final String relativeWorkingProject;
    private final String relativeMetadata;
    private final String metadataPath;

    public ProjectMinimizer(String repoPath, String relativeBuggyProject, String relativeWorkingProject, String relativeMetadataPath) {
        this.relativeWorkingProject = relativeWorkingProject;
        this.relativeBuggyProject = relativeBuggyProject;
        this.relativeMetadata = relativeMetadataPath;
        this.buggyProject = repoPath + File.separator + relativeBuggyProject;
        this.workingProject = repoPath + File.separator + relativeWorkingProject;
        this.metadataPath = repoPath + File.separator + relativeMetadataPath;
    }

    public void minimize() {
        if (!(new File(workingProject).exists() && new File(buggyProject).exists())) {
            System.out.println(workingProject + " " +  buggyProject + " does not exist");
            return;
        }
        List<String> diffResult = GitWrapper.getRawDiff(workingProject, buggyProject);
        List<Instruction> instructionList = DiffParser.parse(diffResult, workingProject, buggyProject);
        List<String> filesToAdd = new ArrayList<>();
        for (Instruction instruction : instructionList) {
            if (instruction.instructionType().equals(InstructionType.ADD)) {
                filesToAdd.add(instruction.pathInTarget());
            }
        }
        copyOverFiles(filesToAdd, metadataPath);
        Metadata metadata = generateMetadata(instructionList);
        MetadataWriter.write(metadataPath + File.separator + METADATA_FILE_NAME, metadata);
        try {
            FileUtils.deleteDirectory(new File(buggyProject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyOverFiles(List<String> filesToCopy, String targetPath) {
        for (String filePath : filesToCopy) {
            try {
                FileUtils.copyFileToDirectory(new File(buggyProject + File.separator + filePath),
                        new File(targetPath + File.separator + getDirectory(filePath)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Metadata generateMetadata(List<Instruction> instructionList) {
        return new Metadata(instructionList, relativeWorkingProject, relativeBuggyProject, relativeMetadata);
    }

    private String getDirectory(String file) {
        int lastIdxOfSeparator = file.lastIndexOf(File.separator);
        return file.substring(0, lastIdxOfSeparator);
    }

    public void maximise() {
        try {
            FileUtils.copyDirectory(new File(workingProject), new File(buggyProject));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Metadata metadata = MetadataParser.parse(metadataPath + File.separator + METADATA_FILE_NAME);
        List<Instruction> instructionList = metadata.instructionList();
        for (Instruction instruction : instructionList) {
            applyInstruction(instruction);
        }
    }

    private void applyInstruction(Instruction instruction) {
       String fileInMetaData = metadataPath + File.separator + instruction.pathInMetadata();
       String destInBuggyTarget = buggyProject + File.separator + instruction.pathInTarget();
       try {
           FileUtils.copyFile(new File(fileInMetaData), new File(destInBuggyTarget), false);
       } catch (IOException e) {
           e.printStackTrace();
       }
    }
}
