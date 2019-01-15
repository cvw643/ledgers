package de.adorsys.ledgers.data.importer.core;

import java.io.InputStream;

import org.springframework.stereotype.Service;

import de.adorsys.ledgers.data.importer.data.MockbankInitData;
@Service
public class LedgerClientDataImporter {
	
	private final ImportOperationProccessor operationProcessor;
	
	public LedgerClientDataImporter(ImportOperationProccessor operationProcessor) {
		this.operationProcessor = operationProcessor;
	}

	public boolean readFileAndImportData(String filePath) {
		// System Checker : Test if the Systems if configured and runnable.
		new SystemChecker().filePath(filePath).validatePath();
		// File Manager : Read files
		InputStream stream = new FileManager(filePath).getStream();
		// Object Mapper : Convert data read from file to java objects
		MockbankInitData data = new StreamMapper(stream, MapperType.YAML).mapTo(MockbankInitData.class);
		// Import Operations : Performs Data Operations
		boolean processImport = operationProcessor.processImport(data);
		// File Manager: Version the file
		return processImport;
	}
}
