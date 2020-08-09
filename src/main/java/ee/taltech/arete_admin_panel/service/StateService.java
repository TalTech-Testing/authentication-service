package ee.taltech.arete_admin_panel.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StateService {

	public static List<String> tailFile(final Path source, final int noOfLines) throws IOException {
		try (Stream<String> stream = Files.lines(source)) {
			FileBuffer fileBuffer = new FileBuffer(noOfLines);
			stream.forEach(fileBuffer::collect);
			return fileBuffer.getLines();
		}
	}

	private static final class FileBuffer {
		private final int noOfLines;
		private final String[] lines;
		private int offset = 0;

		public FileBuffer(int noOfLines) {
			this.noOfLines = noOfLines;
			this.lines = new String[noOfLines];
		}

		public void collect(String line) {
			lines[offset++ % noOfLines] = line;
		}

		public List<String> getLines() {
			return IntStream.range(offset < noOfLines ? 0 : offset - noOfLines, offset)
					.mapToObj(idx -> lines[idx % noOfLines]).collect(Collectors.toList());
		}
	}
}
