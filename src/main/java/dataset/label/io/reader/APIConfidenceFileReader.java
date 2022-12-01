package dataset.label.io.reader;

import java.util.Iterator;

import org.json.JSONObject;

import dataset.label.APIConfidenceDict;
import dataset.label.io.writer.APIConfidenceFileWriter;
import jmutation.utils.JSONWrapper;

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
