package dataset.label.io.reader;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import jmutation.utils.JSONWrapper;

public class LabelFileReader {
    private final String path;
    public LabelFileReader(String path) {
        this.path = path;
    }

    public Map<String, Double> read() {
        JSONObject contents = JSONWrapper.getJSONObjectFromFile(path);
        Map<String, Object> mapOfObjs = contents.toMap();
        Map<String, Double> result = new HashMap<>();
        for (Entry<String, Object> entry : mapOfObjs.entrySet()) {
            result.put(entry.getKey(), (double) entry.getValue());
        }
        return result;
    }
}
