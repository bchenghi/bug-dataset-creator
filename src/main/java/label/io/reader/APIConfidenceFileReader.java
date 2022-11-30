package label.io.reader;

import java.util.Iterator;

import org.json.JSONObject;

import jmutation.utils.JSONWrapper;
import label.APIConfidenceDict;
import label.io.writer.APIConfidenceFileWriter;

public class APIConfidenceFileReader {
	private final String pathToFile;

	public APIConfidenceFileReader(String pathToFile) {
		this.pathToFile = pathToFile;
	}
	
	public APIConfidenceDict read() {
		JSONObject contents = JSONWrapper.getJSONObjectFromFile(pathToFile);
		APIConfidenceDict result = new APIConfidenceDict();
		Iterator<String> keys = contents.keys();
		while(keys.hasNext()) {
			final String key = keys.next();
			JSONObject value = contents.getJSONObject(key);
			int totalCount = value.getInt(APIConfidenceFileWriter.TOTAL_COUNT_KEY);
			int correctCount = value.getInt(APIConfidenceFileWriter.CORRECT_COUNT_KEY);
			result.putValue(key, correctCount, totalCount);
		}
		return result;
	}
}
