package dataset.bug.minimize.instruction;

import org.json.JSONObject;

public record Instruction(InstructionType instructionType, String pathInMetadata, String pathInTarget) {
    public static final String PATH_IN_METADATA_KEY = "pathInMetadata";
    public static final String PATH_IN_TARGET_KEY = "pathInTarget";
    public static final String INSTRUCTION_TYPE_KEY = "instructionType";
    public enum InstructionType {
        ADD, DELETE
    }

    public JSONObject getValue() {
        JSONObject result = new JSONObject();
        result.put(INSTRUCTION_TYPE_KEY, instructionType);
        result.put(PATH_IN_METADATA_KEY, pathInMetadata);
        result.put(PATH_IN_TARGET_KEY, pathInTarget);
        return result;
    }
}
