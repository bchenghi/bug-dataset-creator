package label;

import java.util.Map;

import label.io.reader.LabelFileReader;
import label.io.writer.LabelFileWriter;

public class LabelManager {
	private final LabelFileReader reader;
	private final LabelFileWriter writer;
	private final Labeller labeller;
	private final String pathToBuggyTrace;
	private final String pathToWorkingTrace;



	public LabelManager(LabelFileReader reader, LabelFileWriter writer, Labeller labeller, String pathToBuggyTrace,
			String pathToWorkingTrace) {
		super();
		this.reader = reader;
		this.writer = writer;
		this.labeller = labeller;
		this.pathToBuggyTrace = pathToBuggyTrace;
		this.pathToWorkingTrace = pathToWorkingTrace;
	}

	public void label() {
		// Read working and buggy trace files, create pair list, and diffmatcher
		// Obtain probability of each node from buggy trace
		// Pass the dictionary to writer
	}
	
	public Map<String, Double> readLabels() {
		return reader.read();
	}
}
