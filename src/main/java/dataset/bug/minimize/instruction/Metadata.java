package dataset.bug.minimize.instruction;

import java.util.List;

public record Metadata(List<Instruction> instructionList, String pathToFixed,
                       String pathToBuggy, String pathToMetaData) {
    public static final String PATH_TO_FIXED_KEY = "fixedPath";
    public static final String PATH_TO_BUGGY_KEY = "buggyPath";
    public static final String PATH_TO_METADATA_KEY = "metadataPath";
    public static final String INSTRUCTIONS_KEY = "instructions";
}
