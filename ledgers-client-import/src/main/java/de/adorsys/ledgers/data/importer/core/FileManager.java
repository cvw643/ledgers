package de.adorsys.ledgers.data.importer.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {
	
	private final String filePath;

	public FileManager(String filePath) {
		this.filePath = filePath;
	}

	public InputStream getStream() {
		Path path = Paths.get(this.filePath);
		try {
			return new FileInputStream(path.toFile());
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(String.format("Cannot load file %s", this.filePath), e);
		}
	}
	
}
