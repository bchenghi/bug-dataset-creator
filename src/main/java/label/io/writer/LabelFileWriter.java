package label.io.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;


public class LabelFileWriter {
	private final String path;
	public LabelFileWriter(String path) {
		this.path = path;
	}

	public boolean write(Map<String, Double> mapOfLabels) {
		File file = new File(path);
		
		JSONObject contents = new JSONObject(mapOfLabels);
		try (FileWriter writer = new FileWriter(file)) {
			contents.write(writer);
		} catch (JSONException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
