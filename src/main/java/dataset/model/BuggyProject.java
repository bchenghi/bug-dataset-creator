package dataset.model;

import jmutation.model.TestCase;
import jmutation.mutation.MutationCommand;
import org.json.JSONObject;

public record BuggyProject(TestCase testCase, MutationCommand command, String projectName) {

    public JSONObject createJSONObject() {
        JSONObject value = new JSONObject();
        value.put("testCase", testCase.toString());
        value.put("command", command.toString());
        value.put("projectName", projectName);
        return value;
    }

    public String key() {
        return toString();
    }

    @Override
    public String toString() {
        return projectName + "#" + testCase + "#" + command;
    }
}
