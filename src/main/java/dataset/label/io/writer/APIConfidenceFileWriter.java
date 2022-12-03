package dataset.label.io.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.json.JSONObject;

import dataset.label.APIConfidenceDict;
import jmutation.utils.JSONWrapper;
import sav.common.core.Pair;

public class APIConfidenceFileWriter {
    public static final String TOTAL_COUNT_KEY = "total-count";
    public static final String CORRECT_COUNT_KEY = "correct-count";
    public static final String STORAGE_FILE = "api-confidence.json";

    private final String pathToFile;
    private final APIConfidenceDict dictionary;

    public APIConfidenceFileWriter(String pathToFile, APIConfidenceDict dictionary) {
        this.pathToFile = pathToFile;
        this.dictionary = dictionary;
    }

    public boolean write() {
        JSONObject json = getStartingJSONObject();
        List<String> keys = dictionary.getAPIList();
        for (String key : keys) {
            Pair<Integer, Integer> value = dictionary.getValue(key);
            JSONObject valueJSON = json.has(key) ? json.getJSONObject(key) : createEmptyValue();
            valueJSON.put(CORRECT_COUNT_KEY, valueJSON.getInt(CORRECT_COUNT_KEY) + value.first());
            valueJSON.put(TOTAL_COUNT_KEY, valueJSON.getInt(TOTAL_COUNT_KEY) + value.second());
            json.put(key, valueJSON);
        }
        try (FileWriter writer = new FileWriter(pathToFile)) {
            json.write(writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private JSONObject getStartingJSONObject() {
        File file = new File(pathToFile);
        if (file.exists()) {
            return JSONWrapper.getJSONObjectFromFile(file);
        }

        return new JSONObject();
    }

    private JSONObject createEmptyValue() {
        JSONObject result = new JSONObject();
        result.put(CORRECT_COUNT_KEY, 0);
        result.put(TOTAL_COUNT_KEY, 0);
        return result;
    }
}
