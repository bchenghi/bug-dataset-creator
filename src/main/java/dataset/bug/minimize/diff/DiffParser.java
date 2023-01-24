package dataset.bug.minimize.diff;

import dataset.bug.minimize.instruction.Instruction;
import dataset.bug.minimize.instruction.Instruction.InstructionType;
import jmutation.constants.OperatingSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static dataset.bug.minimize.instruction.Instruction.InstructionType.*;

/**
 * Parses the result from git diff between working and buggy project, and generates instructions
 */
public class DiffParser {
    public static String ADDED_OR_REMOVED_FILE_NAME = "/dev/null";
    private static String SOURCE_FILE_PREFIX = "a" + File.separator;
    private static String TARGET_FILE_PREFIX = "b" + File.separator;

    public static List<Instruction> parse(List<String> gitDiff, String workingProject, String buggyProject) {
        // TODO: Handle obtaining relative paths. Handle delete command.
        List<Instruction> instructionList = new ArrayList<>();

        for(String line: gitDiff) {
            if (!filter(line)) continue;
            FilePair filePair = createFilePair(cleanUpLine(line), workingProject, buggyProject);
            instructionList.add(generateInstructionFromFilePair(ADD, filePair));
        }

        return instructionList;
    }

    private static FilePair createFilePair(String line, String workingProject, String buggyProject) {
        assert(line.startsWith("diff"));
        int indexOfB = line.indexOf(TARGET_FILE_PREFIX);
        String workingFile = line.substring(line.indexOf(SOURCE_FILE_PREFIX) + 2, indexOfB - 3);
        System.out.println("working file0: " + workingFile);
        workingFile = workingFile.substring(workingProject.length() + 1);
        System.out.println("working file1: " + workingFile);
        String buggyFile = line.substring(indexOfB + 2, line.length() - 1);
        System.out.println("buggyFile file0: " + buggyFile);
        buggyFile = buggyFile.substring(buggyProject.length() + 1);
        System.out.println("buggyFile file1: " + buggyFile);
        return new FilePair(workingFile, buggyFile);
    }

    private static Instruction generateInstructionFromFilePair(InstructionType type, FilePair filePair) {
        switch (type) {
        case ADD:
            return new Instruction(type, filePair.buggyFile, filePair.buggyFile);
        case DELETE:
            return new Instruction(type, null, filePair.workingFile);
        default:
            return null;
        }
    }

    private static String cleanUpLine(String line) {
        String result = line.replace("\\\\", File.separator);
        result = result.replace("/", File.separator);
        return result;
    }

    private static boolean filter(String line) {
        return line.contains(".java") && line.startsWith("diff");
    }
    private record FilePair(String workingFile, String buggyFile){}
}
